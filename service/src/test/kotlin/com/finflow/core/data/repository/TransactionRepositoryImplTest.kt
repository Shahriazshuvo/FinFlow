package com.finflow.core.data.repository

import com.finflow.core.common.error.AppError
import com.finflow.core.common.result.AppResult
import com.finflow.core.data.session.CurrentUserProvider
import com.finflow.core.data.sync.SyncTrigger
import com.finflow.core.data.sync.Synchronizer
import com.finflow.core.database.datasource.TransactionLocalDataSource
import com.finflow.core.model.Money
import com.finflow.core.model.PendingRecord
import com.finflow.core.model.SyncStatus
import com.finflow.core.data.sync.SyncTable
import com.finflow.core.model.Transaction
import com.finflow.core.model.TransactionDraft
import com.finflow.core.model.TransactionType
import com.finflow.core.network.datasource.TransactionRemoteDataSource
import com.finflow.core.network.error.DataErrorMapper
import com.finflow.core.testing.TEST_CLOCK
import com.finflow.core.testing.TEST_INSTANT
import com.finflow.core.testing.TEST_USER_ID
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * The offline-first contract from APP_SPEC.md §12 and §18: a write lands in Room as pending
 * without touching the network, a successful sync flips it to SYNCED, and a failed sync
 * leaves it pending so nothing is ever lost.
 */
class TransactionRepositoryImplTest {

    private val local = mockk<TransactionLocalDataSource>(relaxed = true)
    private val remote = mockk<TransactionRemoteDataSource>(relaxed = true)
    private val currentUser = mockk<CurrentUserProvider>()
    private val syncTrigger = mockk<SyncTrigger>(relaxed = true)
    private val synchronizer = mockk<Synchronizer>(relaxed = true)
    private val clock = TEST_CLOCK

    private lateinit var repository: TransactionRepositoryImpl

    @Before
    fun setUp() {
        repository = TransactionRepositoryImpl(
            local = local,
            remote = remote,
            currentUser = currentUser,
            errorMapper = DataErrorMapper(),
            syncTrigger = syncTrigger,
            clock = clock,
        )
        coEvery { currentUser.requireUserId() } returns AppResult.Success(USER_ID)
        coEvery { currentUser.userIdOrNull() } returns USER_ID
        coEvery { synchronizer.lastSyncedAt(SyncTable.TRANSACTIONS) } returns null
    }

    @Test
    fun `offline create stores the row as PENDING_CREATE without calling the network`() = runTest {
        coEvery { local.getById(any()) } returns null
        val captured = slot<Transaction>()
        coEvery { local.upsert(capture(captured), any()) } just Runs

        val result = repository.save(draft())

        assertTrue(result is AppResult.Success)
        assertEquals(SyncStatus.PENDING_CREATE, captured.captured.syncStatus)
        assertEquals(USER_ID, captured.captured.userId)
        assertEquals(NOW, captured.captured.updatedAt)
        coVerify(exactly = 0) { remote.upsert(any(), any()) }
        verify { syncTrigger.requestSync() }
    }

    @Test
    fun `editing a row that never reached the server keeps it PENDING_CREATE`() = runTest {
        coEvery { local.getById(EXISTING_ID) } returns
            existing(SyncStatus.PENDING_CREATE)
        val captured = slot<Transaction>()
        coEvery { local.upsert(capture(captured), any()) } just Runs

        repository.save(draft(id = EXISTING_ID, amount = "99.00"))

        // Otherwise sync would try to update a row Postgres has never seen.
        assertEquals(SyncStatus.PENDING_CREATE, captured.captured.syncStatus)
    }

    @Test
    fun `editing a synced row marks it PENDING_UPDATE and preserves createdAt`() = runTest {
        coEvery { local.getById(EXISTING_ID) } returns existing(SyncStatus.SYNCED)
        val captured = slot<Transaction>()
        coEvery { local.upsert(capture(captured), any()) } just Runs

        repository.save(draft(id = EXISTING_ID, amount = "99.00"))

        assertEquals(SyncStatus.PENDING_UPDATE, captured.captured.syncStatus)
        assertEquals(CREATED_AT, captured.captured.createdAt)
    }

    @Test
    fun `deleting a synced row soft-deletes it so the tombstone can be pushed`() = runTest {
        coEvery { local.getById(EXISTING_ID) } returns existing(SyncStatus.SYNCED)

        repository.delete(EXISTING_ID)

        coVerify { local.markDeleted(EXISTING_ID, NOW) }
        coVerify(exactly = 0) { local.deleteHard(any()) }
        verify { syncTrigger.requestSync() }
    }

    @Test
    fun `deleting a row that never synced removes it outright`() = runTest {
        coEvery { local.getById(EXISTING_ID) } returns existing(SyncStatus.PENDING_CREATE)

        repository.delete(EXISTING_ID)

        // No tombstone is needed for a row the server has never seen.
        coVerify { local.deleteHard(EXISTING_ID) }
        coVerify(exactly = 0) { local.markDeleted(any(), any()) }
    }

    @Test
    fun `successful sync pushes pending rows and marks them synced`() = runTest {
        val pending = existing(SyncStatus.PENDING_CREATE)
        coEvery { local.getPending() } returnsMany listOf(
            listOf(PendingRecord(pending.id, pending, SyncStatus.PENDING_CREATE, null)),
            emptyList(),
        )
        coEvery { remote.fetchSince(USER_ID, null) } returns emptyList()

        val synced = repository.syncWith(synchronizer)

        assertTrue(synced)
        coVerify { remote.upsert(pending, null) }
        coVerify { local.markSynced(pending.id, NOW, pending.updatedAt) }
    }

    @Test
    fun `failed sync leaves the row pending and reports failure for retry`() = runTest {
        val pending = existing(SyncStatus.PENDING_UPDATE)
        coEvery { local.getPending() } returns
            listOf(PendingRecord(pending.id, pending, SyncStatus.PENDING_UPDATE, null))
        coEvery { remote.upsert(any(), any()) } throws IOException("offline")

        val synced = repository.syncWith(synchronizer)

        assertTrue(!synced)
        coVerify(exactly = 0) { local.markSynced(any(), any(), any()) }
        coVerify(exactly = 0) { synchronizer.updateLastSyncedAt(any(), any()) }
    }

    @Test
    fun `saving without a session fails as unauthorized`() = runTest {
        coEvery { currentUser.requireUserId() } returns AppResult.Failure(AppError.Unauthorized)

        val result = repository.save(draft())

        assertEquals(AppResult.Failure(AppError.Unauthorized), result)
        coVerify(exactly = 0) { local.upsert(any(), any()) }
    }

    private fun draft(id: String? = null, amount: String = "12.50") = TransactionDraft(
        id = id,
        accountId = ACCOUNT_ID,
        categoryId = CATEGORY_ID,
        type = TransactionType.EXPENSE,
        amount = Money.ofMajorUnits(amount),
        note = "  lunch  ",
        transactionDate = LocalDate.of(2026, 9, 3),
    )

    private fun existing(status: SyncStatus) = Transaction(
        id = EXISTING_ID,
        userId = USER_ID,
        accountId = ACCOUNT_ID,
        categoryId = CATEGORY_ID,
        type = TransactionType.EXPENSE,
        amount = Money.ofMajorUnits("12.50"),
        note = "lunch",
        transactionDate = LocalDate.of(2026, 9, 3),
        createdAt = CREATED_AT,
        updatedAt = CREATED_AT,
        syncStatus = status,
    )

    private companion object {
        val NOW: Instant = TEST_INSTANT
        val CREATED_AT: Instant = Instant.parse("2026-09-01T08:00:00Z")
        const val USER_ID = TEST_USER_ID
        const val ACCOUNT_ID = "account-1"
        const val CATEGORY_ID = "category-1"
        const val EXISTING_ID = "transaction-1"
    }
}

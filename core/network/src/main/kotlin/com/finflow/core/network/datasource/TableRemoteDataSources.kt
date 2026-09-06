package com.finflow.core.network.datasource

import com.finflow.core.model.Account
import com.finflow.core.model.Budget
import com.finflow.core.model.Category
import com.finflow.core.model.Goal
import com.finflow.core.model.Profile
import com.finflow.core.model.RemoteRecord
import com.finflow.core.model.Transaction
import com.finflow.core.network.dto.AccountDto
import com.finflow.core.network.dto.BudgetDto
import com.finflow.core.network.dto.CategoryDto
import com.finflow.core.network.dto.GoalDto
import com.finflow.core.network.dto.ProfileDto
import com.finflow.core.network.dto.ProfileUpdateDto
import com.finflow.core.network.dto.TransactionDto
import com.finflow.core.network.mapper.toDomain
import com.finflow.core.network.mapper.toDto
import com.finflow.core.network.util.toInstantOrNull
import com.finflow.core.network.util.toPostgresTimestamp
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * One remote source per table. Pulls are incremental (`updated_at > watermark`) and
 * deliberately include soft-deleted rows, because a tombstone is exactly what the local
 * store needs in order to remove a record another device deleted.
 *
 * Row-level security scopes every one of these queries to the signed-in user, so the
 * `user_id` filter is defence in depth rather than the security boundary (APP_SPEC.md §25).
 */

internal object SupabaseTables {
    const val PROFILES = "profiles"
    const val ACCOUNTS = "accounts"
    const val CATEGORIES = "categories"
    const val TRANSACTIONS = "transactions"
    const val BUDGETS = "budgets"
    const val GOALS = "goals"
    const val COLUMN_USER_ID = "user_id"
    const val COLUMN_UPDATED_AT = "updated_at"
    const val COLUMN_ID = "id"
}

@Singleton
class ProfileRemoteDataSource @Inject constructor(private val client: SupabaseClient) {

    suspend fun fetch(userId: String): Profile? = client.from(SupabaseTables.PROFILES)
        .select {
            filter { eq(SupabaseTables.COLUMN_ID, userId) }
        }
        .decodeList<ProfileDto>()
        .firstOrNull()
        ?.toDomain()

    /**
     * `profiles` rows are created by the `handle_new_user` trigger on signup, so the client
     * only ever patches the two user-editable columns.
     */
    suspend fun update(profile: Profile) {
        client.from(SupabaseTables.PROFILES).update(
            ProfileUpdateDto(fullName = profile.fullName, currencyCode = profile.currencyCode),
        ) {
            filter { eq(SupabaseTables.COLUMN_ID, profile.id) }
        }
    }
}

@Singleton
class AccountRemoteDataSource @Inject constructor(private val client: SupabaseClient) {

    suspend fun fetchSince(userId: String, since: Instant?): List<RemoteRecord<Account>> =
        client.from(SupabaseTables.ACCOUNTS)
            .select {
                filter {
                    eq(SupabaseTables.COLUMN_USER_ID, userId)
                    since?.let {
                        filter(
                            SupabaseTables.COLUMN_UPDATED_AT,
                            FilterOperator.GT,
                            it.toPostgresTimestamp(),
                        )
                    }
                }
            }
            .decodeList<AccountDto>()
            .map { RemoteRecord(record = it.toDomain(), deletedAt = it.deletedAt.toInstantOrNull()) }

    suspend fun upsert(account: Account, deletedAt: Instant? = null) {
        client.from(SupabaseTables.ACCOUNTS).upsert(account.toDto(deletedAt))
    }
}

@Singleton
class CategoryRemoteDataSource @Inject constructor(private val client: SupabaseClient) {

    suspend fun fetchSince(userId: String, since: Instant?): List<RemoteRecord<Category>> =
        client.from(SupabaseTables.CATEGORIES)
            .select {
                filter {
                    eq(SupabaseTables.COLUMN_USER_ID, userId)
                    since?.let {
                        filter(
                            SupabaseTables.COLUMN_UPDATED_AT,
                            FilterOperator.GT,
                            it.toPostgresTimestamp(),
                        )
                    }
                }
            }
            .decodeList<CategoryDto>()
            .map { RemoteRecord(record = it.toDomain(), deletedAt = it.deletedAt.toInstantOrNull()) }

    suspend fun upsert(category: Category, deletedAt: Instant? = null) {
        client.from(SupabaseTables.CATEGORIES).upsert(category.toDto(deletedAt))
    }
}

@Singleton
class TransactionRemoteDataSource @Inject constructor(private val client: SupabaseClient) {

    suspend fun fetchSince(userId: String, since: Instant?): List<RemoteRecord<Transaction>> =
        client.from(SupabaseTables.TRANSACTIONS)
            .select {
                filter {
                    eq(SupabaseTables.COLUMN_USER_ID, userId)
                    since?.let {
                        filter(
                            SupabaseTables.COLUMN_UPDATED_AT,
                            FilterOperator.GT,
                            it.toPostgresTimestamp(),
                        )
                    }
                }
            }
            .decodeList<TransactionDto>()
            .map { RemoteRecord(record = it.toDomain(), deletedAt = it.deletedAt.toInstantOrNull()) }

    suspend fun upsert(transaction: Transaction, deletedAt: Instant? = null) {
        client.from(SupabaseTables.TRANSACTIONS).upsert(transaction.toDto(deletedAt))
    }
}

@Singleton
class BudgetRemoteDataSource @Inject constructor(private val client: SupabaseClient) {

    suspend fun fetchSince(userId: String, since: Instant?): List<RemoteRecord<Budget>> =
        client.from(SupabaseTables.BUDGETS)
            .select {
                filter {
                    eq(SupabaseTables.COLUMN_USER_ID, userId)
                    since?.let {
                        filter(
                            SupabaseTables.COLUMN_UPDATED_AT,
                            FilterOperator.GT,
                            it.toPostgresTimestamp(),
                        )
                    }
                }
            }
            .decodeList<BudgetDto>()
            .map { RemoteRecord(record = it.toDomain(), deletedAt = it.deletedAt.toInstantOrNull()) }

    suspend fun upsert(budget: Budget, deletedAt: Instant? = null) {
        client.from(SupabaseTables.BUDGETS).upsert(budget.toDto(deletedAt))
    }
}

@Singleton
class GoalRemoteDataSource @Inject constructor(private val client: SupabaseClient) {

    suspend fun fetchSince(userId: String, since: Instant?): List<RemoteRecord<Goal>> =
        client.from(SupabaseTables.GOALS)
            .select {
                filter {
                    eq(SupabaseTables.COLUMN_USER_ID, userId)
                    since?.let {
                        filter(
                            SupabaseTables.COLUMN_UPDATED_AT,
                            FilterOperator.GT,
                            it.toPostgresTimestamp(),
                        )
                    }
                }
            }
            .decodeList<GoalDto>()
            .map { RemoteRecord(record = it.toDomain(), deletedAt = it.deletedAt.toInstantOrNull()) }

    suspend fun upsert(goal: Goal, deletedAt: Instant? = null) {
        client.from(SupabaseTables.GOALS).upsert(goal.toDto(deletedAt))
    }
}

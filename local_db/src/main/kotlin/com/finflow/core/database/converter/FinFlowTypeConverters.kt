package com.finflow.core.database.converter

import androidx.room.TypeConverter
import com.finflow.core.model.AccountType
import com.finflow.core.model.SyncStatus
import com.finflow.core.model.TransactionType
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth

/**
 * Dates are stored as ISO text rather than epoch numbers: they sort correctly with plain
 * string comparison (so `BETWEEN` range filters work), and they stay readable in the
 * database inspector. Instants stay numeric because they are only ever compared, never
 * ranged over by a user-facing filter.
 */
internal class FinFlowTypeConverters {

    @TypeConverter
    fun instantToEpochMillis(value: Instant?): Long? = value?.toEpochMilli()

    @TypeConverter
    fun epochMillisToInstant(value: Long?): Instant? = value?.let(Instant::ofEpochMilli)

    @TypeConverter
    fun localDateToString(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun stringToLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @TypeConverter
    fun yearMonthToString(value: YearMonth?): String? = value?.toString()

    @TypeConverter
    fun stringToYearMonth(value: String?): YearMonth? = value?.let(YearMonth::parse)

    @TypeConverter
    fun transactionTypeToString(value: TransactionType?): String? = value?.name

    @TypeConverter
    fun stringToTransactionType(value: String?): TransactionType? =
        value?.let(TransactionType::valueOf)

    @TypeConverter
    fun accountTypeToString(value: AccountType?): String? = value?.name

    @TypeConverter
    fun stringToAccountType(value: String?): AccountType? = value?.let(AccountType::valueOf)

    @TypeConverter
    fun syncStatusToString(value: SyncStatus?): String? = value?.name

    @TypeConverter
    fun stringToSyncStatus(value: String?): SyncStatus? = value?.let(SyncStatus::valueOf)
}

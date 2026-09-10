package com.finflow.core.model

import java.time.LocalDate

/**
 * Query shape for the transaction list. Every field is optional; `null` means "no
 * restriction on this dimension".
 */
data class TransactionFilter(
    val type: TransactionType? = null,
    val categoryIds: Set<String> = emptySet(),
    val accountIds: Set<String> = emptySet(),
    val from: LocalDate? = null,
    val to: LocalDate? = null,
    val query: String? = null,
) {
    val isActive: Boolean
        get() = type != null ||
            categoryIds.isNotEmpty() ||
            accountIds.isNotEmpty() ||
            from != null ||
            to != null ||
            !query.isNullOrBlank()

    companion object {
        val None = TransactionFilter()
    }
}

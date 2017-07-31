package io.userfeeds.whitelist

import java.math.BigDecimal

data class WhitelistedRankingItem(
        val id: String,
        val target: String,
        val score: BigDecimal,
        val total: BigDecimal,
        val title: String?,
        val summary: String?,
        val whitelisted: Boolean
)

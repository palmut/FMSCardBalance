package net.palmut.fmscardbalance.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BalanceResponse(
    @SerialName("availableAmount") val availableAmount: Double? = null
)
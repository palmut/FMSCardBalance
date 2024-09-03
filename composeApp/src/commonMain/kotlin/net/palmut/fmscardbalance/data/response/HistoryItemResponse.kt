package net.palmut.fmscardbalance.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HistoryItemResponse(
    @SerialName("time") val time: String? = null,
    @SerialName("amount") val amount: Double? = null,
    @SerialName("locationName") val locationName: List<String>? = null,
    @SerialName("trnType") val trnType: Int? = null,
    @SerialName("mcc") val mcc: String? = null,
    @SerialName("currency") val currency: String? = null,
    @SerialName("merchantId") val merchantId: String? = null,
    @SerialName("reversal") val reversal: Boolean? = null,
    @SerialName("posRechargeReceipt") val posRechargeReceipt: String? = null
)
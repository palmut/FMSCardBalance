package net.palmut.fmscardbalance.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Data(
    @SerialName("status") val status: String? = null,
    @SerialName("maskedPan") val maskedPan: String? = null,
    @SerialName("activationDate") val activationDate: String? = null,
    @SerialName("orderId") val orderId: String? = null,
    @SerialName("validUntil") val validUntil: String?,
    @SerialName("goalCard") val goalCard: Boolean? = null,
    @SerialName("activationCode") val activationCode: String? = null,
    @SerialName("expireDate") val expireDate: String? = null,
    @SerialName("authLimitId") val authLimitId: String? = null,
    @SerialName("customDomainPart") val customDomainPart: String? = null,
    @SerialName("paymentDate") val paymentDate: String? = null,
    @SerialName("smsNotificationAvailable") val smsNotificationAvailable: Boolean? = null,
    @SerialName("balance") val balance: Balance? = null,
    @SerialName("history") val history: List<HistoryItem>? = null,
    @SerialName("phone") val phone: String? = null,
    @SerialName("cardType") val cardType: String? = null,
    @SerialName("smsInfoStatus") val smsInfoStatus: String? = null
)
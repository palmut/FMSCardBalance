package net.palmut.fmscardbalance.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Response(
    @SerialName("status") val status: String? = null,
    @SerialName("messages") val messages: List<MessageResponse>? = null,
    @SerialName("data") val data: DataResponse? = null
)
package net.palmut.fmscardbalance.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Message(
    @SerialName("type") val type: String? = null,
    @SerialName("context") val context: String? = null,
    @SerialName("code") val code: String? = null,
    @SerialName("path") val path: String? = null
)
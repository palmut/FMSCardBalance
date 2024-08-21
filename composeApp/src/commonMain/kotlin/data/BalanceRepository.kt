package data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.encodeURLPath
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class BalanceRepository {

    private val client = HttpClient {
        engine {
            pipelining = true
        }
        install(Logging) {
            logger = Logger.SIMPLE
            level = LogLevel.ALL
        }
        install(ContentNegotiation) {
            json()
        }
        install(HttpTimeout) {
            socketTimeoutMillis = 10000
        }
    }

    suspend fun getBalance(phone: String, pan: String): Response = withContext(Dispatchers.IO) {
        val url = "https://meal.gift-cards.ru/api/1/virtual-cards/$phone/$pan".encodeURLPath()
        client.get(url).body<Response>()
    }

    @Serializable
    data class Response(
        @SerialName("status")
        val status: String? = null,
        @SerialName("messages")
        val messages: List<Message>? = null,
        @SerialName("data")
        val data: Data? = null
    )

    @Serializable
    data class Message(
        @SerialName("type")
        val type: String? = null,
        @SerialName("context")
        val context: String? = null,
        @SerialName("code")
        val code: String? = null,
        @SerialName("path")
        val path: String? = null
    )

    @Serializable
    data class Data(
        @SerialName("status")
        val status: String? = null,
        @SerialName("maskedPan")
        val maskedPan: String? = null,
        @SerialName("activationDate")
        val activationDate: String? = null,
        @SerialName("orderId")
        val orderId: String?,
        @SerialName("validUntil")
        val validUntil: String?,
        @SerialName("goalCard")
        val goalCard: Boolean? = null,
        @SerialName("activationCode")
        val activationCode: String? = null,
        @SerialName("expireDate")
        val expireDate: String? = null,
        @SerialName("authLimitId")
        val authLimitId: String? = null,
        @SerialName("customDomainPart")
        val customDomainPart: String? = null,
        @SerialName("paymentDate")
        val paymentDate: String? = null,
        @SerialName("smsNotificationAvailable")
        val smsNotificationAvailable: Boolean? = null,
        @SerialName("balance")
        val balance: Balance? = null,
        @SerialName("history")
        val history: List<HistoryItem>? = null,
        @SerialName("phone")
        val phone: String? = null,
        @SerialName("cardType")
        val cardType: String? = null,
        @SerialName("smsInfoStatus")
        val smsInfoStatus: String? = null
    )

    @Serializable
    data class Balance(
        @SerialName("availableAmount")
        val availableAmount: Double? = null
    )

    @Serializable
    data class HistoryItem(
        @SerialName("time")
        val time: String? = null,
        @SerialName("amount")
        val amount: Double? = null,
        @SerialName("locationName")
        val locationName: List<String>? = null,
        @SerialName("trnType")
        val trnType: Int? = null,
        @SerialName("mcc")
        val mcc: String? = null,
        @SerialName("currency")
        val currency: String? = null,
        @SerialName("merchantId")
        val merchantId: String? = null,
        @SerialName("reversal")
        val reversal: Boolean? = null,
        @SerialName("posRechargeReceipt")
        val posRechargeReceipt: String? = null
    )
}
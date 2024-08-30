package net.palmut.fmscardbalance.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.get
import io.ktor.http.encodeURLPath
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.random.Random

interface BalanceRepository {
    val balance: MutableStateFlow<MutableList<CardModel>>

    @Serializable
    data class Response(
        @SerialName("status") val status: String? = null,
        @SerialName("messages") val messages: List<Message>? = null,
        @SerialName("net/palmut/fmscardbalance/data") val data: Data? = null
    )

    @Serializable
    data class Message(
        @SerialName("type") val type: String? = null,
        @SerialName("context") val context: String? = null,
        @SerialName("code") val code: String? = null,
        @SerialName("path") val path: String? = null
    )

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

    @Serializable
    data class Balance(
        @SerialName("availableAmount") val availableAmount: Double? = null
    )

    @Serializable
    data class HistoryItem(
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

    val getBalance: suspend (phone: String, pan: String) -> Response

    val addCard: (cardModel: CardModel) -> Unit

    val removeCard: (cardModel: CardModel) -> Unit
}

class DefaultBalanceRepository(
    private val preferences: SharedPreferences = SharedPreferences.INSTANCE
) : BalanceRepository {

    private val client = HttpClient {
        engine {
            pipelining = true
        }
        install(Logging) {
            logger = Logger.SIMPLE
            level = LogLevel.ALL
        }
        install(ContentNegotiation) {
            json(Json {
                encodeDefaults = true
                ignoreUnknownKeys = true
            })
        }
        install(HttpTimeout) {
            socketTimeoutMillis = 15000
        }
    }

    override val balance = MutableStateFlow(mutableListOf<CardModel>())

    init {
        val phone = preferences.getString("phone") ?: ""
        val balanceListString = preferences.getString(phone) ?: "[]"
        val balanceList = Json.decodeFromString<List<CardModel>>(balanceListString)

        balance.update {
            balanceList.toMutableList()
        }

        if (balanceList.isEmpty() && false) {
            val tempBalanceList = listOf(
                CardModel(
                    title = "Спорт", availableAmount = "0", tail = "0356"
                ),
                CardModel(
                    title = "Еда", availableAmount = "0", tail = "6665"
                ),
                CardModel(title = "Проезд", availableAmount = "0", tail = "7491")
            )
            val tempBalanceListString = Json.encodeToString(tempBalanceList)
            preferences.putString(phone, tempBalanceListString)

            balance.update {
                tempBalanceList.toMutableList()
            }
        }
    }

    override val getBalance: suspend (phone: String, pan: String) -> BalanceRepository.Response = { phone, pan ->
        withContext(Dispatchers.IO) {
            val url = "https://meal.gift-cards.ru/api/1/virtual-cards/$phone/$pan".encodeURLPath()
            val response = client.get(url).body<BalanceRepository.Response>()

            response.data?.let { data ->
                this@DefaultBalanceRepository.balance.value.let {
                    var cardModel = it.find { it.tail == data.maskedPan?.takeLast(4) }!!
                    cardModel = data.map(cardModel.title)

                    val savedBalance = preferences.getString(phone)?.let {
                        Json.decodeFromString<List<CardModel>>(it).toMutableList()
                    } ?: mutableListOf()

                    savedBalance.forEachIndexed { index, item ->
                        item.takeIf { it.tail == cardModel.tail }?.let {
                            savedBalance[index] =
                                it.copy(availableAmount = cardModel.availableAmount)
                        }
                    }

                    preferences.putString(phone, Json.encodeToString(savedBalance))

                    this@DefaultBalanceRepository.balance.update { models ->
                        savedBalance
                    }
                }
            }
            response
        }
    }

    override val addCard: (cardModel: CardModel) -> Unit = { cardModel ->
        val phone = preferences.getString("phone") ?: ""
        val cards = getCards().toMutableList()

        cards.add(cardModel)

        val tempBalanceListString = Json.encodeToString(cards)
        preferences.putString(phone, tempBalanceListString)

        balance.update { cards }
    }

    override val removeCard: (cardModel: CardModel) -> Unit = { cardModel ->
        val phone = preferences.getString("phone") ?: ""
        val cards = getCards().toMutableList()

        val index = cards.indexOfFirst { it.tail == cardModel.tail }
        cards.removeAt(index)

        val tempBalanceListString = Json.encodeToString(cards)
        preferences.putString(phone, tempBalanceListString)

        balance.update { cards }
    }


    private val getCards: () -> List<CardModel> = {
        val phone = preferences.getString("phone") ?: ""
        val balanceListString = preferences.getString(phone) ?: "[]"
        Json.decodeFromString<List<CardModel>>(balanceListString)
    }

    suspend fun fetchBalance(phone: String, pan: String) {
        getBalance(phone, pan)
    }

    val saveCard: () -> Unit = {

    }

    fun getSavedBalance(phone: String): List<CardModel> {
        val balance = preferences.getString(phone)

        return balance?.let {
            Json.decodeFromString<List<CardModel>>(it)
        } ?: emptyList()
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class CardModel(
    @SerialName("title") val title: String,
    @SerialName("availableAmount") val availableAmount: String,
    @SerialName("tail") val tail: String = "****",
) {
    @SerialName("id")
    var id: Int = Random.nextInt(0, 1000)

    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    @SerialName("date")
    val date: String
        get() = getDate()
}

fun BalanceRepository.Data.map(title: String) = CardModel(
    title = title,
    availableAmount = balance?.availableAmount?.toString() ?: "",
    tail = maskedPan?.takeLast(4) ?: ""
)

class PreviewBalanceRepository : BalanceRepository {
    override val balance = MutableStateFlow(
        mutableListOf(
            CardModel(title = "Спорт", availableAmount = "1000"),
            CardModel(title = "Спорт", availableAmount = "1000")
        )
    )

    override val getBalance: suspend (phone: String, pan: String) -> BalanceRepository.Response
        get() = TODO("Not yet implemented")

    override val addCard: (cardModel: CardModel) -> Unit
        get() = TODO("Not yet implemented")


    override val removeCard: (cardModel: CardModel) -> Unit
        get() = TODO("Not yet implemented")
}
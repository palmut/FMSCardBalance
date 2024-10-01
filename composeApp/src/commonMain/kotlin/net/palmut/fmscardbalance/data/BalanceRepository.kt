package net.palmut.fmscardbalance.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.get
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.errors.IOException
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
import net.palmut.fmscardbalance.data.response.DataResponse
import net.palmut.fmscardbalance.data.response.Response
import net.palmut.fmscardbalance.store.entity.CardModel
import kotlin.random.Random

interface BalanceRepository {
    suspend fun getBalance(phone: String, pan: String): Response?

    fun addCard(cardModel: CardModel)

    fun removeCard(cardModel: CardModel)

    fun getCards(): List<CardModel>

    var phone: String
}

private const val PHONE_KEY = "phone"

internal class DefaultBalanceRepository(
    private val preferences: SharedPreferences = SharedPreferences.INSTANCE
) : BalanceRepository {

    private val client = HttpClient {
        defaultRequest {
            url {
                protocol = URLProtocol.HTTPS
                host = "meal.gift-cards.ru"
            }
            contentType(ContentType.Application.Json)
        }
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

    override suspend fun getBalance(phone: String, pan: String): Response? {
        return withContext(Dispatchers.IO) {
            try {
                val url = "/api/1/virtual-cards/$phone/$pan".encodeURLPath()

                val response = client.get(url)

                if (response.status == HttpStatusCode.OK) {
                    response.body<Response>().data?.also { data ->
                        getCards().let {
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
                        }
                    }

                    response.body<Response>()
                } else {
                    null
                }
            } catch (_: IOException) {
                return@withContext null
            }
        }
    }

    override fun addCard(cardModel: CardModel) {
        val phone = preferences.getString(PHONE_KEY) ?: ""
        val cards = getCards().toMutableList()

        cards.add(cardModel)

        val tempBalanceListString = Json.encodeToString(cards)
        preferences.putString(phone, tempBalanceListString)
    }

    override fun removeCard(cardModel: CardModel) {
        val phone = preferences.getString(PHONE_KEY) ?: ""
        val cards = getCards().toMutableList()

        val index = cards.indexOfFirst { it.tail == cardModel.tail }
        cards.removeAt(index)

        val tempBalanceListString = Json.encodeToString(cards)
        preferences.putString(phone, tempBalanceListString)
    }


    override fun getCards(): List<CardModel> {
        val phone = preferences.getString(PHONE_KEY) ?: ""
        val balanceListString = preferences.getString(phone) ?: "[]"
        return Json.decodeFromString<List<CardModel>>(balanceListString)
    }

    override var phone: String
        get() = preferences.getString(PHONE_KEY) ?: ""
        set(value) {
            preferences.putString(PHONE_KEY, value)
        }
}

fun DataResponse.map(title: String) = CardModel(
    title = title,
    availableAmount = balance?.availableAmount?.toString() ?: "",
    tail = maskedPan?.takeLast(4) ?: ""
)

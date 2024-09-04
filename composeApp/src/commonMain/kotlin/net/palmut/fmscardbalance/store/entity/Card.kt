@file:OptIn(ExperimentalSerializationApi::class)

package net.palmut.fmscardbalance.store.entity

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.palmut.fmscardbalance.data.getDate
import net.palmut.fmscardbalance.store.MainStore.Status
import kotlin.random.Random

@Serializable
data class CardModel(
    @SerialName("title") val title: String,
    @SerialName("availableAmount") val availableAmount: String = "",
    @SerialName("tail") val tail: String = "****",
    val status: Status = Status.SUCCESS
) {
    @SerialName("id")
    var id: Int = Random.nextInt(0, 1000)

    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    @SerialName("date")
    var date: String = getDate()
}
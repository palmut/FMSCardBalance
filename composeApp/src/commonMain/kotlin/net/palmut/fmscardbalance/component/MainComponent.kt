package net.palmut.fmscardbalance.component

import com.arkivanov.decompose.value.Value

interface MainComponent {
    val model: Value<Model>

    data class Model(
        val status: Status = Status.DEFAULT,
        val data: List<Card> = emptyList(),
        val phoneState: String = "",
        val buttonEnable: Boolean = true,
        val newCardState: NewCardState = NewCardState()
    )

    enum class Status {
        LOADING, ERROR, LOADED, DEFAULT, SUCCESS;

        operator fun invoke() = name
    }

    /**
     *
     */
    fun goToNewCard()
    /**
     *
     */
    fun addNewCard()
    /**
     *
     */
    fun setPhoneState(input: String)
    /**
     *
     */
    fun getBalance(tail: String)
    /**
     *
     */
    fun onBackPressed()
}

data class Card(
    val title: String,
    val availableAmount: String,
    val tail: String,
    val id: Int,
    val date: String
)

data class NewCardState(
    val label: String = "",
    val tail: String = ""
)

fun Card.map() {}

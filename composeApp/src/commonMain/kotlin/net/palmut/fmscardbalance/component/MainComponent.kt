package net.palmut.fmscardbalance.component

import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import net.palmut.fmscardbalance.RootComponentContext
import net.palmut.fmscardbalance.asValue
import net.palmut.fmscardbalance.component.MainComponent.Status
import net.palmut.fmscardbalance.component.entity.Card
import net.palmut.fmscardbalance.component.entity.NewCardState
import net.palmut.fmscardbalance.data.DefaultBalanceRepository
import net.palmut.fmscardbalance.store.MainStore
import net.palmut.fmscardbalance.store.MainStoreProvider
import net.palmut.fmscardbalance.store.mapper.map

interface MainComponent {
    val model: Value<Model>

    data class Model(
        val data: List<Card> = emptyList(),
        val phoneState: String = "",
        val buttonEnable: Boolean = true,
        val newCardState: NewCardState = NewCardState(),
        val isOnNewCard: Boolean = false
    )

    enum class Status {
        LOADING, ERROR, SUCCESS;

        operator fun invoke() = name
    }

    /**
     * @param show - флаг, что нужно открыть форму добавления новой карты
     */
    fun goToNewCard(show: Boolean)

    /**
     * Добавляет новую карту, которая была добавлена в стор интентом Intent.NewCardState
     */
    fun addNewCard()

    /**
     * Стейт для инпутов формы новой карты. Этот же стейт используется для добавления новой карты
     */
    fun setNewCardState(card: NewCardState)

    /**
     * Стейт для инпута номера телефона
     */
    fun setPhoneInput(input: String)

    /**
     * Обновляет список карт с переданным номером карты
     */
    fun getBalance(tail: String)

    /**
     * Удалить карту из списка
     */
    fun removeCard(card: Card)
}

class DefaultMainComponent(
    private val componentContext: RootComponentContext
) : MainComponent, RootComponentContext by componentContext {

    private val store = instanceKeeper.getStore {
        MainStoreProvider(
            storeFactory = componentContext.storeFactory,
            repository = DefaultBalanceRepository()
        ).provide()
    }

    override val model: Value<MainComponent.Model>
        get() = store.asValue().map {
            MainComponent.Model(
                data = it.data.map { it.map() },
                phoneState = it.phoneState,
                newCardState = it.newCardModel.map(),
                isOnNewCard = it.isOnNewCard
            )
        }

    override fun goToNewCard(show: Boolean) {
        store.accept(MainStore.Intent.OpenNewCard(show))
    }

    override fun addNewCard() {
        goToNewCard(false)
        store.accept(MainStore.Intent.AddCard)
    }

    override fun setPhoneInput(input: String) {
        store.accept(MainStore.Intent.PhoneInput(input))
    }

    override fun setNewCardState(card: NewCardState) {
        store.accept(
            MainStore.Intent.NewCardState(
                label = card.label,
                tail = card.tail
            )
        )
    }

    override fun getBalance(tail: String) {
        store.accept(MainStore.Intent.Balance(tail))
    }

    override fun removeCard(card: Card) {
        store.accept(MainStore.Intent.RemoveCard(card))
    }
}

fun Status.map() =
    when (this) {
        Status.LOADING -> MainStore.Status.LOADING
        Status.ERROR -> MainStore.Status.ERROR
        Status.SUCCESS -> MainStore.Status.SUCCESS
    }

fun MainStore.Status.map() =
    when (this) {
        MainStore.Status.LOADING -> Status.LOADING
        MainStore.Status.ERROR -> Status.ERROR
        MainStore.Status.SUCCESS -> Status.SUCCESS
    }

package net.palmut.fmscardbalance.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.launch
import net.palmut.fmscardbalance.component.entity.Card
import net.palmut.fmscardbalance.component.mapper.map
import net.palmut.fmscardbalance.data.BalanceRepository
import net.palmut.fmscardbalance.store.entity.CardModel
import net.palmut.fmscardbalance.store.entity.NewCardModel
import net.palmut.fmscardbalance.store.mapper.toCardModel

interface MainStore : Store<MainStore.Intent, MainStore.State, Unit> {
    sealed interface Intent {
        data class PhoneInput(val input: String) : Intent
        data class NewCardState(val label: String, val tail: String) : Intent
        data object Cards : Intent
        data class Balance(val tail: String) : Intent
        data class RemoveCard(val card: Card) : Intent
        data class OpenNewCard(val show: Boolean) : Intent
        data object AddCard : Intent
    }

    data class State(
        val data: List<CardModel> = emptyList(),
        val phoneState: String = "",
        val buttonEnable: Boolean = true,
        val newCardModel: NewCardModel = NewCardModel(),
        val isOnNewCard: Boolean = false
    )

    enum class Status {
        LOADING, ERROR, SUCCESS;

        operator fun invoke() = name
    }
}

internal class MainStoreProvider(
    private val storeFactory: StoreFactory,
    private val repository: BalanceRepository
) {
    fun provide(): MainStore =
        object : MainStore, Store<MainStore.Intent, MainStore.State, Unit> by storeFactory.create(
            name = "MainStore",
            bootstrapper = BootstrapperImpl(),
            initialState = MainStore.State(),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl()
        ) {}

    private inner class BootstrapperImpl : CoroutineBootstrapper<MainStore.State>() {
        override fun invoke() {
            val cards = repository.getCards()
            val phone = repository.phone
            dispatch(
                MainStore.State(
                    data = cards,
                    phoneState = phone
                )
            )
        }
    }

    private inner class ExecutorImpl : CoroutineExecutor<
            MainStore.Intent,
            MainStore.State,
            MainStore.State,
            MainStore.State,
            Unit>() {

        override fun executeAction(action: MainStore.State) {
            dispatch(action)
        }

        override fun executeIntent(intent: MainStore.Intent) {
            when (intent) {
                is MainStore.Intent.AddCard -> {
                    val state = state()
                    if (state.newCardModel.label.isNotEmpty() && state.newCardModel.tail.length == 4) {
                        repository.addCard(state.newCardModel.toCardModel())
                        dispatch(state.copy(data = repository.getCards()))
                    }
                }

                is MainStore.Intent.Balance -> {
                    val phone = repository.phone
                    val state = state()
                    val data = state.data.toMutableList()

                    val item = state.data.first { it.tail == intent.tail }
                    val index = state.data.indexOf(item)
                    data[index] = item.copy(status = MainStore.Status.LOADING)

                    dispatch(state.copy(data = data))

                    scope.launch {
                        val response = repository.getBalance(phone, intent.tail)

                        data[index] = item.copy(
                            status = MainStore.Status.SUCCESS,
                            availableAmount = response?.data?.balance?.availableAmount.toString()
                        )

                        dispatch(state.copy(data = data))
                    }
                }

                is MainStore.Intent.Cards -> {
                    val cards = repository.getCards()
                    dispatch(state().copy(data = cards))
                }

                is MainStore.Intent.OpenNewCard -> {
                    dispatch(state().copy(isOnNewCard = intent.show))
                }

                is MainStore.Intent.PhoneInput -> {
                    repository.phone = intent.input
                    dispatch(state().copy(phoneState = intent.input))
                }

                is MainStore.Intent.RemoveCard -> {
                    repository.removeCard(intent.card.map())
                    dispatch(state().copy(data = repository.getCards()))
                }

                is MainStore.Intent.NewCardState -> {
                    val state = state()
                    dispatch(
                        state.copy(
                            newCardModel = state.newCardModel.copy(
                                label = intent.label,
                                tail = intent.tail
                            )
                        )
                    )
                }
            }
        }
    }

    private inner class ReducerImpl : Reducer<MainStore.State, MainStore.State> {
        override fun MainStore.State.reduce(msg: MainStore.State): MainStore.State {
            return msg
        }
    }
}
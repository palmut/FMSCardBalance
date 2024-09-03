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

interface MainComponent {
    val model: Value<Model>

    data class Model(
        val status: Status = Status.LOADED,
        val data: List<Card> = emptyList(),
        val phoneState: String = "",
        val buttonEnable: Boolean = true,
        val newCardState: NewCardState = NewCardState()
    )

    enum class Status {
        LOADING, ERROR, LOADED, SUCCESS;

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
    fun removeCard()
    /**
     *
     */
    fun onBackPressed()
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
                status = it.status.map()
            )
        }

    override fun goToNewCard() {
        TODO("Not yet implemented")
    }

    override fun addNewCard() {
        TODO("Not yet implemented")
    }

    override fun setPhoneState(input: String) {
        TODO("Not yet implemented")
    }

    override fun getBalance(tail: String) {
        TODO("Not yet implemented")
    }

    override fun removeCard() {
        TODO("Not yet implemented")
    }

    override fun onBackPressed() {
        TODO("Not yet implemented")
    }
}

fun Status.map() =
    when (this) {
        Status.LOADING -> MainStore.Status.LOADING
        Status.ERROR -> MainStore.Status.ERROR
        Status.LOADED -> MainStore.Status.LOADED
        Status.SUCCESS -> MainStore.Status.SUCCESS
    }

fun MainStore.Status.map() =
    when (this) {
        MainStore.Status.LOADING -> Status.LOADING
        MainStore.Status.ERROR -> Status.ERROR
        MainStore.Status.LOADED -> Status.LOADED
        MainStore.Status.SUCCESS -> Status.SUCCESS
    }

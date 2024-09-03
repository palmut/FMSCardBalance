package net.palmut.fmscardbalance.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import net.palmut.fmscardbalance.data.BalanceRepository

interface MainStore: Store<MainStore.Intent, MainStore.State, Unit> {
    sealed interface Intent {

    }

    data class State(
        val status: Status = Status.LOADED,
    )

    enum class Status {
        LOADING, ERROR, LOADED, SUCCESS;

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
            dispatch(MainStore.State(status = MainStore.Status.LOADED))
        }
    }

    private inner class ExecutorImpl : CoroutineExecutor<
            MainStore.Intent,
            MainStore.State,
            MainStore.State,
            MainStore.State,
            Unit>() {
        override fun executeAction(action: MainStore.State) {
            super.executeAction(action)
        }

        override fun executeIntent(intent: MainStore.Intent) {
            super.executeIntent(intent)
        }
    }

    private inner class ReducerImpl : Reducer<MainStore.State, MainStore.State> {
        override fun MainStore.State.reduce(msg: MainStore.State): MainStore.State {
            return msg
        }
    }
}
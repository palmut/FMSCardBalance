package net.palmut.fmscardbalance

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ComponentContextFactory
import com.arkivanov.decompose.GenericComponentContext
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import com.arkivanov.essenty.instancekeeper.InstanceKeeperOwner
import com.arkivanov.essenty.lifecycle.LifecycleOwner
import com.arkivanov.essenty.statekeeper.StateKeeperOwner
import com.arkivanov.mvikotlin.core.store.StoreFactory

interface RootComponentContext : GenericComponentContext<RootComponentContext> {
    val storeFactory: StoreFactory
}

class DefaultRootComponentContext(
    componentContext: ComponentContext,
    override val storeFactory: StoreFactory
) :
    RootComponentContext,
    LifecycleOwner by componentContext,
    StateKeeperOwner by componentContext,
    InstanceKeeperOwner by componentContext,
    BackHandlerOwner by componentContext {


    override val componentContextFactory: ComponentContextFactory<RootComponentContext> =
        ComponentContextFactory { lifecycle, stateKeeper, instanceKeeper, backHandler ->
            val context = componentContext.componentContextFactory(
                lifecycle,
                stateKeeper,
                instanceKeeper,
                backHandler
            )
            DefaultRootComponentContext(
                componentContext = context,
                storeFactory = storeFactory
            )
        }
}
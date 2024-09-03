package net.palmut.fmscardbalance.component

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import net.palmut.fmscardbalance.RootComponentContext

interface ApplicationComponent {
    val childStack: Value<ChildStack<*, Child>>

    sealed class Child {
        class Main(val component: MainComponent) : Child()
    }

    fun onBackPressed()
}

class DefaultApplicationComponent(
    private val componentContext: RootComponentContext
) : ApplicationComponent, RootComponentContext by componentContext {
    private val navigation = StackNavigation<Config>()

    override val childStack: Value<ChildStack<*, ApplicationComponent.Child>> = childStack(
        key = "ApplicationComponent",
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = Config.Main,
        handleBackButton = false,
        childFactory = ::createChild,
    )

    private fun createChild(
        config: Config,
        componentContext: RootComponentContext,
    ): ApplicationComponent.Child =
        when (config) {
            Config.Main -> ApplicationComponent.Child.Main(
                component = DefaultMainComponent(
                    componentContext = componentContext
                )
            )
        }

    override fun onBackPressed() {
        navigation.pop()
    }

    @Serializable
    private sealed interface Config {
        @Serializable
        data object Main : Config
    }
}
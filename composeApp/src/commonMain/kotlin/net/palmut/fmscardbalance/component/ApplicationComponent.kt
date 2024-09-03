package net.palmut.fmscardbalance.component

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value

interface ApplicationComponent {
    val childStack: Value<ChildStack<*, Child>>

    sealed class Child {
        class Main(val component: MainComponent) : Child()
    }

    fun onBackPressed()
}
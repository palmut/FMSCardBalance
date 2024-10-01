package net.palmut.fmscardbalance

import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.mvikotlin.logging.store.LoggingStoreFactory
import com.arkivanov.mvikotlin.timetravel.store.TimeTravelStoreFactory
import net.palmut.fmscardbalance.component.DefaultApplicationComponent
import net.palmut.fmscardbalance.ui.AppTheme
import net.palmut.fmscardbalance.ui.MainScreen

fun main() = application {
    Window(
        resizable = false,
        state = WindowState(
            size = DpSize(360.dp, 640.dp),
            position = WindowPosition(Alignment.Center),
        ),
        onCloseRequest = ::exitApplication,
        title = "СУП Баланс",
    ) {
        val applicationComponent = DefaultApplicationComponent(
            componentContext = DefaultRootComponentContext(
                componentContext = DefaultComponentContext(
                    lifecycle = LifecycleRegistry()
                ),
                storeFactory = LoggingStoreFactory(TimeTravelStoreFactory())
            )
        )

        AppTheme {
            MainScreen(component = applicationComponent)
        }
    }
}

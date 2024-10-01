import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.mvikotlin.logging.store.LoggingStoreFactory
import com.arkivanov.mvikotlin.timetravel.store.TimeTravelStoreFactory
import net.palmut.fmscardbalance.DefaultRootComponentContext
import net.palmut.fmscardbalance.component.DefaultApplicationComponent
import net.palmut.fmscardbalance.ui.AppTheme
import net.palmut.fmscardbalance.ui.MainScreen

fun MainViewController() = ComposeUIViewController {
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
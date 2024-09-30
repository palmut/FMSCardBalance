package net.palmut.fmscardbalance

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import com.arkivanov.decompose.defaultComponentContext
import com.arkivanov.mvikotlin.logging.store.LoggingStoreFactory
import com.arkivanov.mvikotlin.timetravel.store.TimeTravelStoreFactory
import net.palmut.fmscardbalance.component.DefaultApplicationComponent
import net.palmut.fmscardbalance.data.SharedPreferences
import net.palmut.fmscardbalance.ui.AppTheme
import net.palmut.fmscardbalance.ui.MainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SharedPreferences.create(this.applicationContext)

        val applicationComponent = DefaultApplicationComponent(
            componentContext = DefaultRootComponentContext(
                componentContext = defaultComponentContext(),
                storeFactory = LoggingStoreFactory(TimeTravelStoreFactory())
            )
        )

        setContent {
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.light(
                    Color.TRANSPARENT,
                    MaterialTheme.colorScheme.primary.toArgb()
                ),
                navigationBarStyle = SystemBarStyle.light(
                    Color.TRANSPARENT,
                    MaterialTheme.colorScheme.primary.toArgb()
                )
            )

            AppTheme {
                MainScreen(applicationComponent)
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
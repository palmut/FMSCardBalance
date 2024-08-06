package net.palmut.fmscardbalance

import App
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
import data.Preferences
import ui.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Preferences.create(this.applicationContext)

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
//            App()
            AppTheme {
                CardListScreen()
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
package net.palmut.fmscardbalance

import ui.CardListScreen
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import ui.AppTheme

fun main() = application {
    Window(
        state = WindowState(size = DpSize(360.dp, 480.dp)),
        onCloseRequest = ::exitApplication,
        title = "Desktop",
    ) {
        AppTheme {
            CardListScreen()
        }
    }
}
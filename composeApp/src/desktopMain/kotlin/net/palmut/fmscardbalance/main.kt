package net.palmut.fmscardbalance

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import net.palmut.fmscardbalance.ui.AppTheme
import net.palmut.fmscardbalance.ui.CardListScreen

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
        AppTheme {
            CardListScreen()
        }
    }
}

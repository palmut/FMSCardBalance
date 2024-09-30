package net.palmut.fmscardbalance.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.text.platform.Font
import fmscardbalance.composeapp.generated.resources.Res
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.ExperimentalResourceApi

private val cache: MutableMap<String, Font> = mutableMapOf()
@OptIn(ExperimentalResourceApi::class)
@Composable
actual fun font(
    name: String,
    res: String,
    weight: FontWeight,
    style: FontStyle
): Font {
    return cache.getOrPut(res) {
        val byteArray = runBlocking {
            Res.readBytes("font/$res.ttf")
        }
        Font(res, byteArray, weight, style)
    }
}
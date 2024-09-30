package net.palmut.fmscardbalance.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            primary = Color(0xFF74AD8F),
            secondary = Color(0xFFDCDAB5),
            tertiary = Color(0xFF1E3848),
            background = Color(0xFFDCDAB5),
            surface = Color(0xFFDCDAB5),
        ),
        typography = Typography,
        content = content
    )
}
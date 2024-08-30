package net.palmut.fmscardbalance.ui

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

@Composable
expect fun font(
    name: String,
    res: String,
    weight: FontWeight,
    style: FontStyle
): Font

val AppFont: FontFamily
    @Composable get() = FontFamily(
        font(
            name = "Atom Regular",
            res = "atom_regular",
            weight = FontWeight.Normal,
            style = FontStyle.Normal
        ),
        font(
            name = "Atom Regular",
            res = "atom_regular",
            weight = FontWeight.Normal,
            style = FontStyle.Italic
        ),
        font(
            name = "Atom Regular",
            res = "atom_medium",
            weight = FontWeight.Medium,
            style = FontStyle.Normal
        ),
        font(
            name = "Atom Regular",
            res = "atom_medium",
            weight = FontWeight.Medium,
            style = FontStyle.Italic
        ),
        font(
            name = "Atom Regular",
            res = "atom_bold",
            weight = FontWeight.Bold,
            style = FontStyle.Normal
        ),
        font(
            name = "Atom Regular",
            res = "atom_bold",
            weight = FontWeight.Bold,
            style = FontStyle.Italic
        )
    )


private val defaultTypography = Typography()
val Typography: Typography
    @Composable get() = Typography(
        displayLarge = defaultTypography.displayLarge.copy(fontFamily = AppFont),
        displayMedium = defaultTypography.displayMedium.copy(fontFamily = AppFont),
        displaySmall = defaultTypography.displaySmall.copy(fontFamily = AppFont),

        headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = AppFont),
        headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = AppFont),
        headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = AppFont),

        titleLarge = defaultTypography.titleLarge.copy(fontFamily = AppFont),
        titleMedium = defaultTypography.titleMedium.copy(fontFamily = AppFont),
        titleSmall = defaultTypography.titleSmall.copy(fontFamily = AppFont),

        bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = AppFont),
        bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = AppFont),
        bodySmall = defaultTypography.bodySmall.copy(fontFamily = AppFont),

        labelLarge = defaultTypography.labelLarge.copy(fontFamily = AppFont),
        labelMedium = defaultTypography.labelMedium.copy(fontFamily = AppFont),
        labelSmall = defaultTypography.labelSmall.copy(fontFamily = AppFont)
    )
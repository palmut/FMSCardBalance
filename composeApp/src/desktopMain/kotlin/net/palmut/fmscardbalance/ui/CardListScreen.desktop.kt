package net.palmut.fmscardbalance.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.NativePaint
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.skia.FilterBlurMode
import org.jetbrains.skia.MaskFilter

actual fun Modifier.advancedShadow(
    color: Color,
    alpha: Float,
    cornersRadius: Dp,
    offsetY: Dp,
    offsetX: Dp,
    blurRadius: Float
) = drawBehind {
    val shadowColor = color.copy(alpha = alpha).toArgb()
    val transparentColor = color.copy(alpha = 0f).toArgb()

    drawIntoCanvas {
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.color = shadowColor

        if (blurRadius != 0f) {
            frameworkPaint.setMaskFilter(blurRadius)
        }

        it.drawRoundRect(
            0f,
            0f,
            this.size.width + offsetX.toPx(),
            this.size.height + offsetY.toPx(),
            cornersRadius.toPx(),
            cornersRadius.toPx(),
            paint
        )
    }
}

fun NativePaint.setMaskFilter(blurRadius: Float) {
    this.maskFilter = MaskFilter.makeBlur(FilterBlurMode.NORMAL, blurRadius / 2, true)
}

@Preview
@Composable
private fun CardListScreenPreview() {
    AppTheme {
//        CardListScreen(repository = PreviewBalanceRepository())
    }
}

package net.palmut.fmscardbalance.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.CoroutineScope
import net.palmut.fmscardbalance.component.MainComponent
import net.palmut.fmscardbalance.component.entity.Card
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.skia.FilterBlurMode
import org.jetbrains.skia.MaskFilter
import java.awt.Component
import javax.swing.JButton

private const val CARD_ASPECT_RATIO = 1.58f
const val CARD_WIDTH = 0.8f
private const val SEMITRANSPARENT = 0.9f
private const val TRANSPARENT = 0f

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CardListScreen(component: MainComponent) {

    val state by component.model.subscribeAsState()

    val configuration = LocalWindowInfo.current

    val screenWidth = configuration.containerSize.width.dp
    val screenHeight = configuration.containerSize.height.dp

    val alphaTarget = derivedStateOf {
        if (state.isOnNewCard) {
            SEMITRANSPARENT
        } else {
            TRANSPARENT
        }
    }
    val alpha = animateFloatAsState(targetValue = alphaTarget.value, label = "alpha")

    Box {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFFFF9)),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(CARD_WIDTH).fillMaxHeight().align(Alignment.Center),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(vertical = 80.dp)
            ) {
                items(state.data) { item ->
                    val removeAction = remember { mutableStateOf(false) }
                    Card(
                        colors = CardDefaults.cardColors(),
                        border = BorderStroke(3.dp, Color.Black),
                        modifier = Modifier
                            .width(screenWidth - 32.dp)
                            .aspectRatio(CARD_ASPECT_RATIO)
                            .advancedShadow()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onLongPress = {
                                        removeAction.value = removeAction.value.not()
                                    }
                                )
                            },
                        shape = MaterialTheme.shapes.small.copy(CornerSize(10.dp))
                    ) {
                        val loading = remember { mutableStateOf(false) }
                        Box {
                            CardContent(item, refreshEnabled = loading) {
                                component.getBalance(it)
                            }
                            this@Card.AnimatedVisibility(
                                modifier = Modifier.align(Alignment.TopEnd),
                                visible = removeAction.value,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .padding(top = 16.dp)
                                        .padding(end = 16.dp)
                                        .size(32.dp)
                                        .background(color = CardDefaults.cardColors().containerColor)
                                        .align(Alignment.TopEnd)
                                        .clip(CircleShape)
                                        .clickable {
                                            component.removeCard(item)
                                        }
                                ) {
                                    Icon(
                                        modifier = Modifier.size(24.dp),
                                        painter = painterResource("close.png"),
                                        contentDescription = ""
                                    )
                                }
                            }

                            if (item.status == MainComponent.Status.LOADING) {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.Center),
                                    strokeCap = StrokeCap.Round,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }
            }

            if (state.data.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Нет сохранённых карт".lowercase(),
                        fontSize = 25.sp,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            InputField(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .systemBarsPadding()
                    .padding(top = 16.dp)
                    .advancedShadow(cornersRadius = 16.dp),
                type = InputFieldType.PHONE,
                state = state.phoneState
            ) {
                component.setPhoneInput(it)
            }

            TextButton(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp)
                    .advancedShadow(cornersRadius = 16.dp)
                    .height(48.dp)
                    .fillMaxWidth(CARD_WIDTH),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.textButtonColors(containerColor = Color(0xFF138DFF)),
                border = BorderStroke(2.dp, Color.Black),
                onClick = {
                    component.goToNewCard(true)
                }
            ) {
                Text(text = "Добавить карту".lowercase(), fontSize = 25.sp, color = Color.White)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha.value))
        )

        AnimatedVisibility(
            modifier = Modifier
                .align(Alignment.Center).fillMaxWidth(CARD_WIDTH),
            visible = state.isOnNewCard,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
            ) {
                Card(
                    border = BorderStroke(3.dp, Color.Black),
                    modifier = Modifier
                        .width(screenWidth)
                        .padding(bottom = 16.dp)
                        .advancedShadow()
                        .aspectRatio(CARD_ASPECT_RATIO),
                    shape = MaterialTheme.shapes.small.copy(CornerSize(10.dp))
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column {
                            Text(text = "Название карты".lowercase())
                            InputField(
                                modifier = Modifier.advancedShadow(cornersRadius = 16.dp),
                                state = state.newCardState.label,
                                type = InputFieldType.TEXT
                            ) {
                                component.setNewCardState(state.newCardState.copy(label = it))
                            }
                        }
                        Column {
                            Text(text = "4 цифры карты".lowercase())
                            InputField(
                                modifier = Modifier.advancedShadow(cornersRadius = 16.dp),
                                state = state.newCardState.tail,
                                type = InputFieldType.NUMBER
                            ) {
                                component.setNewCardState(state.newCardState.copy(tail = it))
                            }
                        }
                    }
                }

                TextButton(
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .advancedShadow(cornersRadius = 16.dp)
                        .height(48.dp)
                        .width(screenWidth)
                        .zIndex(-5f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.textButtonColors(containerColor = Color(0xFF138DFF)),
                    border = BorderStroke(2.dp, Color.Black),
                    onClick = {
                        component.addNewCard()
//                            repository.addCard(newModel.value)
                    }
                ) {
                    Text(text = "Добавить".lowercase(), fontSize = 25.sp, color = Color.White)
                }

                TextButton(
                    modifier = Modifier
                        .advancedShadow(cornersRadius = 16.dp)
                        .height(48.dp)
                        .width(screenWidth)
                        .zIndex(-5f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.textButtonColors(containerColor = Color(0xFF138DFF)),
                    border = BorderStroke(2.dp, Color.Black),
                    onClick = {
                        component.goToNewCard(false)
                    }
                ) {
                    Text(text = "Отмена".lowercase(), fontSize = 25.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun AnimatedCard(
    block: @Composable (
        border: State<Dp>,
        offsetY: State<Dp>,
        zIndex: State<Float>,
        scale: State<Float>,
        draggableState: DraggableState,
        onDragStopped: CoroutineScope.(velocity: Float) -> Unit
    ) -> Unit
) {
    val density = LocalDensity.current

    var newOrder: Int = 0

    val zIndex = remember { mutableFloatStateOf(0f) }
    val scaleTarget = remember { mutableFloatStateOf(1f) }
    val offsetYTarget = remember { mutableStateOf(0.dp) }
    val borderTarget = remember { mutableStateOf(3.dp) }

    val scale = animateFloatAsState(targetValue = scaleTarget.value, label = "scale") {
        if (it == 1f) {
            newOrder -= 1
        }
    }

    val border = animateDpAsState(targetValue = borderTarget.value, label = "elevation") {
        if (it == 1.dp) {

        }
    }

    val offsetY = animateDpAsState(targetValue = offsetYTarget.value, label = "offsetY") {
        if (it == (-150).dp) {
            zIndex.value -= 1.1f
            offsetYTarget.value = 0.dp

        }
    }

    val draggableState = rememberDraggableState {
        val it = with(density) { it.toDp() }
        offsetYTarget.value += it
    }

    val onDragStopped: CoroutineScope.(velocity: Float) -> Unit = {
        if (offsetYTarget.value < (-100).dp) {
            offsetYTarget.value = (-150).dp
        } else {
            offsetYTarget.value = 0.dp
        }
    }

    block(border, offsetY, zIndex, scale, draggableState, onDragStopped)
}

@Composable
fun CardContent(
    model: Card,
    refreshEnabled: MutableState<Boolean>,
    refresh: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .padding(top = 8.dp)
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = model.title,
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraLight
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(8.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable {
                        if (refreshEnabled.value.not()) {
                            refresh(model.tail)
                        }
                    }
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource("sync.png"),
                    contentDescription = ""
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
        ) {

            Text(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .offset(y = 8.dp),
                text = "баланс",
                fontSize = 15.6.sp
            )

            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .padding(bottom = 8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${model.availableAmount}₽",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraLight
                )
                Text(
                    text = "Обновлено\n${model.date}",
                    fontSize = 15.6.sp,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

fun Modifier.advancedShadow(
    color: Color = Color.Black,
    alpha: Float = 0.9f,
    cornersRadius: Dp = 10.dp,
    offsetY: Dp = 3.dp,
    offsetX: Dp = 3.dp,
    blurRadius: Float = 0f,
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
fun CardListScreenPreview() {
    AppTheme {
//        CardListScreen(repository = PreviewBalanceRepository())
    }
}

fun actionButton(
    text: String,
    action: () -> Unit
): JButton {
    val button = JButton(text)
    button.alignmentX = Component.CENTER_ALIGNMENT
    button.addActionListener { action() }

    return button
}

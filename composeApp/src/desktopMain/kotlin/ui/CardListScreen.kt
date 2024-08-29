package ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import data.BalanceRepository
import data.CardModel
import data.DefaultBalanceRepository
import data.PreviewBalanceRepository
import data.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.skia.FilterBlurMode
import org.jetbrains.skia.MaskFilter

private const val CARD_ASPECT_RATIO = 1.58f
const val CARD_WIDTH = 0.8f
private const val SEMITRANSPARENT = 0.9f
private const val NOT_TRANSPARENT = 0f

@Composable
fun CardListScreen(repository: BalanceRepository = DefaultBalanceRepository()) {

    val state by repository.balance.collectAsState()
//    val state by  remember { mutableStateOf<List<CardModel>>(emptyList()) }

    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    var editing by remember { mutableStateOf(false) }
    var cardTitle by remember { mutableStateOf("") }
    var cardPan by remember { mutableStateOf("") }

    val preferences = SharedPreferences.INSTANCE
    val phone = remember { mutableStateOf(preferences.getString("phone") ?: "") }

    val alphaTarget = remember { mutableFloatStateOf(NOT_TRANSPARENT) }
    val alpha = animateFloatAsState(targetValue = alphaTarget.floatValue, label = "alpha")

    val elevationTarget = remember { mutableStateOf(0.dp) }
    val elevation = animateDpAsState(targetValue = elevationTarget.value, label = "elevation")

    val newModel = remember {
        mutableStateOf(
            CardModel(
                title = "",
                availableAmount = "0",
                tail = ""
            )
        )
    }

    var focusedCardZIndex by remember { mutableFloatStateOf(0f) }

    Box {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFFFF9)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            InputField(
                modifier = Modifier
                    .systemBarsPadding()
                    .padding(top = 16.dp)
                    .advancedShadow(cornersRadius = 16.dp),
                type = InputFieldType.PHONE,
                state = phone.value
            ) {
                phone.value = it
                preferences.putString("phone", it)
            }

            Box(
                modifier = Modifier
//                    .verticalScroll(rememberScrollState())
            ) {
                val shape = MaterialTheme.shapes.small.copy(CornerSize(10.dp))
                MaskFilter
                repeat(state.size) {
                    AnimatedCard { border, offsetY, zIndex, scale, draggableState, onDragStopped ->
                        var removeAction = remember { mutableStateOf(false) }
                        Card(
                            colors = CardDefaults.cardColors(),
                            border = BorderStroke(border.value, Color.Black),
                            modifier = Modifier
                                .fillMaxWidth(CARD_WIDTH)
                                .aspectRatio(CARD_ASPECT_RATIO)
                                .scale(scale.value)
                                .offset(y = offsetY.value)
                                .shadow(elevation = elevation.value)
                                .advancedShadow()
                                .zIndex(zIndex.value)
                                .draggable(
                                    onDragStopped = onDragStopped,
                                    state = draggableState,
                                    orientation = Orientation.Vertical
                                )
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onLongPress = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            removeAction.value = removeAction.value.not()
                                        }
                                    )
                                },
                            shape = MaterialTheme.shapes.small.copy(CornerSize(10.dp))
                        ) {
                            val loading = remember { mutableStateOf(false) }
                            Box {
                                CardContent(state[it], refreshEnabled = loading) {
                                    scope.launch {
                                        loading.value = true
                                        try {
                                            repository.getBalance(phone.value, it)
                                        } catch (e: Exception) {

                                        } finally {
                                            loading.value = false
                                        }
                                    }
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
                                                repository.removeCard(state[it])
                                            }
                                    ) {
                                        Icon(
                                            modifier = Modifier.size(24.dp),
                                            painter = painterResource("close.png"),
                                            contentDescription = ""
                                        )
                                    }
                                }

                                if (loading.value) {
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
            }

            TextButton(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp)
                    .advancedShadow(cornersRadius = 16.dp)
                    .height(48.dp)
                    .fillMaxWidth(CARD_WIDTH)
                    .zIndex(-5f),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.textButtonColors(containerColor = Color(0xFF138DFF)),
                border = BorderStroke(2.dp, Color.Black),
                onClick = {
                    editing = true
                    alphaTarget.floatValue = SEMITRANSPARENT
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
                .align(Alignment.Center),
            visible = editing,
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
                        .fillMaxWidth(CARD_WIDTH)
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
                                state = cardTitle,
                                type = InputFieldType.TEXT
                            ) {
                                cardTitle = it
                                newModel.value = newModel.value.copy(title = it)
                            }
                        }
                        Column {
                            Text(text = "4 цифры карты".lowercase())
                            InputField(
                                modifier = Modifier.advancedShadow(cornersRadius = 16.dp),
                                state = cardPan,
                                type = InputFieldType.NUMBER
                            ) {
                                cardPan = it
                                newModel.value = newModel.value.copy(tail = it)
                            }
                        }
                    }
                }

                TextButton(
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .advancedShadow(cornersRadius = 16.dp)
                        .height(48.dp)
                        .fillMaxWidth(CARD_WIDTH)
                        .zIndex(-5f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.textButtonColors(containerColor = Color(0xFF138DFF)),
                    border = BorderStroke(2.dp, Color.Black),
                    onClick = {
                        if (newModel.value.title.isNotEmpty() && newModel.value.tail.length == 4) {
                            repository.addCard(newModel.value)
                            editing = false
                            alphaTarget.floatValue = NOT_TRANSPARENT
                        }
                    }
                ) {
                    Text(text = "Добавить".lowercase(), fontSize = 25.sp, color = Color.White)
                }

                TextButton(
                    modifier = Modifier
                        .advancedShadow(cornersRadius = 16.dp)
                        .height(48.dp)
                        .fillMaxWidth(CARD_WIDTH)
                        .zIndex(-5f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.textButtonColors(containerColor = Color(0xFF138DFF)),
                    border = BorderStroke(2.dp, Color.Black),
                    onClick = {
                        editing = false
                        alphaTarget.floatValue = NOT_TRANSPARENT
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
    model: CardModel,
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
        CardListScreen(repository = PreviewBalanceRepository())
    }
}

@file:OptIn(InternalResourceApi::class)

package net.palmut.fmscardbalance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import data.BalanceRepository
import data.CardModel
import data.DefaultBalanceRepository
import data.Preferences
import data.PreviewBalanceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.InternalResourceApi
import ui.AppTheme

private const val CARD_ASPECT_RATIO = 1.58f
const val CARD_WIDTH = 0.8f
private val BLURED = 12.dp
private val UNBLURED = 0.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CardListScreen(repository: BalanceRepository = DefaultBalanceRepository()) {

    val state by repository.balance.collectAsState()

    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    var editing by remember { mutableStateOf(false) }
    var cardTitle by remember { mutableStateOf("") }
    var cardPan by remember { mutableStateOf("") }

    val preferences = Preferences.INSTANCE
    val phone = remember { mutableStateOf(preferences.getString("phone") ?: "") }

    val blurTarget = remember { mutableStateOf(UNBLURED) }
    val blur = animateDpAsState(targetValue = blurTarget.value, label = "blur")

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
                .background(Color(0xFFFFFFF9))
                .blur(blur.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            InputField(
                modifier = Modifier.systemBarsPadding(),
                type = InputFieldType.PHONE,
                state = phone.value
            ) {
                phone.value = it
                preferences.putString("phone", it)
            }

            Box(modifier = Modifier) {
                val shape = MaterialTheme.shapes.small.copy(CornerSize(10.dp))

                repeat(state.size) {
                    AnimatedCard { border, offsetY, zIndex, scale, draggableState, onDragStopped ->
                        Card(
                            border = BorderStroke(border.value, Color.Black),
                            modifier = Modifier
                                .fillMaxWidth(CARD_WIDTH)
                                .aspectRatio(CARD_ASPECT_RATIO)
                                .scale(scale.value)
                                .offset(y = offsetY.value)
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
                                        }
                                    )
                                },
                            shape = MaterialTheme.shapes.small.copy(CornerSize(10.dp))
                        ) {
                            val loading = remember { mutableStateOf(false) }
                            Box(contentAlignment = Alignment.Center) {
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
                                if (loading.value) {
                                    CircularProgressIndicator(
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
                    .height(64.dp)
                    .fillMaxWidth(CARD_WIDTH)
                    .padding(bottom = 16.dp)
                    .zIndex(-5f),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.textButtonColors(containerColor = Color(0xFF138DFF)),
                border = BorderStroke(2.dp, Color.Black),
                onClick = {
                    editing = true
                    blurTarget.value = BLURED
                }
            ) {
                Text(text = "Добавить карту".lowercase(), fontSize = 25.sp, color = Color.White)
            }
        }
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
                            InputField(state = cardTitle, type = InputFieldType.TEXT) {
                                cardTitle = it
                                newModel.value = newModel.value.copy(title = it)
                            }
                        }
                        Column {
                            Text(text = "4 цифры карты".lowercase())
                            InputField(state = cardPan, type = InputFieldType.NUMBER) {
                                cardPan = it
                                newModel.value = newModel.value.copy(tail = it)
                            }
                        }
                    }
                }

                TextButton(
                    modifier = Modifier
                        .height(64.dp)
                        .padding(top = 16.dp)
                        .fillMaxWidth(CARD_WIDTH)
                        .zIndex(-5f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.textButtonColors(containerColor = Color(0xFF138DFF)),
                    border = BorderStroke(2.dp, Color.Black),
                    onClick = {
                        if (newModel.value.title.isNotEmpty() && newModel.value.tail.length == 4) {
                            repository.addCard(newModel.value)
                            editing = false
                            blurTarget.value = UNBLURED
                        }
                    }
                ) {
                    Text(text = "Добавить".lowercase(), fontSize = 25.sp, color = Color.White)
                }

                TextButton(
                    modifier = Modifier
                        .height(64.dp)
                        .padding(top = 16.dp)
                        .fillMaxWidth(CARD_WIDTH)
                        .zIndex(-5f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.textButtonColors(containerColor = Color(0xFF138DFF)),
                    border = BorderStroke(2.dp, Color.Black),
                    onClick = {
                        editing = false
                        blurTarget.value = UNBLURED
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
                    painter = painterResource(id = R.drawable.ic_refresh),
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
                    text = "Добавлено\n${model.date}",
                    fontSize = 15.6.sp,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Preview
@Composable
fun CardListScreenPreview() {
    AppTheme {
        CardListScreen(repository = PreviewBalanceRepository())
    }
}
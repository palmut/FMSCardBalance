@file:OptIn(InternalResourceApi::class)

package net.palmut.fmscardbalance

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.gestures.Orientation
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import data.BalanceRepository
import data.CardModel
import data.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.InternalResourceApi
import ui.AppTheme

private const val CARD_ASPECT_RATIO = 1.58f
private const val CARD_WIDTH = 0.8f

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CardListScreen(repository: BalanceRepository = BalanceRepository()) {

    val state by repository.balance.collectAsState()

    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val preferences = Preferences.INSTANCE
    var phone by remember { mutableStateOf(preferences.getString("phone") ?: "") }

//    val balance = repository.getSavedBalance(phone)

    val cardIndex = remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFFF9)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        BasicTextField(
            modifier = Modifier
                .systemBarsPadding()
                .height(64.dp)
                .fillMaxWidth(CARD_WIDTH)
                .padding(bottom = 16.dp)
                .zIndex(-5f)
                .border(
                    border = BorderStroke(2.dp, Color.Black),
                    shape = RoundedCornerShape(16.dp)
                ),
            singleLine = true,
            textStyle = TextStyle(
                color = Color(0xFF138DFF),
                fontSize = 25.sp,
                textAlign = TextAlign.Center
            ),
            value = phone,
            onValueChange = {
                phone = it
                preferences.putString("phone", it)
            },
            keyboardActions = KeyboardActions(onDone = {
                keyboardController?.hide()
                focusManager.clearFocus()
            }),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                autoCorrect = false
            ),
            cursorBrush = SolidColor(Color(0xFF138DFF)),
            decorationBox = { innerTextField ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
//                    Text(text = "+7 999 111-22-33".lowercase(), fontSize = 25.sp, color = Color(0xFF138DFF))
                    innerTextField()
                }
            }
        )

        Box(modifier = Modifier) {
            val shape = MaterialTheme.shapes.small.copy(CornerSize(10.dp))

            repeat(state.size) {
                AnimatedCard(
                    state = cardIndex,
                    order = 1,
                    upperOffset = 0.dp,
                    lowerOffset = 0.dp,
                    upperScale = 1f,
                    lowerScale = 1f,
                ) { border, offsetY, zIndex, scale, draggableState, onDragStopped ->
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
                            ),
                        shape = MaterialTheme.shapes.small.copy(CornerSize(10.dp))
                    ) {
                        val loading = remember { mutableStateOf(false) }
                        Box(contentAlignment = Alignment.Center) {
                            CardContent(state[it], refreshEnabled = loading) {
                                scope.launch {
                                    loading.value = true
                                    try {
                                        repository.getBalance(phone, it)
                                    } catch (e: Exception) {

                                    } finally {
                                        loading.value = false
                                    }
                                }
                            }
                            if (loading.value) {
                                CircularProgressIndicator(strokeCap = StrokeCap.Round, color = Color(0xFF138DFF))
                            }
                        }
                    }
                }
            }
        }

        TextButton(
            enabled = false,
            modifier = Modifier
                .navigationBarsPadding()
                .height(64.dp)
                .fillMaxWidth(CARD_WIDTH)
                .padding(bottom = 16.dp)
                .zIndex(-5f),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.textButtonColors(backgroundColor = Color(0xFF138DFF)),
            border = BorderStroke(2.dp, Color.Black),
            onClick = { /*TODO*/ })
        {
            Text(text = "Добавить карту".lowercase(), fontSize = 25.sp, color = Color.White)
        }
    }
}

@Composable
fun AnimatedCard(
    state: MutableIntState,
    order: Int,
    upperOffset: Dp,
    lowerOffset: Dp,
    upperScale: Float,
    lowerScale: Float,
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

    var newOrder: Int = order

    val zIndex = remember { mutableFloatStateOf(newOrder.toFloat()) }
    val scaleTarget = remember { mutableFloatStateOf(upperScale) }
    val offsetYTarget = remember { mutableStateOf(upperOffset) }
    val borderTarget = remember { mutableStateOf(3.dp) }

    val scale = animateFloatAsState(targetValue = scaleTarget.value, label = "scale") {
        if (it == lowerScale) {
            newOrder -= 1
        }
    }

    val border = animateDpAsState(targetValue = borderTarget.value, label = "elevation") {
        if (it == 1.dp) {

        }
    }

    val offsetY = animateDpAsState(targetValue = offsetYTarget.value, label = "offsetY") {
        if (it == (-150).dp) {
//            borderTarget.value = 1.dp
            zIndex.value -= 1.1f
//            scaleTarget.value = 0.97f
            offsetYTarget.value = 0.dp

        }

        if (it == lowerOffset) {
//            borderTarget.value += 1.dp
//            scaleTarget.value += 0.02f
//            offsetYTarget.value += 10.dp
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

    LaunchedEffect(state) {
        println("moved")
//        scaleTarget.value += 0.02f
//        offsetYTarget.value += 10.dp
    }

    block(border, offsetY, zIndex, scale, draggableState, onDragStopped)
}

@Composable
fun CardContent(model: CardModel, refreshEnabled: MutableState<Boolean>,  refresh: (String) -> Unit) {
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
                /*Text(
                    text = "обновлено ${model.date}",
                    fontSize = 15.6.sp,
                    textAlign = TextAlign.End
                )*/
            }
        }
    }
}


@Preview
@Composable
fun CardContentPreview() {
    AppTheme {
        CardContent(CardModel(title = "Спорт", availableAmount = "1000", date = "Сегодня"), refreshEnabled = mutableStateOf(false)) {}
    }
}

@Preview
@Composable
fun CardListScreenPreview() {
    AppTheme {
        CardListScreen()
    }
}
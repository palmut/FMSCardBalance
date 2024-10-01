package net.palmut.fmscardbalance.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import fmscardbalance.composeapp.generated.resources.Res
import fmscardbalance.composeapp.generated.resources.ic_refresh
import fmscardbalance.composeapp.generated.resources.round_close_24
import net.palmut.fmscardbalance.component.MainComponent
import net.palmut.fmscardbalance.component.entity.Card
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

const val CARD_ASPECT_RATIO = 1.58f
const val CARD_WIDTH = 0.8f
const val SEMITRANSPARENT = 0.9f
const val TRANSPARENT = 0f

//todo сделать отступы по вертикали для андроида

@Composable
fun CardListScreen(component: MainComponent) {
    val state by component.model.subscribeAsState()
    val hapticFeedback = LocalHapticFeedback.current

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
                modifier = Modifier.fillMaxHeight()
                    .align(Alignment.Center),
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
                            .fillMaxWidth(CARD_WIDTH)
                            .aspectRatio(CARD_ASPECT_RATIO)
                            .advancedShadow()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onLongPress = {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
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
                                        painter = painterResource(Res.drawable.round_close_24),
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
                .align(Alignment.Center)
                .fillMaxWidth(CARD_WIDTH),
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
                        .fillMaxWidth()
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
                        .fillMaxWidth(CARD_WIDTH)
                        .zIndex(-5f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.textButtonColors(containerColor = Color(0xFF138DFF)),
                    border = BorderStroke(2.dp, Color.Black),
                    onClick = {
                        component.addNewCard()
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
private fun CardContent(
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
                    painter = painterResource(Res.drawable.ic_refresh),
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

expect fun Modifier.advancedShadow(
    color: Color = Color.Black,
    alpha: Float = 0.9f,
    cornersRadius: Dp = 10.dp,
    offsetY: Dp = 3.dp,
    offsetX: Dp = 3.dp,
    blurRadius: Float = 0f,
): Modifier

@Preview
@Composable
private fun CardListScreenPreview() {
    AppTheme {
//        CardListScreen(repository = PreviewBalanceRepository())
    }
}

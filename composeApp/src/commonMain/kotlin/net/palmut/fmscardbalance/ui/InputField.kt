package net.palmut.fmscardbalance.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InputField(
    modifier: Modifier = Modifier,
    type: InputFieldType = InputFieldType.TEXT,
    state: String,
    onInput: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val keyboardType = when (type) {
        InputFieldType.NUMBER -> {
            KeyboardType.Number
        }

        InputFieldType.PASSWORD -> {
            KeyboardType.Password
        }

        InputFieldType.PHONE -> {
            KeyboardType.Phone
        }

        else -> {
            KeyboardType.Text
        }
    }

    BasicTextField(
        modifier = modifier
            .height(48.dp)
            .fillMaxWidth(CARD_WIDTH)
            .background(
                color = Color(0xFFFFFFF9),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                border = BorderStroke(2.dp, Color.Black),
                shape = RoundedCornerShape(16.dp)
            ),
        singleLine = true,
        textStyle = TextStyle(
            color = MaterialTheme.colorScheme.tertiary,
            fontSize = 25.sp,
            textAlign = TextAlign.Center,
            fontFamily = AppFont
        ),
        value = state,
        onValueChange = onInput,
        keyboardActions = KeyboardActions(onDone = {
            keyboardController?.hide()
            focusManager.clearFocus()
        }),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            autoCorrect = false
        ),
        cursorBrush = SolidColor(Color(0xFF138DFF)),
        decorationBox = { innerTextField ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                innerTextField()
            }
        }
    )
}

enum class InputFieldType {
    TEXT, NUMBER, PASSWORD, PHONE
}


@Composable
private fun InputFieldPreview() {
    AppTheme {
        InputField(
            state = ""
        ) {}
    }
}
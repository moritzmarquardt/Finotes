package de.marquisproject.finotes.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import de.marquisproject.finotes.utils.MarkdownUtils

@Composable
fun MarkdownTextField(
    modifier: Modifier = Modifier,
    state: TextFieldState = rememberTextFieldState(),
    placeholder: @Composable (() -> Unit)? = null,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    focusRequester: FocusRequester = remember { FocusRequester() },
    readOnly: Boolean = false
) {
    val onBackground = MaterialTheme.colorScheme.onBackground
    val markerColor = remember(textStyle.color, onBackground) {
        (textStyle.color.takeIf { it != Color.Unspecified } 
            ?: onBackground).copy(alpha = 0.35f)
    }

    val markdownOutputTransformation = remember(markerColor) {
        // as long as marker color does not change, the output transformation is remembered
        // through re-composition which is good for performance
        MarkdownUtils.getMarkdownOutputTransformation(markerColor)
    }
    val inputTransformation = remember {
        // this will be remembered once because inside is just a lambda which does not change
        MarkdownUtils.getListLogicInputTransformation()
    }

    TextField(
        state = state,
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        textStyle = textStyle,
        outputTransformation = markdownOutputTransformation,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Default,
            autoCorrectEnabled = true
        ),
        placeholder = placeholder,
        readOnly = readOnly,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        inputTransformation = inputTransformation
    )
}

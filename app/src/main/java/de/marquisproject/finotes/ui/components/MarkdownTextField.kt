package de.marquisproject.finotes.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import de.marquisproject.finotes.utils.MarkdownUtils

@Composable
fun MarkdownTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    focusRequester: FocusRequester = remember { FocusRequester() },
    showToolbar: Boolean = true
) {
    val showToolbarState = remember { mutableStateOf(showToolbar) }
    val currentValue = value
    
    Column(modifier = modifier) {
        if (showToolbarState.value) {
            MarkdownToolbar(
                onBoldClick = {
                    onValueChange(MarkdownUtils.applyFormatting(currentValue, "**"))
                },
                onItalicClick = {
                    onValueChange(MarkdownUtils.applyFormatting(currentValue, "*"))
                },
                onStrikethroughClick = {
                    onValueChange(MarkdownUtils.applyFormatting(currentValue, "~~"))
                },
                onBulletListClick = {
                    val cursorPosition = currentValue.selection.start
                    val text = currentValue.text
                    val textBeforeCursor = text.substring(0, cursorPosition)
                    val currentLine = textBeforeCursor.substringAfterLast('\n')
                    
                    // If not already in a list, start a bullet list
                    if (!currentLine.startsWith("-\s".toRegex()) && !currentLine.matches(Regex("^\\d+\\.\\s"))) {
                        val newText = if (currentLine.isBlank()) {
                            text.substring(0, cursorPosition) + "- " + text.substring(cursorPosition)
                        } else {
                            text.substring(0, cursorPosition) + "\n- " + text.substring(cursorPosition)
                        }
                        onValueChange(currentValue.copy(
                            text = newText,
                            selection = androidx.compose.ui.text.TextRange(cursorPosition + 3)
                        ))
                    }
                },
                onNumberedListClick = {
                    val cursorPosition = currentValue.selection.start
                    val text = currentValue.text
                    val textBeforeCursor = text.substring(0, cursorPosition)
                    val currentLine = textBeforeCursor.substringAfterLast('\n')
                    
                    // If not already in a list, start a numbered list
                    if (!currentLine.startsWith("-\s".toRegex()) && !currentLine.matches(Regex("^\\d+\\.\\s"))) {
                        val newText = if (currentLine.isBlank()) {
                            text.substring(0, cursorPosition) + "1. " + text.substring(cursorPosition)
                        } else {
                            text.substring(0, cursorPosition) + "\n1. " + text.substring(cursorPosition)
                        }
                        onValueChange(currentValue.copy(
                            text = newText,
                            selection = androidx.compose.ui.text.TextRange(cursorPosition + 4)
                        ))
                    }
                },
                onLinkClick = {
                    onValueChange(MarkdownUtils.applyFormatting(currentValue, "[", "]()"))
                    // Move cursor to be between the brackets
                    val newSelection = currentValue.selection.start + 1
                    onValueChange(currentValue.copy(
                        selection = androidx.compose.ui.text.TextRange(newSelection, newSelection)
                    ))
                }
            )
        }
        
        BasicTextField(
            value = value,
            onValueChange = { newValue ->
                onValueChange(newValue)
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        // Show toolbar when field is focused
                        showToolbarState.value = true
                    }
                }
                .onKeyEvent { keyEvent ->
                    if (keyEvent.key == Key.Enter) {
                        val processedValue = MarkdownUtils.handleEnterKey(value)
                        if (processedValue != value) {
                            onValueChange(processedValue)
                            return@onKeyEvent true
                        }
                    } else if (keyEvent.key == Key.Backspace) {
                        val processedValue = MarkdownUtils.handleBackspace(value)
                        if (processedValue != value) {
                            onValueChange(processedValue)
                            return@onKeyEvent true
                        }
                    }
                    false
                },
            textStyle = textStyle,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Default
            ),
            keyboardActions = KeyboardActions(
                onDone = { /* Handle done if needed */ }
            ),
            decorationBox = { innerTextField ->
                if (currentValue.text.isEmpty() && placeholder != null) {
                    placeholder()
                }
                innerTextField()
            }
        )
    }
}
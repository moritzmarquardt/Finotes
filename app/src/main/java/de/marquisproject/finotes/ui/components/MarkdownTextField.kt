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
    
    Column(modifier = modifier) {
        if (showToolbar.value) {
            MarkdownToolbar(
                onBoldClick = {
                    onValueChange(MarkdownUtils.applyFormatting(value, "**"))
                },
                onItalicClick = {
                    onValueChange(MarkdownUtils.applyFormatting(value, "*"))
                },
                onStrikethroughClick = {
                    onValueChange(MarkdownUtils.applyFormatting(value, "~~"))
                },
                onBulletListClick = {
                    val cursorPosition = value.selection.start
                    val text = value.text
                    val textBeforeCursor = text.substring(0, cursorPosition)
                    val currentLine = textBeforeCursor.substringAfterLast('\n')
                    
                    // If not already in a list, start a bullet list
                    if (!currentLine.startsWith("-\s") && !currentLine.matches(Regex("^\\d+\\.\\s"))) {
                        val newText = if (currentLine.isBlank()) {
                            text.insert(cursorPosition, "- ")
                        } else {
                            text.insert(cursorPosition, "\n- ")
                        }
                        onValueChange(value.copy(
                            text = newText,
                            selection = TextRange(cursorPosition + 3)
                        ))
                    }
                },
                onNumberedListClick = {
                    val cursorPosition = value.selection.start
                    val text = value.text
                    val textBeforeCursor = text.substring(0, cursorPosition)
                    val currentLine = textBeforeCursor.substringAfterLast('\n')
                    
                    // If not already in a list, start a numbered list
                    if (!currentLine.startsWith("-\s") && !currentLine.matches(Regex("^\\d+\\.\\s"))) {
                        val newText = if (currentLine.isBlank()) {
                            text.insert(cursorPosition, "1. ")
                        } else {
                            text.insert(cursorPosition, "\n1. ")
                        }
                        onValueChange(value.copy(
                            text = newText,
                            selection = TextRange(cursorPosition + 4)
                        ))
                    }
                },
                onLinkClick = {
                    onValueChange(MarkdownUtils.applyFormatting(value, "[", "]()"))
                    // Move cursor to be between the brackets
                    val newSelection = value.selection.start + 1
                    onValueChange(value.copy(
                        selection = TextRange(newSelection, newSelection)
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
                        showToolbar.value = true
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
                if (value.text.isEmpty() && placeholder != null) {
                    placeholder()
                }
                innerTextField()
            }
        )
    }
}
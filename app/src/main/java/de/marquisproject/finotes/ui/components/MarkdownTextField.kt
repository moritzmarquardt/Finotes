package de.marquisproject.finotes.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import de.marquisproject.finotes.utils.MarkdownUtils

@Composable
fun MarkdownTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    focusRequester: FocusRequester = remember { FocusRequester() },
    readOnly: Boolean = false
) {
    val markerColor = (textStyle.color.takeIf { it != Color.Unspecified } 
        ?: MaterialTheme.colorScheme.onBackground).copy(alpha = 0.35f)

    TextField(
        value = value,
        onValueChange = { newValue ->
            if (newValue.text.length > value.text.length && 
                newValue.text.getOrNull(newValue.selection.start - 1) == '\n') {
                onValueChange(MarkdownUtils.handleEnterKey(newValue))
            } else {
                onValueChange(newValue)
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
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
        visualTransformation = remember(markerColor) { MarkdownVisualTransformation(markerColor) },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Default
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
        )
    )
}

/**
 * VisualTransformation that applies WhatsApp-style formatting (bold, italic, strikethrough)
 * while keeping the Markdown symbols visible but faded.
 */
class MarkdownVisualTransformation(private val markerColor: Color) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val annotatedString = buildAnnotatedString {
            val rawText = text.text
            append(rawText)

            // Bold: *text* or **text**
            Regex("(\\*{1,2})(.*?)\\1").findAll(rawText).forEach { match ->
                val prefix = match.groups[1]!!

                // Style the whole range as bold
                addStyle(SpanStyle(fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
                
                // Fade the markers
                addStyle(SpanStyle(color = markerColor), prefix.range.first, prefix.range.last + 1)
                addStyle(SpanStyle(color = markerColor), match.range.last - prefix.value.length + 1, match.range.last + 1)
            }

            // Italic: _text_
            Regex("(_)(.*?)\\1").findAll(rawText).forEach { match ->
                val prefix = match.groups[1]!!
                
                addStyle(SpanStyle(fontStyle = FontStyle.Italic), match.range.first, match.range.last + 1)
                
                addStyle(SpanStyle(color = markerColor), prefix.range.first, prefix.range.last + 1)
                addStyle(SpanStyle(color = markerColor), match.range.last, match.range.last + 1)
            }

            // Strikethrough: ~text~ or ~~text~~
            Regex("(~{1,2})(.*?)\\1").findAll(rawText).forEach { match ->
                val prefix = match.groups[1]!!
                
                addStyle(SpanStyle(textDecoration = TextDecoration.LineThrough), match.range.first, match.range.last + 1)
                
                addStyle(SpanStyle(color = markerColor), prefix.range.first, prefix.range.last + 1)
                addStyle(SpanStyle(color = markerColor), match.range.last - prefix.value.length + 1, match.range.last + 1)
            }
        }

        return TransformedText(annotatedString, OffsetMapping.Identity)
    }
}

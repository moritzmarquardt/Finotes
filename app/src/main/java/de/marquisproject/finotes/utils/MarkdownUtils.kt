package de.marquisproject.finotes.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration

object MarkdownUtils {

    val BOLD_REGEX = Regex("(\\*{1,2})(.*?)\\1")
    val ITALIC_REGEX = Regex("(_)(.*?)\\1")
    val STRIKETHROUGH_REGEX = Regex("(~{1,2})(.*?)\\1")

    private val BULLET_PREFIX_REGEX = Regex("^(\\s*)([-*])\\s")

    /**
     * Handles the enter key to continue or terminate list formatting.
     * Works both when triggered by a hardware key event (before newline is added)
     * or by a change event (after newline is added).
     */
    fun handleEnterKey(currentValue: TextFieldValue): TextFieldValue {
        val selection = currentValue.selection
        val text = currentValue.text
        
        if (selection.start < 0) return currentValue.copy(composition = null)
        
        // Determine if the newline is already present (triggered by onValueChange)
        val isAfterNewline = selection.start > 0 && text.getOrNull(selection.start - 1) == '\n'
        
        val lineEnd = if (isAfterNewline) selection.start - 1 else selection.start
        val lastNewLine = text.lastIndexOf('\n', lineEnd - 1)
        val lineStart = if (lastNewLine == -1) 0 else lastNewLine + 1
        
        val previousLine = text.substring(lineStart, lineEnd)
        
        // Handle Bullet Lists (- or *)
        val bulletMatch = BULLET_PREFIX_REGEX.find(previousLine)
        if (bulletMatch != null) {
            val indent = bulletMatch.groups[1]?.value ?: ""
            val symbol = bulletMatch.groups[2]?.value ?: "-"
            
            // If the line is empty (just the bullet and space), terminate the list
            if (previousLine.trim() == symbol) {
                val newText = text.substring(0, lineStart) + text.substring(selection.start)
                return currentValue.copy(
                    text = newText,
                    selection = TextRange(lineStart),
                    composition = null
                )
            }
            
            // Otherwise, continue the list on the new line
            val newLinePrefix = (if (isAfterNewline) "" else "\n") + indent + symbol + " "
            val newText = text.substring(0, selection.start) + newLinePrefix + text.substring(selection.start)
            return currentValue.copy(
                text = newText,
                selection = TextRange(selection.start + newLinePrefix.length),
                composition = null
            )
        }
        
        return currentValue.copy(composition = null)
    }

    /**
     * Handles backspace to remove list formatting if the cursor is at the start of a list item.
     */
    fun handleBackspace(oldValue: TextFieldValue, newValue: TextFieldValue): TextFieldValue {
        // If it's not a deletion, just return the newValue
        if (newValue.text.length >= oldValue.text.length) return newValue
        
        val selection = oldValue.selection
        val text = oldValue.text
        
        // Only trigger if the cursor was collapsed and at a potential bullet position
        if (!selection.collapsed || selection.start == 0) return newValue
        
        val lineStart = text.lastIndexOf('\n', selection.start - 1).let { if (it == -1) 0 else it + 1 }
        val currentLine = text.substring(lineStart, selection.start)
        
        // If the user deleted the space of a "- " or "* " prefix, remove the whole prefix
        if (currentLine.matches(BULLET_PREFIX_REGEX)) {
            return newValue.copy(
                text = text.substring(0, lineStart) + text.substring(selection.start),
                selection = TextRange(lineStart),
                composition = null
            )
        }
        
        return newValue
    }

    /**
     * Renders markdown text into an AnnotatedString, applying styles and removing markers.
     */
    fun renderMarkdown(text: String): AnnotatedString {
        val markers = mutableListOf<IntRange>()
        val styles = mutableListOf<Pair<IntRange, SpanStyle>>()

        BOLD_REGEX.findAll(text).forEach { match ->
            val prefix = match.groups[1]!!
            markers.add(prefix.range)
            markers.add(IntRange(match.range.last - prefix.value.length + 1, match.range.last))
            styles.add(match.range to SpanStyle(fontWeight = FontWeight.Bold))
        }

        ITALIC_REGEX.findAll(text).forEach { match ->
            val prefix = match.groups[1]!!
            markers.add(prefix.range)
            markers.add(IntRange(match.range.last, match.range.last))
            styles.add(match.range to SpanStyle(fontStyle = FontStyle.Italic))
        }

        STRIKETHROUGH_REGEX.findAll(text).forEach { match ->
            val prefix = match.groups[1]!!
            markers.add(prefix.range)
            markers.add(IntRange(match.range.last - prefix.value.length + 1, match.range.last))
            styles.add(match.range to SpanStyle(textDecoration = TextDecoration.LineThrough))
        }

        val skip = BooleanArray(text.length)
        markers.forEach { range ->
            for (i in range) {
                if (i in skip.indices) skip[i] = true
            }
        }

        return buildAnnotatedString {
            val offsetMap = IntArray(text.length + 1)
            var removedCount = 0
            for (i in text.indices) {
                if (skip[i]) {
                    removedCount++
                } else {
                    append(text[i])
                }
                offsetMap[i + 1] = i + 1 - removedCount
            }

            styles.forEach { (range, style) ->
                val start = offsetMap[range.first]
                val end = offsetMap[range.last + 1]
                if (start < end) {
                    addStyle(style, start, end)
                }
            }
        }
    }
}

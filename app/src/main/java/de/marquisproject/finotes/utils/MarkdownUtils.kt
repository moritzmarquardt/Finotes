package de.marquisproject.finotes.utils

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.delete
import androidx.compose.foundation.text.input.insert
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

object MarkdownUtils {

    val BOLD_REGEX = Regex("(\\*{1,2})(.*?)\\1")
    val ITALIC_REGEX = Regex("(_)(.*?)\\1")
    val STRIKETHROUGH_REGEX = Regex("(~{1,2})(.*?)\\1")

    private val BULLET_PREFIX_REGEX = Regex("^(\\s*)([-*])\\s")
    private val JUST_SYMBOL_REGEX = Regex("^(\\s*)([-*])$")

    /**
     * Handles list formatting in a TextFieldBuffer for the new TextFieldState API.
     */
    @OptIn(ExperimentalFoundationApi::class)
    fun handleListInput(buffer: TextFieldBuffer) {
        val text = buffer.asCharSequence()
        val selection = buffer.selection

        // return and exit if a selection is present. List logic should only be applied
        // when a user types normally with no selection.
        if (!selection.collapsed) return

        val changes = buffer.changes
        // Only handle single-character changes to avoid interfering with pastes/multi-edits
        if (changes.changeCount == 1) {
            val changeRange = changes.getRange(0)
            val originalRange = changes.getOriginalRange(0)

            // 1. Handle Newline (Enter key)
            if (changeRange.length == 1 && originalRange.length == 0 && (text.getOrNull(changeRange.start) == '\n')) {
                val lineEnd = changeRange.start
                val lastNewLine = if (lineEnd == 0) -1 else text.lastIndexOf('\n', lineEnd - 1)
                val lineStart = if (lastNewLine == -1) 0 else lastNewLine + 1
                
                val previousLine = text.substring(lineStart, lineEnd)
                val bulletMatch = BULLET_PREFIX_REGEX.find(previousLine)
                
                if (bulletMatch != null) {
                    val indent = bulletMatch.groups[1]?.value ?: ""
                    val symbol = bulletMatch.groups[2]?.value ?: "-"
                    
                    if (previousLine.trim() == symbol) {
                        // User pressed enter on an empty bullet line: remove the bullet and end the list
                        buffer.delete(lineStart, changeRange.end)
                    } else {
                        // Continue the list with the same indent and symbol
                        buffer.insert(changeRange.end, "$indent$symbol ")
                    }
                }
                return
            }

            // 2. Handle Backspace (Ported from handleBackspace)
            // Triggers if a single character was deleted, and we are now left with just a symbol
            if (changeRange.length == 0 && originalRange.length == 1) {
                val lineStart = if (selection.start <= 0) 0 else {
                    text.lastIndexOf('\n', selection.start - 1).let { if (it == -1) 0 else it + 1 }
                }
                val currentLine = text.substring(lineStart, selection.start)
                val originalText = buffer.originalText
                
                // Safety check for indices against original text
                if (lineStart >= originalText.length) return
                
                // Optimize wasBullet check to avoid large substring allocations
                val lineEndInOriginal = originalText.indexOf('\n', lineStart).let {
                    if (it == -1) originalText.length else it
                }
                val wasBullet = BULLET_PREFIX_REGEX.find(originalText.substring(lineStart, lineEndInOriginal)) != null
                val isJustSymbol = JUST_SYMBOL_REGEX.matches(currentLine)
                
                if (wasBullet && isJustSymbol) {
                    buffer.delete(lineStart, selection.start)
                }
            }
        }
    }

    /**
     * Returns an InputTransformation that handles Markdown list formatting.
     *
     * @return An InputTransformation that handles Markdown list formatting.
     */
    fun getListLogicInputTransformation(): InputTransformation {
        return InputTransformation {
            handleListInput(this)
        }
    }

    /**
     * Applies Markdown styling to the visual output of a TextField.
     *
     * @param markerColor The color to use for the markers.
     * @return An OutputTransformation that applies Markdown styling.
     */
    fun getMarkdownOutputTransformation(markerColor: Color): OutputTransformation {
        return OutputTransformation {
            val rawText = asCharSequence()
            forEachMarkdownMatch(rawText) { style, fullRange, startMarker, endMarker ->
                addStyle(style, fullRange.first, fullRange.last + 1)
                addStyle(SpanStyle(color = markerColor), startMarker.first, startMarker.last + 1)
                addStyle(SpanStyle(color = markerColor), endMarker.first, endMarker.last + 1)
            }
        }
    }

    /**
     * Renders Markdown text into an AnnotatedString, applying styles and removing markers.
     */
    fun renderMarkdown(text: String): AnnotatedString {
        val markers = mutableListOf<IntRange>()
        val styles = mutableListOf<Pair<IntRange, SpanStyle>>()

        forEachMarkdownMatch(text) { style, fullRange, startMarker, endMarker ->
            markers.add(startMarker)
            markers.add(endMarker)
            styles.add(fullRange to style)
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

    /**
     * Internal helper to unify the regex matching logic.
     */
    private fun forEachMarkdownMatch(
        text: CharSequence,
        action: (style: SpanStyle, fullRange: IntRange, startMarker: IntRange, endMarker: IntRange) -> Unit
    ) {
        // Bold: *text* or **text**
        BOLD_REGEX.findAll(text).forEach { match ->
            val prefix = match.groups[1]!!
            action(
                SpanStyle(fontWeight = FontWeight.Bold),
                match.range,
                prefix.range,
                IntRange(match.range.last - prefix.value.length + 1, match.range.last)
            )
        }

        // Italic: _text_
        ITALIC_REGEX.findAll(text).forEach { match ->
            val prefix = match.groups[1]!!
            action(
                SpanStyle(fontStyle = FontStyle.Italic),
                match.range,
                prefix.range,
                IntRange(match.range.last, match.range.last)
            )
        }

        // Strikethrough: ~text~ or ~~text~~
        STRIKETHROUGH_REGEX.findAll(text).forEach { match ->
            val prefix = match.groups[1]!!
            action(
                SpanStyle(textDecoration = TextDecoration.LineThrough),
                match.range,
                prefix.range,
                IntRange(match.range.last - prefix.value.length + 1, match.range.last)
            )
        }
    }
}

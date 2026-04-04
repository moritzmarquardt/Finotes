package de.marquisproject.finotes.utils

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

object MarkdownUtils {

    private val BULLET_REGEX = Regex("^(\\s*)([-*])\\s")
    private val ORDERED_REGEX = Regex("^(\\s*)((\\d+)|([a-zA-Z]))([.)])\\s")

    /**
     * Toggles formatting (e.g., bold, italic) for the current selection or at the cursor.
     */
    fun toggleFormatting(
        currentValue: TextFieldValue,
        prefix: String,
        suffix: String = prefix
    ): TextFieldValue {
        val selection = currentValue.selection
        val text = currentValue.text

        return if (selection.collapsed) {
            val newText = text.substring(0, selection.start) + prefix + suffix + text.substring(selection.start)
            currentValue.copy(
                text = newText,
                selection = TextRange(selection.start + prefix.length),
                composition = null
            )
        } else {
            val selectedText = text.substring(selection.start, selection.end)
            val isFormatted = selectedText.startsWith(prefix) && selectedText.endsWith(suffix)
            
            val newText = if (isFormatted) {
                text.substring(0, selection.start) + 
                selectedText.substring(prefix.length, selectedText.length - suffix.length) + 
                text.substring(selection.end)
            } else {
                text.substring(0, selection.start) + prefix + selectedText + suffix + text.substring(selection.end)
            }
            
            currentValue.copy(
                text = newText,
                selection = if (isFormatted) {
                    TextRange(selection.start, selection.end - prefix.length - suffix.length)
                } else {
                    TextRange(selection.start + prefix.length, selection.end + prefix.length)
                },
                composition = null
            )
        }
    }

    fun handleEnterKey(currentValue: TextFieldValue): TextFieldValue {
        val selection = currentValue.selection
        val text = currentValue.text
        
        if (selection.start < 1) return currentValue.copy(composition = null)
        
        val lastNewLine = text.lastIndexOf('\n', selection.start - 2)
        val lineStart = if (lastNewLine == -1) 0 else lastNewLine + 1
        val previousLine = text.substring(lineStart, selection.start - 1)
        
        // Handle Bullet Lists (- or *)
        val bulletMatch = BULLET_REGEX.find(previousLine)
        if (bulletMatch != null) {
            val indent = bulletMatch.groups[1]?.value ?: ""
            val symbol = bulletMatch.groups[2]?.value ?: "-"
            if (previousLine.trim().length <= 1) { // Just the bullet and maybe whitespace
                val newText = text.substring(0, lineStart) + text.substring(selection.start)
                return currentValue.copy(text = newText, selection = TextRange(lineStart), composition = null)
            }
            val newLinePrefix = "$indent$symbol "
            val newText = text.substring(0, selection.start) + newLinePrefix + text.substring(selection.start)
            return currentValue.copy(
                text = newText,
                selection = TextRange(selection.start + newLinePrefix.length),
                composition = null
            )
        }
        
        // Handle Ordered Lists (1. 1) a. a) )
        val orderedMatch = ORDERED_REGEX.find(previousLine)
        if (orderedMatch != null) {
            val indent = orderedMatch.groups[1]?.value ?: ""
            val marker = orderedMatch.groups[2]?.value ?: "1"
            val separator = orderedMatch.groups[5]?.value ?: "."
            
            val content = previousLine.substring(orderedMatch.range.last + 1).trim()
            if (content.isEmpty()) {
                val newText = text.substring(0, lineStart) + text.substring(selection.start)
                return currentValue.copy(text = newText, selection = TextRange(lineStart), composition = null)
            }
            
            val nextMarker = incrementMarker(marker)
            val newLinePrefix = "$indent$nextMarker$separator "
            val newText = text.substring(0, selection.start) + newLinePrefix + text.substring(selection.start)
            return currentValue.copy(
                text = newText,
                selection = TextRange(selection.start + newLinePrefix.length),
                composition = null
            )
        }
        
        return currentValue.copy(composition = null)
    }

    private fun incrementMarker(marker: String): String {
        val num = marker.toIntOrNull()
        if (num != null) return (num + 1).toString()
        
        if (marker.length == 1) {
            val char = marker[0]
            if (char in 'a'..'y' || char in 'A'..'Y') return (char + 1).toString()
            if (char == 'z') return "aa"
            if (char == 'Z') return "AA"
        }
        return marker
    }

    fun handleBackspace(currentValue: TextFieldValue): TextFieldValue {
        val selection = currentValue.selection
        val text = currentValue.text
        
        if (!selection.collapsed || selection.start == 0) return currentValue
        
        val lineStart = text.lastIndexOf('\n', selection.start - 1).let { if (it == -1) 0 else it + 1 }
        val currentLine = text.substring(lineStart, selection.start)
        
        if (currentLine.matches(BULLET_REGEX) || currentLine.matches(ORDERED_REGEX)) {
            return currentValue.copy(
                text = text.substring(0, lineStart) + text.substring(selection.start),
                selection = TextRange(lineStart),
                composition = null
            )
        }
        
        return currentValue
    }

    fun toggleBulletList(currentValue: TextFieldValue): TextFieldValue {
        val selection = currentValue.selection
        val text = currentValue.text
        val lineStart = text.lastIndexOf('\n', selection.start - 1).let { if (it == -1) 0 else it + 1 }
        val lineEnd = text.indexOf('\n', selection.start).let { if (it == -1) text.length else it }
        val currentLine = text.substring(lineStart, lineEnd)
        
        val match = BULLET_REGEX.find(currentLine)
        return if (match != null) {
            val newText = text.substring(0, lineStart + match.range.first) + 
                         currentLine.substring(match.range.last + 1) + 
                         text.substring(lineEnd)
            currentValue.copy(
                text = newText,
                selection = TextRange(selection.start - (match.range.last - match.range.first + 1)),
                composition = null
            )
        } else {
            val prefix = "- "
            val newText = text.substring(0, lineStart) + prefix + text.substring(lineStart)
            currentValue.copy(
                text = newText,
                selection = TextRange(selection.start + prefix.length),
                composition = null
            )
        }
    }

    fun toggleNumberedList(currentValue: TextFieldValue): TextFieldValue {
        val selection = currentValue.selection
        val text = currentValue.text
        val lineStart = text.lastIndexOf('\n', selection.start - 1).let { if (it == -1) 0 else it + 1 }
        val lineEnd = text.indexOf('\n', selection.start).let { if (it == -1) text.length else it }
        val currentLine = text.substring(lineStart, lineEnd)
        
        val match = ORDERED_REGEX.find(currentLine)
        return if (match != null) {
            val newText = text.substring(0, lineStart + match.range.first) + 
                         currentLine.substring(match.range.last + 1) + 
                         text.substring(lineEnd)
            currentValue.copy(
                text = newText,
                selection = TextRange(selection.start - (match.range.last - match.range.first + 1)),
                composition = null
            )
        } else {
            val prefix = "1. "
            val newText = text.substring(0, lineStart) + prefix + text.substring(lineStart)
            currentValue.copy(
                text = newText,
                selection = TextRange(selection.start + prefix.length),
                composition = null
            )
        }
    }

    fun toggleLink(currentValue: TextFieldValue): TextFieldValue {
        val selection = currentValue.selection
        val text = currentValue.text
        
        return if (selection.collapsed) {
            val prefix = "["
            val suffix = "](url)"
            val newText = text.substring(0, selection.start) + prefix + suffix + text.substring(selection.start)
            currentValue.copy(
                text = newText,
                selection = TextRange(selection.start + prefix.length, selection.start + prefix.length),
                composition = null
            )
        } else {
            val selectedText = text.substring(selection.start, selection.end)
            val newText = text.substring(0, selection.start) + "[" + selectedText + "](url)" + text.substring(selection.end)
            val urlStart = selection.start + selectedText.length + 3
            currentValue.copy(
                text = newText,
                selection = TextRange(urlStart, urlStart + 3),
                composition = null
            )
        }
    }

    fun markdownToHtml(markdown: String): String {
        var result = markdown
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
        
        result = Regex("^#\\s+(.+)$", RegexOption.MULTILINE).replace(result, "<h1>$1</h1>")
        result = Regex("^##\\s+(.+)$", RegexOption.MULTILINE).replace(result, "<h2>$1</h2>")
        result = Regex("^###\\s+(.+)$", RegexOption.MULTILINE).replace(result, "<h3>$1</h3>")
        
        result = result.replace(Regex("\\*\\*(.*?)\\*\\*"), "<strong>$1</strong>")
        result = result.replace(Regex("__([^_]+)__"), "<strong>$1</strong>")
        result = result.replace(Regex("\\*([^*]+)\\*"), "<em>$1</em>")
        result = result.replace(Regex("_([^_]+)_"), "<em>$1</em>")
        result = result.replace(Regex("~~(.*?)~~"), "<s>$1</s>")
        result = result.replace(Regex("\\[([^\\]]+)\\]\\(([^)]+)\\)"), "<a href=\"$2\">$1</a>")
        result = Regex("^\\s*([-*])\\s+(.+)$", RegexOption.MULTILINE).replace(result, "<li>$2</li>")
        result = Regex("^\\s*((\\d+)|([a-zA-Z]))[.)]\\s+(.+)$", RegexOption.MULTILINE).replace(result, "<li>$4</li>")
        
        val lines = result.split("\n")
        val processedLines = lines.map { line ->
            if (line.startsWith("<h") || line.startsWith("<li")) line else if (line.isBlank()) "" else "<p>$line</p>"
        }
        
        return processedLines.joinToString("\n").replace("\n\n", "<br/>")
    }
}

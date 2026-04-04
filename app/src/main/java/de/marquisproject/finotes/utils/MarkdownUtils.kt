package de.marquisproject.finotes.utils

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

object MarkdownUtils {

    private val BULLET_REGEX = Regex("^(\\s*)([-*])\\s")
    private val ORDERED_REGEX = Regex("^(\\s*)((\\d+)|([a-zA-Z]))([.)])\\s")

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
}

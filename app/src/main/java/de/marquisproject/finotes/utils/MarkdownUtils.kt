package de.marquisproject.finotes.utils

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

object MarkdownUtils {

    private val BULLET_REGEX = Regex("^(\\s*)([-*])\\s")

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
            if (previousLine.trim().length <= symbol.length) { // Just the bullet and maybe whitespace
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
        
        return currentValue.copy(composition = null)
    }

    fun handleBackspace(currentValue: TextFieldValue): TextFieldValue {
        val selection = currentValue.selection
        val text = currentValue.text
        
        if (!selection.collapsed || selection.start == 0) return currentValue
        
        val lineStart = text.lastIndexOf('\n', selection.start - 1).let { if (it == -1) 0 else it + 1 }
        val currentLine = text.substring(lineStart, selection.start)
        
        if (currentLine.matches(BULLET_REGEX)) {
            return currentValue.copy(
                text = text.substring(0, lineStart) + text.substring(selection.start),
                selection = TextRange(lineStart),
                composition = null
            )
        }
        
        return currentValue
    }
}

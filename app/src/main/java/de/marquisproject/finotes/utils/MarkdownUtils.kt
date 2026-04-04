package de.marquisproject.finotes.utils

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

object MarkdownUtils {
    
    // Apply formatting around selected text or at cursor position
    fun applyFormatting(
        currentValue: TextFieldValue,
        prefix: String,
        suffix: String = prefix
    ): TextFieldValue {
        val selection = currentValue.selection
        val text = currentValue.text
        
        return if (selection.start == selection.end) {
            // No selection, insert formatting at cursor
            val newText = text.insert(selection.start, prefix + suffix)
            currentValue.copy(
                text = newText,
                selection = TextRange(selection.start + prefix.length)
            )
        } else {
            // Selection exists, wrap it with formatting
            val selectedText = text.substring(selection.start, selection.end)
            val beforeSelection = text.substring(0, selection.start)
            val afterSelection = text.substring(selection.end)
            
            val newText = beforeSelection + prefix + selectedText + suffix + afterSelection
            currentValue.copy(
                text = newText,
                selection = TextRange(selection.start + prefix.length, selection.start + prefix.length + selectedText.length)
            )
        }
    }
    
    fun handleEnterKey(currentValue: TextFieldValue): TextFieldValue {
        val cursorPosition = currentValue.selection.start
        val text = currentValue.text
        val textBeforeCursor = text.substring(0, cursorPosition)
        
        // Get the current line
        val currentLine = textBeforeCursor.substringAfterLast('\n')
        
        return when {
            // Continue bullet list (- )
            currentLine.startsWith("-\s") -> {
                val indent = currentLine.takeWhile { it == ' ' }.length
                val newText = text.insert(cursorPosition, "\n${' '.repeat(indent)}- ")
                currentValue.copy(
                    text = newText,
                    selection = TextRange(cursorPosition + 3 + indent)
                )
            }
            // Continue numbered list (1. )
            currentLine.matches(Regex("^\\d+\\.\\s")) -> {
                val indent = currentLine.takeWhile { it == ' ' }.length
                val currentNumber = currentLine.substring(indent).substringBefore('.').toIntOrNull() ?: 1
                val newNumber = currentNumber + 1
                val newText = text.insert(cursorPosition, "\n${' '.repeat(indent)}$newNumber. ")
                currentValue.copy(
                    text = newText,
                    selection = TextRange(cursorPosition + (newNumber.toString().length + 2 + indent))
                )
            }
            // Start new bullet list if previous line was a bullet
            textBeforeCursor.endsWith("\n-\s") -> {
                val newText = text.insert(cursorPosition, "\n- ")
                currentValue.copy(
                    text = newText,
                    selection = TextRange(cursorPosition + 3)
                )
            }
            // Start new numbered list if previous line was numbered
            textBeforeCursor.matches(Regex(".*\\n\\d+\\.\\s$")) -> {
                val lastNumber = textBeforeCursor.substringAfterLast('\n').substringBefore('.').toIntOrNull() ?: 1
                val newNumber = lastNumber + 1
                val newText = text.insert(cursorPosition, "\n$newNumber. ")
                currentValue.copy(
                    text = newText,
                    selection = TextRange(cursorPosition + (newNumber.toString().length + 2))
                )
            }
            else -> currentValue // No special handling
        }
    }
    
    fun handleBackspace(currentValue: TextFieldValue): TextFieldValue {
        val cursorPosition = currentValue.selection.start
        val text = currentValue.text
        
        if (cursorPosition <= 0) return currentValue
        
        // Check if we're at the beginning of a list item
        val textBeforeCursor = text.substring(0, cursorPosition)
        val currentLine = textBeforeCursor.substringAfterLast('\n')
        
        return when {
            // Remove bullet point formatting
            currentLine == "- " -> {
                val newText = text.removeRange(cursorPosition - 2, cursorPosition)
                currentValue.copy(
                    text = newText,
                    selection = TextRange(cursorPosition - 2)
                )
            }
            // Remove numbered list formatting
            currentLine.matches(Regex("^\\d+\\.\\s$")) -> {
                val newText = text.removeRange(cursorPosition - currentLine.length, cursorPosition)
                currentValue.copy(
                    text = newText,
                    selection = TextRange(cursorPosition - currentLine.length)
                )
            }
            else -> currentValue // Let default backspace handle it
        }
    }
    
    // Simple Markdown to HTML conversion for display
    fun markdownToHtml(markdown: String): String {
        var result = markdown
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
        
        // Headers
        result = Regex("^#\\s(.+)$", RegexOption.MULTILINE).replace(result, "<h1>$1</h1>")
        result = Regex("^##\\s(.+)$", RegexOption.MULTILINE).replace(result, "<h2>$1</h2>")
        result = Regex("^###\\s(.+)$", RegexOption.MULTILINE).replace(result, "<h3>$1</h3>")
        
        // Bold
        result = result.replace(Regex("\\*\\*(.*?)\\*\\*"), "<strong>$1</strong>")
        result = result.replace(Regex("__([^_]+)__"), "<strong>$1</strong>")
        
        // Italic
        result = result.replace(Regex("\\*([^*]+)\\*"), "<em>$1</em>")
        result = result.replace(Regex("_([^_]+)_"), "<em>$1</em>")
        
        // Strikethrough
        result = result.replace(Regex("~~(.*?)~~"), "<s>$1</s>")
        
        // Links
        result = result.replace(Regex("\[([^\]]+)\]\(([^)]+)\)"), "<a href=\"$2\">$1</a>")
        
        // Lists
        result = result.replace(Regex("^\\s*-\\s(.+)$", RegexOption.MULTILINE), "<li>$1</li>")
        result = result.replace(Regex("^\\s*\\d+\\.\\s(.+)$", RegexOption.MULTILINE), "<li>$1</li>")
        
        // Paragraphs and line breaks
        result = result.replace(Regex("\\n\\n"), "</p><p>")
        result = result.replace(Regex("\\n"), "<br/>")
        
        return "<p>$result</p>"
    }
}
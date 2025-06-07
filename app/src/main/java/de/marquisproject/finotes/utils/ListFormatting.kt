package de.marquisproject.finotes.utils

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/**
 * Handles automatic list continuation and list termination for a TextField.
 * Returns the modified TextFieldValue if a list action occurred, otherwise returns the original newTextFieldValue.
 */
fun handleListLogic(
    oldTextFieldValue: TextFieldValue,
    newTextFieldValue: TextFieldValue
): TextFieldValue {
    val oldText = oldTextFieldValue.text
    val newText = newTextFieldValue.text
    val newSelection = newTextFieldValue.selection

    val enterPressed = newText.length > oldText.length &&
            newSelection.start == newSelection.end &&
            newSelection.start > 0 &&
            newText[newSelection.start - 1] == '\n'
    // check the enter pressed in this way and not directly checking key events since checking key events depends on the used keyboard and so on

    if (!enterPressed) {
        return newTextFieldValue
    } else {
        // Only apply list logic if a newline was just inserted and it's not a deletion
        val textBeforeCursor = newText.substring(0, newSelection.start - 1)
        val textAfterCursor = newText.substring(newSelection.start)
        val previousLine = textBeforeCursor.substringAfterLast('\n', textBeforeCursor)
        //val listItemPattern = Regex("""^-\s""")
        val listItemPattern = Regex("""^\s*-\s""")
        val matchResult = listItemPattern.find(previousLine)
        val doesMatchPreviousLine = matchResult != null && matchResult.range.first == 0

        return when {
            // Case 1: Continue an existing list item
            doesMatchPreviousLine && previousLine.trim() != "-" -> {
                val updatedText = "$textBeforeCursor\n- $textAfterCursor"
                newTextFieldValue.copy(
                    text = updatedText,
                    selection = TextRange(newSelection.start + 2)
                )
            }
            // Case 2: Terminate an empty list item (remove "- ")
            previousLine.trim() == "-" -> {
                val updatedText = textBeforeCursor.dropLast(previousLine.length) + textAfterCursor
                newTextFieldValue.copy(
                    text = updatedText,
                    selection = TextRange(newSelection.start - previousLine.length -1)
                )
            }
            else -> {
                // No list action needed, return original newTextFieldValue
                newTextFieldValue
            }
        }
    }
}
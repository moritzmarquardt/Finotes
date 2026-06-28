package de.marquisproject.finotes.utils

import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.insert
import androidx.compose.ui.text.TextRange
import org.junit.Assert.assertEquals
import org.junit.Test

class MarkdownUtilsTest {

    /**
     * Creates a TextFieldState with the given initial text and selection, and applies the given edit block to it as well as the list logic.
     *
     * @param initialText The initial text of the TextFieldState.
     * @param initialSelection The initial selection of the TextFieldState.
     * @param editBlock A lambda that will be applied to the TextFieldState to edit it.
     * @return The edited TextFieldState.
     */
    private fun createStateAndEdit(
        initialText: String,
        initialSelection: TextRange,
        editBlock: TextFieldBuffer.() -> Unit
    ): TextFieldState {
        val state = TextFieldState(initialText, initialSelection)
        state.edit { editBlock(); MarkdownUtils.handleListInput(this) }
        return state
    }

    @Test
    fun `handleListInput continues unordered list with dash`() {
        val testState = createStateAndEdit(
            "- item",
            TextRange(6),
            editBlock = { append("\n") }
        )
        assertEquals("- item\n- ", testState.text.toString())
        assertEquals(TextRange(9), testState.selection)
    }

    @Test
    fun `handleListInput continues unordered list with asterisk`() {
        val testState = createStateAndEdit(
            "* item",
            TextRange(6),
            editBlock = { append("\n") }
        )
        assertEquals("* item\n* ", testState.text.toString())
        assertEquals(TextRange(9), testState.selection)
    }

    @Test
    fun `handleListInput continues indented unordered list`() {
        val testState = createStateAndEdit(
            "  - item",
            TextRange(8),
            editBlock = { append("\n") }
        )
        assertEquals("  - item\n  - ", testState.text.toString())
        assertEquals(TextRange(13), testState.selection)
    }

    @Test
    fun `handleListInput removes empty list item`() {
        val testState = createStateAndEdit(
            "- ",
            TextRange(2),
            editBlock = { append("\n") }
        )
        assertEquals("", testState.text.toString())
        assertEquals(TextRange(0), testState.selection)
    }

    @Test
    fun `handleListInput removes indented empty list item`() {
        val testState = createStateAndEdit(
            "  - ",
            TextRange(4),
            editBlock = { append("\n") }
        )
        assertEquals("", testState.text.toString())
        assertEquals(TextRange(0), testState.selection)
    }

    @Test
    fun `handleListInput splits list item and preserves suffix`() {
        // "- it|em" -> "- it\n- em"
        val testState = createStateAndEdit(
            "- item",
            TextRange(4),
            editBlock = {
                // simulate cursor between t and e and press enter
                replace(selection.start, selection.end, "\n")
            }
        )
        assertEquals("- it\n- em", testState.text.toString())
        assertEquals(TextRange(7), testState.selection)
    }

    @Test
    fun `handleListInput handles tab indentation`() {
        val testState = createStateAndEdit(
            "\t- item",
            TextRange(7),
            editBlock = { append("\n") }
        )
        assertEquals("\t- item\n\t- ", testState.text.toString())
        assertEquals(TextRange(11), testState.selection)
    }

    @Test
    fun `handleListInput does nothing when enter is pressed at very start of bullet line`() {
        // "|- item" -> "\n- item" (should not auto-bullet the new empty line above)
        val testState = createStateAndEdit(
            "- item",
            TextRange(0),
            editBlock = { replace(selection.start, selection.end, "\n") }
        )
        assertEquals("\n- item", testState.text.toString())
        assertEquals(TextRange(1), testState.selection)
    }

    @Test
    fun `handleListInput allows multiple newlines to break list`() {
        // If we are at "- item\n- " and press enter again, it should clear the bullet (covered by empty list item test)
        // But what if we are at "- item\n" and press enter?
        val state = createStateAndEdit(
            "- item\n",
            TextRange(7),
            editBlock = { append("\n") }
        )
        assertEquals("- item\n\n", state.text.toString())
        assertEquals(TextRange(8), state.selection)
    }

    @Test
    fun `handleListInput does nothing on regular text`() {
        val testText = "Regular text -* -- * <+) "
        val state = createStateAndEdit(
            testText,
            TextRange(testText.length),
            editBlock = { append("\n") }
        )
        assertEquals("$testText\n", state.text.toString())
        assertEquals(TextRange(testText.length + 1), state.selection)
    }

    @Test
    fun `handleListInput does not get confused by dash in middle of text`() {
        // "Some - text" -> Enter -> "Some - text\n" (Should not auto-bullet)
        val state = createStateAndEdit(
            "Some - text",
            TextRange(11),
            editBlock = { append("\n") }
        )
        assertEquals("Some - text\n", state.text.toString())
        assertEquals(TextRange(12), state.selection)
    }

    @Test
    fun `handleListInput ignores fake bullet without trailing space`() {
        // "-Text" is not a Markdown list item. " -Text\n" should not trigger.
        val state = createStateAndEdit(
            "-Text",
            TextRange(5),
            editBlock = { append("\n") }
        )
        assertEquals("-Text\n", state.text.toString())
        assertEquals(TextRange(6), state.selection)
    }

    @Test
    fun `handleListInput removes bullet on backspace`() {
        val state = createStateAndEdit(
            "- item",
            TextRange(2),
            editBlock = { replace(selection.start - 1, selection.end, "") }
        )
        assertEquals("item", state.text.toString())
        assertEquals(TextRange(0), state.selection)
    }

    @Test
    fun `handleListInput removes indented bullet on backspace`() {
        val state = createStateAndEdit(
            "  - item",
            TextRange(4),
            editBlock = { replace(selection.start - 1, selection.end, "") }
        )
        assertEquals("item", state.text.toString())
        assertEquals(TextRange(0), state.selection)
    }

    @Test
    fun `handleListInput backspacing in middle of text does not trigger bullet removal`() {
        val state = createStateAndEdit(
            "- item",
            TextRange(4),
            editBlock = { replace(3, 4, "") }
        )
        assertEquals("- iem", state.text.toString())
        assertEquals(TextRange(3), state.selection)
    }

    @Test
    fun `handleListInput does nothing when multiple characters are inserted`() {
        val testState = createStateAndEdit(
            "- item",
            TextRange(6),
            editBlock = { append("\n- second item") }
        )
        assertEquals("- item\n- second item", testState.text.toString())
        assertEquals(TextRange(20), testState.selection)
    }

    @Test
    fun `handleListInput does nothing when multiple characters are deleted`() {
        val testState = createStateAndEdit(
            "- item",
            TextRange(0, 2),
            editBlock = { replace(selection.start, selection.end, "") }
        )
        val testState2 = createStateAndEdit(
            "- item",
            TextRange(1, 6),
            editBlock = { replace(selection.start, selection.end, "") }
        )
        assertEquals("item", testState.text.toString())
        assertEquals(TextRange(0), testState.selection)
        assertEquals("-", testState2.text.toString())
        assertEquals(TextRange(1), testState2.selection)
    }

    @Test
    fun `handleListInput ignores triple dash`() {
        val testText = "--- hi"
        val state = createStateAndEdit(
            testText,
            TextRange(6),
            editBlock = { append("\n") }
        )
        assertEquals("--- hi\n", state.text.toString())
        assertEquals(TextRange(7), state.selection)
    }

    @Test
    fun `handleListInput ignores numbered list`() {
        val testText = "1. item"
        val state = createStateAndEdit(
            testText,
            TextRange(7),
            editBlock = { append("\n") }
        )
        assertEquals("1. item\n", state.text.toString())
        assertEquals(TextRange(8), state.selection)
    }

    @Test
    fun `handleListInput continues list with mixed indentation`() {
        val testState = createStateAndEdit(
            " \t - item",
            TextRange(9),
            editBlock = { append("\n") }
        )
        assertEquals(" \t - item\n \t - ", testState.text.toString())
        assertEquals(TextRange(15), testState.selection)
    }

    @Test
    fun `handleListInput does not remove symbol on backspace if it was not part of a bullet`() {
        val state = createStateAndEdit(
            "-a",
            TextRange(2),
            editBlock = { replace(1, 2, "") }
        )
        assertEquals("-", state.text.toString())
        assertEquals(TextRange(1), state.selection)
    }

    @Test
    fun `handleListInput does nothing when selection is not collapsed`() {
        val state = createStateAndEdit(
            "- item",
            TextRange(3, 5),
            editBlock = { replace(selection.start, selection.end, "\n") }
        )
        assertEquals("- i\nm", state.text.toString())
    }

    @Test
    fun `handleListInput does nothing when multiple separate changes are made`() {
        val testState = createStateAndEdit(
            "- item",
            TextRange(6),
            editBlock = {
                append("\n")
                insert(0, "prefix ")
            }
        )
        // Change count will be 2, so it should not trigger auto-bullet
        assertEquals("prefix - item\n", testState.text.toString())
    }
}

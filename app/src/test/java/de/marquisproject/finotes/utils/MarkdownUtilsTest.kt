package de.marquisproject.finotes.utils

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.text.TextRange
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class MarkdownUtilsTest {

    private fun testInputTransformation(initialText: String, initialSelection: TextRange, newText: String, newSelection: TextRange): TextFieldState {
        val state = TextFieldState(initialText, initialSelection)
        state.edit {
            replace(0, length, newText)
            selection = newSelection
            MarkdownUtils.handleListInput(this)
        }
        return state
    }

    @Test
    fun `handleListInput continues unordered list with dash`() {
        val state = testInputTransformation("- item", TextRange(6), "- item\n", TextRange(7))
        assertEquals("- item\n- ", state.text.toString())
        assertEquals(TextRange(9), state.selection)
    }

    @Test
    fun `handleListInput continues unordered list with asterisk`() {
        val state = testInputTransformation("* item", TextRange(6), "* item\n", TextRange(7))
        assertEquals("* item\n* ", state.text.toString())
        assertEquals(TextRange(9), state.selection)
    }

    @Test
    fun `handleListInput continues indented unordered list`() {
        val state = testInputTransformation("  - item", TextRange(8), "  - item\n", TextRange(9))
        assertEquals("  - item\n  - ", state.text.toString())
        assertEquals(TextRange(13), state.selection)
    }

    @Test
    fun `handleListInput removes empty list item`() {
        val state = testInputTransformation("- ", TextRange(2), "- \n", TextRange(3))
        assertEquals("", state.text.toString())
        assertEquals(TextRange(0), state.selection)
    }

    @Test
    fun `handleListInput removes indented empty list item`() {
        val state = testInputTransformation("  - ", TextRange(4), "  - \n", TextRange(5))
        assertEquals("", state.text.toString())
        assertEquals(TextRange(0), state.selection)
    }

    @Test
    fun `handleListInput does nothing on regular text`() {
        val state = testInputTransformation("Regular text", TextRange(12), "Regular text\n", TextRange(13))
        assertEquals("Regular text\n", state.text.toString())
        assertEquals(TextRange(13), state.selection)
    }

    @Test
    fun `handleListInput does nothing when cursor simply moves to line after list`() {
        // Text ends with a list item and a newline, but the edit didn't JUST add that newline
        val state = TextFieldState("- item\n", TextRange(7))
        state.edit {
            // No changes, just calling the handler
            MarkdownUtils.handleListInput(this)
        }
        assertEquals("- item\n", state.text.toString())
        assertEquals(TextRange(7), state.selection)
    }

    @Test
    fun `handleListInput removes bullet on backspace`() {
        val state = TextFieldState("- item", TextRange(2))
        state.edit {
            replace(1, 2, "") // Delete space
            MarkdownUtils.handleListInput(this)
        }
        assertEquals("item", state.text.toString())
        assertEquals(TextRange(0), state.selection)
    }

    @Test
    fun `handleListInput removes indented bullet on backspace`() {
        val state = TextFieldState("  - item", TextRange(4))
        state.edit {
            replace(3, 4, "") // Delete space
            MarkdownUtils.handleListInput(this)
        }
        assertEquals("item", state.text.toString())
        assertEquals(TextRange(0), state.selection)
    }

    @Test
    fun `bullet regex matches standard dash list`() {
        val regex = Regex("^(\\s*)([-*])\\s")
        val text = "- item"

        val match = regex.find(text)
        assertNotNull(match)
        assertEquals("", match!!.groups[1]!!.value)
        assertEquals("-", match.groups[2]!!.value)
    }

    @Test
    fun `bullet regex matches indented list`() {
        val regex = Regex("^(\\s*)([-*])\\s")
        val text = "  * item"

        val match = regex.find(text)
        assertNotNull(match)
        assertEquals("  ", match!!.groups[1]!!.value)
        assertEquals("*", match.groups[2]!!.value)
    }
}

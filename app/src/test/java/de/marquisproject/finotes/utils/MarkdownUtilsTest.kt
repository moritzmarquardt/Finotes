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
            // Simulate realistic typing by only inserting the difference
            if (newText.length > initialText.length) {
                val addedText = newText.substring(initialSelection.start, initialSelection.start + (newText.length - initialText.length))
                replace(initialSelection.start, initialSelection.end, addedText)
            } else if (newText.length < initialText.length) {
                replace(newSelection.start, initialSelection.start, "")
            }
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
    fun `handleListInput splits list item and preserves suffix`() {
        // "- it|em" -> "- it\n- em"
        val state = testInputTransformation("- item", TextRange(4), "- it\nem", TextRange(5))
        assertEquals("- it\n- em", state.text.toString())
        assertEquals(TextRange(7), state.selection)
    }

    @Test
    fun `handleListInput handles tab indentation`() {
        val state = testInputTransformation("\t- item", TextRange(7), "\t- item\n", TextRange(8))
        assertEquals("\t- item\n\t- ", state.text.toString())
        assertEquals(TextRange(11), state.selection)
    }

    @Test
    fun `handleListInput does nothing when enter is pressed at very start of bullet line`() {
        // "|- item" -> "\n- item" (should not auto-bullet the new empty line above)
        val state = testInputTransformation("- item", TextRange(0), "\n- item", TextRange(1))
        assertEquals("\n- item", state.text.toString())
        assertEquals(TextRange(1), state.selection)
    }

    @Test
    fun `handleListInput allows multiple newlines to break list`() {
        // If we are at "- item\n- " and press enter again, it should clear the bullet (covered by empty list item test)
        // But what if we are at "- item\n" and press enter?
        val state = testInputTransformation("- item\n", TextRange(7), "- item\n\n", TextRange(8))
        assertEquals("- item\n\n", state.text.toString())
    }

    @Test
    fun `handleListInput does nothing on regular text`() {
        val state = testInputTransformation("Regular text", TextRange(12), "Regular text\n", TextRange(13))
        assertEquals("Regular text\n", state.text.toString())
        assertEquals(TextRange(13), state.selection)
    }

    @Test
    fun `handleListInput does not get confused by dash in middle of text`() {
        // "Some - text" -> Enter -> "Some - text\n" (Should not auto-bullet)
        val state = testInputTransformation("Some - text", TextRange(11), "Some - text\n", TextRange(12))
        assertEquals("Some - text\n", state.text.toString())
        assertEquals(TextRange(12), state.selection)
    }

    @Test
    fun `handleListInput ignores fake bullet without trailing space`() {
        // "-Text" is not a markdown list item. " -Text\n" should not trigger.
        val state = testInputTransformation("-Text", TextRange(5), "-Text\n", TextRange(6))
        assertEquals("-Text\n", state.text.toString())
    }

    @Test
    fun `handleListInput preserves the specific bullet symbol used`() {
        // Asterisk should continue with asterisk
        val asteriskState = testInputTransformation("* item", TextRange(6), "* item\n", TextRange(7))
        assertEquals("* item\n* ", asteriskState.text.toString())

        // Dash should continue with dash
        val dashState = testInputTransformation("- item", TextRange(6), "- item\n", TextRange(7))
        assertEquals("- item\n- ", dashState.text.toString())
    }

    @Test
    fun `handleListInput handles already indented text after newline`() {
        // If the user somehow already has indentation on the new line, we should still behave correctly
        val state = TextFieldState("- item\n  ", TextRange(9))
        state.edit {
            MarkdownUtils.handleListInput(this)
        }
        // This is a bit of an edge case for the current implementation which expects to be called immediately after \n
        // But it's good to ensure it doesn't crash or do something wild.
        assertEquals("- item\n  ", state.text.toString())
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
    fun `handleListInput backspacing in middle of text does not trigger bullet removal`() {
        val state = TextFieldState("- item", TextRange(4))
        state.edit {
            replace(3, 4, "") // Delete 't' -> "- iem"
            MarkdownUtils.handleListInput(this)
        }
        assertEquals("- iem", state.text.toString())
        assertEquals(TextRange(3), state.selection)
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

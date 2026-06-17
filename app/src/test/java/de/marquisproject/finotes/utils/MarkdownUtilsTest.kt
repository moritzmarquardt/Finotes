package de.marquisproject.finotes.utils

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownUtilsTest {
    @Test
    fun `handleEnterKey continues unordered list with dash`() {
        // Note: handleEnterKey is called AFTER the newline is inserted by the IME
        // So the input text already contains the newline character
        val text = "- item\n"  // Newline already inserted
        val selection = TextRange(7) // Cursor AFTER the newline (position 7)
        val input = TextFieldValue(text, selection)

        val result = MarkdownUtils.handleEnterKey(input)

        assertEquals("- item\n- ", result.text)
        assertEquals(TextRange(9), result.selection) // Cursor after "- " on new line
    }

    @Test
    fun `handleEnterKey continues unordered list with asterisk`() {
        val text = "* item\n"
        val selection = TextRange(7) // Cursor after the newline
        val input = TextFieldValue(text, selection)

        val result = MarkdownUtils.handleEnterKey(input)

        assertEquals("* item\n* ", result.text)
        assertEquals(TextRange(9), result.selection)
    }

    @Test
    fun `handleEnterKey continues indented unordered list`() {
        val text = "  - item\n"
        val selection = TextRange(9) // Cursor after the newline
        val input = TextFieldValue(text, selection)

        val result = MarkdownUtils.handleEnterKey(input)

        assertEquals("  - item\n  - ", result.text)
        assertEquals(TextRange(13), result.selection)
    }

    @Test
    fun `handleEnterKey removes empty list item`() {
        // "- \n" has length 3, with '\n' at index 2
        val text = "- \n"
        val selection = TextRange(3) // Cursor after the newline
        val input = TextFieldValue(text, selection)

        val result = MarkdownUtils.handleEnterKey(input)

        assertEquals("", result.text)
        assertEquals(TextRange(0), result.selection)
    }

    @Test
    fun `handleEnterKey removes indented empty list item`() {
        // "  - \n" has length 5, with '\n' at index 4
        val text = "  - \n"
        val selection = TextRange(5) // Cursor after the newline
        val input = TextFieldValue(text, selection)

        val result = MarkdownUtils.handleEnterKey(input)

        assertEquals("", result.text)
        assertEquals(TextRange(0), result.selection)
    }

    @Test
    fun `handleEnterKey does nothing on regular text`() {
        val text = "Regular text"
        val selection = TextRange(12)
        val input = TextFieldValue(text, selection)

        val result = MarkdownUtils.handleEnterKey(input)

        // Should only clear composition, text unchanged
        assertEquals("Regular text", result.text)
        assertEquals(selection, result.selection)
    }

    @Test
    fun `handleEnterKey does nothing on empty text`() {
        val input = TextFieldValue("", TextRange(0))

        val result = MarkdownUtils.handleEnterKey(input)

        assertEquals("", result.text)
        assertEquals(TextRange(0), result.selection)
    }

    @Test
    fun `handleEnterKey handles cursor in middle of line`() {
        // "- it\nem" has length 6, with '\n' at index 4
        val text = "- it\nem"  // User inserted newline in middle
        val selection = TextRange(5) // Cursor after newline
        val input = TextFieldValue(text, selection)

        val result = MarkdownUtils.handleEnterKey(input)

        // Should still continue the list based on previous line "- it"
        assertEquals("- it\n- em", result.text)
        // "- " has 2 characters, so selection is 5 + 2 = 7
        assertEquals(TextRange(7), result.selection)
    }

    @Test
    fun `handleEnterKey handles multiple newlines in text`() {
        // "first line\n- item\n" has length 18, with '\n' at index 17
        val text = "first line\n- item\n"
        val selection = TextRange(18) // Cursor AFTER the newline (position 18)
        val input = TextFieldValue(text, selection)

        val result = MarkdownUtils.handleEnterKey(input)

        assertEquals("first line\n- item\n- ", result.text)
        // "- " has 2 characters, so selection is 18 + 2 = 20
        assertEquals(TextRange(20), result.selection)
    }

    @Test
    fun `handleEnterKey preserves existing content after cursor`() {
        val text = "- item\nafter"
        val selection = TextRange(7) // Cursor right after newline (index 6 is '\n')
        val input = TextFieldValue(text, selection)

        val result = MarkdownUtils.handleEnterKey(input)

        // Text after cursor should be preserved
        assertEquals("- item\n- after", result.text)
        // "- " has 2 characters, so selection is 7 + 2 = 9
        assertEquals(TextRange(9), result.selection)
    }

    @Test
    fun `handleEnterKey with only bullet symbol`() {
        val text = "-"
        val selection = TextRange(1)
        val input = TextFieldValue(text, selection)

        val result = MarkdownUtils.handleEnterKey(input)

        // Single dash without space should not trigger list behavior
        // based on the regex ^(\s*)([-*])\s which requires a space after the bullet
        assertEquals("-", result.text)
    }

    @Test
    fun `handleEnterKey with whitespace before bullet`() {
        // "   - item\n" has length 11, with '\n' at index 10
        val text = "   - item\n"
        val selection = TextRange(10) // Cursor after the newline
        val input = TextFieldValue(text, selection)

        val result = MarkdownUtils.handleEnterKey(input)

        assertEquals("   - item\n   - ", result.text)
        // "   - " has 5 characters (3 spaces + dash + space), so selection is 11 + 5 = 16
        assertEquals(TextRange(15), result.selection)
    }

    @Test
    fun `handleEnterKey with asterisk and no space`() {
        val text = "*item"  // No space after asterisk
        val selection = TextRange(5)
        val input = TextFieldValue(text, selection)

        val result = MarkdownUtils.handleEnterKey(input)

        // Should not match because regex requires space after bullet
        assertEquals("*item", result.text)
    }

    @Test
    fun `handleEnterKey with mixed bullet types`() {
        // text = "- first\n* second\n" has length 17, with '\n' at index 16
        val text = "- first\n* second\n"
        val selection = TextRange(17) // Cursor AFTER the newline
        val input = TextFieldValue(text, selection)

        val result = MarkdownUtils.handleEnterKey(input)

        // Should continue with the same bullet type as current line (*)
        assertEquals("- first\n* second\n* ", result.text)
        // "* " has 2 characters, so selection is 17 + 2 = 19
        assertEquals(TextRange(19), result.selection)
    }

    @Test
    fun `handleBackspace removes bullet when cursor at start of list item`() {
        val oldValue = TextFieldValue("- item", TextRange(2))
        val newValue = TextFieldValue("-item", TextRange(1))

        val result = MarkdownUtils.handleBackspace(oldValue, newValue)

        assertEquals("item", result.text)
        assertEquals(TextRange(0), result.selection)
    }

    @Test
    fun `handleBackspace does nothing when not in list`() {
        val oldValue = TextFieldValue("Regular text", TextRange(8))
        val newValue = TextFieldValue("Regulartext", TextRange(7))

        val result = MarkdownUtils.handleBackspace(oldValue, newValue)

        assertEquals(newValue.text, result.text)
        assertEquals(newValue.selection, result.selection)
    }

    @Test
    fun `handleBackspace does nothing at start of text`() {
        val oldValue = TextFieldValue("- item", TextRange(0))
        val newValue = TextFieldValue("- item", TextRange(0)) // No change possible

        val result = MarkdownUtils.handleBackspace(oldValue, newValue)

        assertEquals(oldValue.text, result.text)
        assertEquals(oldValue.selection, result.selection)
    }

    @Test
    fun `handleBackspace does nothing with selection`() {
        val oldValue = TextFieldValue("- item", TextRange(2, 5))
        val newValue = TextFieldValue("- ", TextRange(2)) // Deletion of "item"

        val result = MarkdownUtils.handleBackspace(oldValue, newValue)

        // Should not handle when selection is not collapsed in oldValue
        assertEquals(newValue.text, result.text)
    }

    @Test
    fun `handleBackspace removes indented bullet`() {
        val oldValue = TextFieldValue("  - item", TextRange(4))
        val newValue = TextFieldValue("  -item", TextRange(3))

        val result = MarkdownUtils.handleBackspace(oldValue, newValue)

        assertEquals("item", result.text)
        assertEquals(TextRange(0), result.selection)
    }

    @Test
    fun `handleBackspace removes asterisk bullet`() {
        val oldValue = TextFieldValue("* item", TextRange(2))
        val newValue = TextFieldValue("*item", TextRange(1))

        val result = MarkdownUtils.handleBackspace(oldValue, newValue)

        assertEquals("item", result.text)
        assertEquals(TextRange(0), result.selection)
    }

    @Test
    fun `handleBackspace with partial bullet match at cursor`() {
        val oldValue = TextFieldValue("-item", TextRange(1))
        val newValue = TextFieldValue("item", TextRange(0))

        val result = MarkdownUtils.handleBackspace(oldValue, newValue)

        // Should not match because there's no space after the bullet in oldValue
        assertEquals("item", result.text)
    }

    @Test
    fun `handleBackspace with bullet in middle of line`() {
        val oldValue = TextFieldValue("text - item", TextRange(7))
        val newValue = TextFieldValue("text -item", TextRange(6))

        val result = MarkdownUtils.handleBackspace(oldValue, newValue)

        // Should not match because bullet is not at start of line
        assertEquals("text -item", result.text)
    }

    @Test
    fun `full workflow - create list, add items, remove empty item`() {
        // Start with first item and newline already inserted
        var value = TextFieldValue("- first\n", TextRange(8))

        // Press Enter to continue list
        value = MarkdownUtils.handleEnterKey(value)
        assertEquals("- first\n- ", value.text)
        assertEquals(TextRange(10), value.selection)

        // Type second item (simulating user typing "second")
        value = value.copy(text = "- first\n- second", selection = TextRange(16))
        
        // User presses Enter - newline is added by IME
        val newValue = value.copy(text = "- first\n- second\n", selection = TextRange(17))
        value = MarkdownUtils.handleEnterKey(newValue)
        assertEquals("- first\n- second\n- ", value.text)
        assertEquals(TextRange(19), value.selection)
        
        // Remove it with backspace (user deletes the space of "- ")
        val deletedSpaceValue = value.copy(text = "- first\n- second\n-", selection = TextRange(18))
        value = MarkdownUtils.handleBackspace(value, deletedSpaceValue)
        assertEquals("- first\n- second\n", value.text)
        assertEquals(TextRange(17), value.selection)
    }

    @Test
    fun `complex indented list workflow`() {
        // Start with root item and newline
        var value = TextFieldValue("- root\n", TextRange(7))

        // Add indented child - newline already inserted
        value = MarkdownUtils.handleEnterKey(value)
        // Now we have "- root\n- " but we want to make it indented
        // Simulate user pressing space twice to indent
        value = value.copy(text = "- root\n  - ", selection = TextRange(11))
        // User types "child"
        value = value.copy(text = "- root\n  - child", selection = TextRange(16))
        
        // User presses Enter - newline added by IME
        val newValue = value.copy(text = "- root\n  - child\n", selection = TextRange(17))
        value = MarkdownUtils.handleEnterKey(newValue)
        assertEquals("- root\n  - child\n  - ", value.text)
        assertEquals(TextRange(21), value.selection)

        // Remove the empty indented item (user deletes space of "  - ")
        val deletedSpaceValue = value.copy(text = "- root\n  - child\n  -", selection = TextRange(20))
        value = MarkdownUtils.handleBackspace(value, deletedSpaceValue)
        assertEquals("- root\n  - child\n", value.text)
        assertEquals(TextRange(17), value.selection)
    }

    @Test
    fun `bullet regex matches standard dash list`() {
        val regex = Regex("^(\\s*)([-*])\\s")
        val text = "- item"

        val match = regex.find(text)
        assertNotNull(match)
        assertEquals("", match!!.groups[1]!!.value) // No leading whitespace
        assertEquals("-", match.groups[2]!!.value)  // Dash bullet
    }

    @Test
    fun `bullet regex matches indented list`() {
        val regex = Regex("^(\\s*)([-*])\\s")
        val text = "  * item"

        val match = regex.find(text)
        assertNotNull(match)
        assertEquals("  ", match!!.groups[1]!!.value) // Two spaces
        assertEquals("*", match.groups[2]!!.value)   // Asterisk bullet
    }

    @Test
    fun `bullet regex does not match without trailing space`() {
        val regex = Regex("^(\\s*)([-*])\\s")
        val text = "-item"

        val match = regex.find(text)
        assertTrue(match == null)
    }

    @Test
    fun `bullet regex does not match in middle of text`() {
        val regex = Regex("^(\\s*)([-*])\\s")
        val text = "prefix - item"

        val match = regex.find(text)
        assertTrue(match == null)
    }
}

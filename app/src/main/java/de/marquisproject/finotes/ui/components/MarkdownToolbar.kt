package de.marquisproject.finotes.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.StrikethroughS
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MarkdownToolbar(
    onBoldClick: () -> Unit,
    onItalicClick: () -> Unit,
    onStrikethroughClick: () -> Unit,
    onBulletListClick: () -> Unit,
    onNumberedListClick: () -> Unit,
    onLinkClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = 8.dp),
    ) {
        IconButton(onClick = onBoldClick) {
            Icon(Icons.Default.FormatBold, contentDescription = "Bold")
        }
        IconButton(onClick = onItalicClick) {
            Icon(Icons.Default.FormatItalic, contentDescription = "Italic")
        }
        IconButton(onClick = onStrikethroughClick) {
            Icon(Icons.Default.StrikethroughS, contentDescription = "Strikethrough")
        }
        IconButton(onClick = onBulletListClick) {
            Icon(Icons.Default.FormatListBulleted, contentDescription = "Bullet list")
        }
        IconButton(onClick = onNumberedListClick) {
            Icon(Icons.Default.FormatListNumbered, contentDescription = "Numbered list")
        }
        IconButton(onClick = onLinkClick) {
            Icon(Icons.Default.Link, contentDescription = "Link")
        }
    }
}
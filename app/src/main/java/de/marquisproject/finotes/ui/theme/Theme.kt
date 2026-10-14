package de.marquisproject.finotes.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun FinotesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeVariant: ThemeVariant? = ThemeVariant.AU,
    content: @Composable (() -> Unit)
) {
    val variant = themeVariant ?: ThemeVariant.AU
    val colorPalette = ThemeVariantMap[variant] 
        ?: ThemeVariantMap[ThemeVariant.AU] 
        ?: error("Theme variant $variant and default theme not found")

    val colorScheme = colorPalette.toColorScheme(darkTheme)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

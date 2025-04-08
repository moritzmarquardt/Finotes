package de.marquisproject.finotes.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.material3.lightColorScheme

private fun colourPaletteToLightColorScheme(
    colorPalette: ColourPalette
): ColorScheme {
    return lightColorScheme(
        primary = colorPalette.primaryLight,
        onPrimary = colorPalette.onPrimaryLight,
        primaryContainer = colorPalette.primaryContainerLight,
        onPrimaryContainer = colorPalette.onPrimaryContainerLight,
        secondary = colorPalette.secondaryLight,
        onSecondary = colorPalette.onSecondaryLight,
        secondaryContainer = colorPalette.secondaryContainerLight,
        onSecondaryContainer = colorPalette.onSecondaryContainerLight,
        tertiary = colorPalette.tertiaryLight,
        onTertiary = colorPalette.onTertiaryLight,
        error = colorPalette.errorLight,
        onError = colorPalette.onErrorLight,
        background = colorPalette.backgroundLight,
        onBackground = colorPalette.onBackgroundLight,
        surface = colorPalette.surfaceLight,
        onSurface = colorPalette.onSurfaceLight,
    )
}

private fun colourPaletteToDarkColorScheme(
    colorPalette: ColourPalette
): ColorScheme {
    return darkColorScheme(
        primary = colorPalette.primaryDark,
        onPrimary = colorPalette.onPrimaryDark,
        primaryContainer = colorPalette.primaryContainerDark,
        onPrimaryContainer = colorPalette.onPrimaryContainerDark,
        secondary = colorPalette.secondaryDark,
        onSecondary = colorPalette.onSecondaryDark,
        secondaryContainer = colorPalette.secondaryContainerDark,
        onSecondaryContainer = colorPalette.onSecondaryContainerDark,
        tertiary = colorPalette.tertiaryDark,
        onTertiary = colorPalette.onTertiaryDark,
        error = colorPalette.errorDark,
        onError = colorPalette.onErrorDark,
        background = colorPalette.backgroundDark,
        onBackground = colorPalette.onBackgroundDark,
        surface = colorPalette.surfaceDark,
        onSurface = colorPalette.onSurfaceDark,
    )
}


@Composable
fun FinotesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeVariant: ThemeVariant? = ThemeVariant.FIONA,
    content: @Composable (() -> Unit)
) {
    val lightColorSchemeVariant = when (themeVariant) {
        ThemeVariant.FIONA -> colourPaletteToLightColorScheme(ThemeVariantMap[ThemeVariant.FIONA] ?: error("Fiona theme not found"))
        ThemeVariant.MARQUI -> colourPaletteToLightColorScheme(ThemeVariantMap[ThemeVariant.MARQUI] ?: error("Marqui theme not found"))
        ThemeVariant.MINION -> colourPaletteToLightColorScheme(ThemeVariantMap[ThemeVariant.MINION] ?: error("Minion theme not found"))
        null -> colourPaletteToLightColorScheme(ThemeVariantMap[ThemeVariant.FIONA] ?: error("Fiona theme not found"))
        ThemeVariant.MATERIAL -> colourPaletteToLightColorScheme(ThemeVariantMap[ThemeVariant.MATERIAL] ?: error("Material theme not found"))
    }
    val darkColorSchemeVariant = when (themeVariant) {
        ThemeVariant.FIONA -> colourPaletteToDarkColorScheme(ThemeVariantMap[ThemeVariant.FIONA] ?: error("Fiona theme not found"))
        ThemeVariant.MARQUI -> colourPaletteToDarkColorScheme(ThemeVariantMap[ThemeVariant.MARQUI] ?: error("Marqui theme not found"))
        ThemeVariant.MINION -> colourPaletteToDarkColorScheme(ThemeVariantMap[ThemeVariant.MINION] ?: error("Minion theme not found"))
        null -> colourPaletteToDarkColorScheme(ThemeVariantMap[ThemeVariant.FIONA] ?: error("Fiona theme not found"))
        ThemeVariant.MATERIAL -> colourPaletteToDarkColorScheme(ThemeVariantMap[ThemeVariant.MATERIAL] ?: error("Material theme not found"))
    }
    val colorScheme = when {
        darkTheme -> darkColorSchemeVariant
        else -> lightColorSchemeVariant
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
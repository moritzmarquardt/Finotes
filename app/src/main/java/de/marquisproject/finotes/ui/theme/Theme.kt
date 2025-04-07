package de.marquisproject.finotes.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.material3.lightColorScheme



/*private fun colourPaletteToLightColorScheme(
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
        tertiaryContainer = colorPalette.tertiaryContainerLight,
        onTertiaryContainer = colorPalette.onTertiaryContainerLight,
        error = colorPalette.errorLight,
        onError = colorPalette.onErrorLight,
        errorContainer = colorPalette.errorContainerLight,
        onErrorContainer = colorPalette.onErrorContainerLight,
        background = colorPalette.backgroundLight,
        onBackground = colorPalette.onBackgroundLight,
        surface = colorPalette.surfaceLight,
        onSurface = colorPalette.onSurfaceLight,
        surfaceVariant = colorPalette.surfaceVariantLight,
        onSurfaceVariant = colorPalette.onSurfaceVariantLight,
        outline = colorPalette.outlineLight,
        outlineVariant = colorPalette.outlineVariantLight,
        scrim = colorPalette.scrimLight,
        inverseSurface = colorPalette.inverseSurfaceLight,
        inverseOnSurface = colorPalette.inverseOnSurfaceLight,
        inversePrimary = colorPalette.inversePrimaryLight,
        surfaceDim = colorPalette.surfaceDimLight,
        surfaceBright = colorPalette.surfaceBrightLight,
        surfaceContainerLowest = colorPalette.surfaceContainerLowestLight,
        surfaceContainerLow = colorPalette.surfaceContainerLowLight,
        surfaceContainer = colorPalette.surfaceContainerLight,
        surfaceContainerHigh = colorPalette.surfaceContainerHighLight,
        surfaceContainerHighest = colorPalette.surfaceContainerHighestLight,
    )
}*/

private fun colourPaletteToLightColorScheme(
    colorPalette: ColourPalette
): ColorScheme {
    return lightColorScheme(
        primary = colorPalette.primaryLight,
        secondary = colorPalette.secondaryLight,
        tertiary = colorPalette.tertiaryLight,
        error = colorPalette.errorLight,
        background = colorPalette.backgroundLight,
        surface = colorPalette.surfaceLight,
    )
}

private fun colourPaletteToDarkColorScheme(
    colorPalette: ColourPalette
): ColorScheme {
    return darkColorScheme(
        primary = colorPalette.primaryDark,
        secondary = colorPalette.secondaryDark,
        tertiary = colorPalette.tertiaryDark,
        error = colorPalette.errorDark,
        background = colorPalette.backgroundDark,
        surface = colorPalette.surfaceDark,
    )
}


/*private fun colourPaletteToDarkColorScheme(
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
        tertiaryContainer = colorPalette.tertiaryContainerDark,
        onTertiaryContainer = colorPalette.onTertiaryContainerDark,
        error = colorPalette.errorDark,
        onError = colorPalette.onErrorDark,
        errorContainer = colorPalette.errorContainerDark,
        onErrorContainer = colorPalette.onErrorContainerDark,
        background = colorPalette.backgroundDark,
        onBackground = colorPalette.onBackgroundDark,
        surface = colorPalette.surfaceDark,
        onSurface = colorPalette.onSurfaceDark,
        surfaceVariant = colorPalette.surfaceVariantDark,
        onSurfaceVariant = colorPalette.onSurfaceVariantDark,
        outline = colorPalette.outlineDark,
        outlineVariant = colorPalette.outlineVariantDark,
        scrim = colorPalette.scrimDark,
        inverseSurface = colorPalette.inverseSurfaceDark,
        inverseOnSurface = colorPalette.inverseOnSurfaceDark,
        inversePrimary = colorPalette.inversePrimaryDark,
        surfaceDim = colorPalette.surfaceDimDark,
        surfaceBright = colorPalette.surfaceBrightDark,
        surfaceContainerLowest = colorPalette.surfaceContainerLowestDark,
        surfaceContainerLow = colorPalette.surfaceContainerLowDark,
        surfaceContainer = colorPalette.surfaceContainerDark,
        surfaceContainerHigh = colorPalette.surfaceContainerHighDark,
        surfaceContainerHighest = colorPalette.surfaceContainerHighestDark,
    )
}*/


@Composable
fun FinotesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeVariant: ThemeVariant? = ThemeVariant.FIONA,
    content: @Composable (() -> Unit)
) {
    //val darkColorSchemeVariant = colourPaletteToDarkColorScheme(ThemeVariantMap[themeVariant] ?: error("Theme not found"))
    //val lightColorSchemeVariant = colourPaletteToLightColorScheme(ThemeVariantMap[themeVariant] ?: error("Theme not found"))
    val lightColorSchemeVariant = when (themeVariant) {
        ThemeVariant.FIONA -> colourPaletteToLightColorScheme(ThemeVariantMap[ThemeVariant.FIONA] ?: error("Fiona theme not found"))
        ThemeVariant.MARQUI -> colourPaletteToLightColorScheme(ThemeVariantMap[ThemeVariant.MARQUI] ?: error("Marqui theme not found"))
        ThemeVariant.MINION -> colourPaletteToLightColorScheme(ThemeVariantMap[ThemeVariant.MINION] ?: error("Minion theme not found"))
        null -> colourPaletteToLightColorScheme(ThemeVariantMap[ThemeVariant.FIONA] ?: error("Fiona theme not found"))
    }
    val darkColorSchemeVariant = when (themeVariant) {
        ThemeVariant.FIONA -> colourPaletteToDarkColorScheme(ThemeVariantMap[ThemeVariant.FIONA] ?: error("Fiona theme not found"))
        ThemeVariant.MARQUI -> colourPaletteToDarkColorScheme(ThemeVariantMap[ThemeVariant.MARQUI] ?: error("Marqui theme not found"))
        ThemeVariant.MINION -> colourPaletteToDarkColorScheme(ThemeVariantMap[ThemeVariant.MINION] ?: error("Minion theme not found"))
        null -> colourPaletteToDarkColorScheme(ThemeVariantMap[ThemeVariant.FIONA] ?: error("Fiona theme not found"))
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
package de.marquisproject.finotes.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

enum class ThemeVariant {
    FIONA, // inspired by Fiona
    MARQUI, // inspired by Marqui
    MINION, // inspired by Minions
    MATERIAL, // Material 3 standard by Google
}

data class ColourPalette(
    val name : String,

    val primaryLight: Color,
    val onPrimaryLight: Color,
    val primaryContainerLight: Color,
    val onPrimaryContainerLight: Color,
    val secondaryLight: Color,
    val onSecondaryLight: Color,
    val secondaryContainerLight: Color,
    val onSecondaryContainerLight: Color,
    val tertiaryLight: Color,
    val onTertiaryLight: Color,
    val errorLight: Color,
    val onErrorLight: Color,
    val backgroundLight: Color,
    val onBackgroundLight: Color,
    val surfaceLight: Color,
    val onSurfaceLight: Color,

    val primaryDark: Color,
    val onPrimaryDark: Color,
    val primaryContainerDark: Color,
    val onPrimaryContainerDark: Color,
    val secondaryContainerDark: Color,
    val onSecondaryContainerDark: Color,
    val secondaryDark: Color,
    val onSecondaryDark: Color,
    val tertiaryDark: Color,
    val onTertiaryDark: Color,
    val errorDark: Color,
    val onErrorDark: Color,
    val backgroundDark: Color,
    val onBackgroundDark: Color,
    val surfaceDark: Color,
    val onSurfaceDark: Color,
)


val FionaColorPalette = ColourPalette(
    name = "Fiona's Theme",

    primaryLight = Color(0xFFFDD084),
    onPrimaryLight = Color(0xFF000000),
    primaryContainerLight = Color(0xFF26C6DA),
    onPrimaryContainerLight = Color(0xFF26C6DA),
    secondaryLight = Color(0xFF762E1F),
    onSecondaryLight = Color(0xFFFFFFFF),
    secondaryContainerLight = Color(0xFFFFDF00),
    onSecondaryContainerLight = Color(0xFF26C6DA),
    tertiaryLight = Color(0xFFCE7745),
    onTertiaryLight = Color(0xFF000000),
    errorLight = Color(0xFFFF9E9E),
    onErrorLight = Color(0xFF600101),
    backgroundLight = Color(0xFFFFF8F4),
    onBackgroundLight = Color(0xFF1F1B17),
    surfaceLight = Color(0xFFE7D2A8),
    onSurfaceLight = Color(0xFF1F1B17),

    primaryDark = Color(0xFFFDD084),
    onPrimaryDark = Color(0xFF472A00),
    primaryContainerDark = Color(0xFF26C6DA),
    onPrimaryContainerDark = Color(0xFF26C6DA),
    secondaryDark = Color(0xFF762E1F),
    onSecondaryDark = Color(0xFFEEDFD5),
    secondaryContainerDark = Color(0xFF776E13),
    onSecondaryContainerDark = Color(0xFF26C6DA),
    tertiaryDark = Color(0xFFBB9370),
    onTertiaryDark = Color(0xFF2E3301),
    errorDark = Color(0xFFFFB4AB),
    onErrorDark = Color(0xFF690005),
    backgroundDark = Color(0xFF17130F),
    onBackgroundDark = Color(0xFFEAE1D9),
    surfaceDark = Color(0xFF1E1A11),
    onSurfaceDark = Color(0xFFEAE1D9),
)


val MarquiColorPalette = ColourPalette(
    name = "Marqui's Theme",

    primaryLight = Color(0xFF8F4952),
    onPrimaryLight = Color(0xFFFFFFFF),
    primaryContainerLight = Color(0xFFFFDADC),
    onPrimaryContainerLight = Color(0xFF72333B),
    secondaryLight = Color(0xFF2A6A47),
    onSecondaryLight = Color(0xFFFFFFFF),
    secondaryContainerLight = Color(0xFFB7E8D8),
    onSecondaryContainerLight = Color(0xFF00391F),
    tertiaryLight = Color(0xFF576422),
    onTertiaryLight = Color(0xFFFFFFFF),
    errorLight = Color(0xFFFF9E9E),
    onErrorLight = Color(0xFF7E0000),
    backgroundLight = Color(0xFFFFF8F7),
    onBackgroundLight = Color(0xFF22191A),
    surfaceLight = Color(0xFFFFF2F1),
    onSurfaceLight = Color(0xFF22191A),

    primaryDark = Color(0xFFFFB2B9),
    onPrimaryDark = Color(0xFF561D26),
    primaryContainerDark = Color(0xFF72333B),
    onPrimaryContainerDark = Color(0xFFFFDADC),
    secondaryDark = Color(0xFF93D5AA),
    onSecondaryDark = Color(0xFF00391F),
    secondaryContainerDark = Color(0xFF004D3A),
    onSecondaryContainerDark = Color(0xFFB7E8D8),
    tertiaryDark = Color(0xFFBECE7F),
    onTertiaryDark = Color(0xFF2A3400),
    errorDark = Color(0xFFFFB4AB),
    onErrorDark = Color(0xFF690005),
    backgroundDark = Color(0xFF0C0A0A),
    onBackgroundDark = Color(0xFFF0DEDF),
    surfaceDark = Color(0xFF1A1112),
    onSurfaceDark = Color(0xFFF0DEDF),
)




val MinionColorPalette = ColourPalette(
    name = "Minion's Theme",

    primaryLight = Color(0xFFFBDF69),
    onPrimaryLight = Color(0xFF262626),
    primaryContainerLight = Color(0xFFFFE240),
    onPrimaryContainerLight = Color(0xFF736400),
    secondaryLight = Color(0xFF395B75),
    onSecondaryLight = Color(0xFFFFFFFF),
    secondaryContainerLight = Color(0xFFB7D6E1),
    onSecondaryContainerLight = Color(0xFF001E2D),
    tertiaryLight = Color(0xFF1E2F3B),
    onTertiaryLight = Color(0xFFFFFFFF),
    errorLight = Color(0xFFBA1A1A),
    onErrorLight = Color(0xFFFFFFFF),
    backgroundLight = Color(0xFFFFF9EC),
    onBackgroundLight = Color(0xFF1E1C11),
    surfaceLight = Color(0xFFFFF9EC),
    onSurfaceLight = Color(0xFF1E1C11),

    primaryDark = Color(0xFFFBDF69),
    onPrimaryDark = Color(0xFF393000),
    primaryContainerDark = Color(0xFFFFE240),
    onPrimaryContainerDark = Color(0xFF736400),
    secondaryDark = Color(0xFFA8CBE9),
    onSecondaryDark = Color(0xFF0B334C),
    secondaryContainerDark = Color(0xFF1F475E),
    onSecondaryContainerDark = Color(0xFFB7D6E1),
    tertiaryDark = Color(0xFFB7C9D9),
    onTertiaryDark = Color(0xFF21323F),
    errorDark = Color(0xFFFFB4AB),
    onErrorDark = Color(0xFF690005),
    backgroundDark = Color(0xFF16130A),
    onBackgroundDark = Color(0xFFE9E2D1),
    surfaceDark = Color(0xFF16130A),
    onSurfaceDark = Color(0xFFE9E2D1),
)

val standardMaterialLightScheme = lightColorScheme()
val standardMaterialDarkScheme = darkColorScheme()

val MaterialColorPalette = ColourPalette(
    name = "Material's Theme",

    primaryLight = standardMaterialLightScheme.primary,
    onPrimaryLight = standardMaterialLightScheme.onPrimary,
    primaryContainerLight = standardMaterialLightScheme.primaryContainer,
    onPrimaryContainerLight = standardMaterialLightScheme.onPrimaryContainer,
    secondaryLight = standardMaterialLightScheme.secondary,
    onSecondaryLight = standardMaterialLightScheme.onSecondary,
    secondaryContainerLight = standardMaterialLightScheme.secondaryContainer,
    onSecondaryContainerLight = standardMaterialLightScheme.onSecondaryContainer,
    tertiaryLight = standardMaterialLightScheme.tertiary,
    onTertiaryLight = standardMaterialLightScheme.onTertiary,
    errorLight = standardMaterialLightScheme.error,
    onErrorLight = standardMaterialLightScheme.onError,
    backgroundLight = standardMaterialLightScheme.background,
    onBackgroundLight = standardMaterialLightScheme.onBackground,
    surfaceLight = standardMaterialLightScheme.surface,
    onSurfaceLight = standardMaterialLightScheme.onSurface,

    primaryDark = standardMaterialDarkScheme.primary,
    onPrimaryDark = standardMaterialDarkScheme.onPrimary,
    primaryContainerDark = standardMaterialDarkScheme.primaryContainer,
    onPrimaryContainerDark = standardMaterialDarkScheme.onPrimaryContainer,
    secondaryDark = standardMaterialDarkScheme.secondary,
    onSecondaryDark = standardMaterialDarkScheme.onSecondary,
    secondaryContainerDark = standardMaterialDarkScheme.secondaryContainer,
    onSecondaryContainerDark = standardMaterialDarkScheme.onSecondaryContainer,
    tertiaryDark = standardMaterialDarkScheme.tertiary,
    onTertiaryDark = standardMaterialDarkScheme.onTertiary,
    errorDark = standardMaterialDarkScheme.error,
    onErrorDark = standardMaterialDarkScheme.onError,
    backgroundDark = standardMaterialDarkScheme.background,
    onBackgroundDark = standardMaterialDarkScheme.onBackground,
    surfaceDark = standardMaterialDarkScheme.surface,
    onSurfaceDark = standardMaterialDarkScheme.onSurface,
)

// create data struc that has for each variant the enum and the colour palette
val ThemeVariantMap: Map<ThemeVariant, ColourPalette> = mapOf(
    ThemeVariant.FIONA to FionaColorPalette,
    ThemeVariant.MARQUI to MarquiColorPalette,
    ThemeVariant.MINION to MinionColorPalette,
    ThemeVariant.MATERIAL to MaterialColorPalette,
)
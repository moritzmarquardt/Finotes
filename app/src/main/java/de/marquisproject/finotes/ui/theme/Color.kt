package de.marquisproject.finotes.ui.theme

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import de.marquisproject.finotes.data.notes.model.Note
import de.marquisproject.finotes.ui.components.TopBarHome
import de.marquisproject.finotes.ui.components.NoteCard

val primaryLight = Color(0xFF8F4952)
val onPrimaryLight = Color(0xFFFFFFFF)
val primaryContainerLight = Color(0xFFFFDADC)
val onPrimaryContainerLight = Color(0xFF72333B)

val secondaryLight = Color(0xFF2A6A47)
val onSecondaryLight = Color(0xFFFFFFFF)
val secondaryContainerLight = Color(0xFFAFF2C4)
val onSecondaryContainerLight = Color(0xFF0B5130)

val tertiaryLight = Color(0xFF576422)
val onTertiaryLight = Color(0xFFFFFFFF)
val tertiaryContainerLight = Color(0xFFDAEA99)
val onTertiaryContainerLight = Color(0xFF3F4C0A)

val errorLight = Color(0xFFBA1A1A)
val onErrorLight = Color(0xFFFFFFFF)
val errorContainerLight = Color(0xFFFFDAD6)
val onErrorContainerLight = Color(0xFF93000A)

val backgroundLight = Color(0xFFFFF8F7)
val onBackgroundLight = Color(0xFF22191A)

val surfaceLight = Color(0xFFFFF8F7)
val onSurfaceLight = Color(0xFF22191A)

val surfaceVariantLight = Color(0xFFF4DDDE)
val onSurfaceVariantLight = Color(0xFF524344)

val outlineLight = Color(0xFFD5DAB4)
val outlineVariantLight = Color(0xFFD7C1C2)

val scrimLight = Color(0xFF000000)
val inverseSurfaceLight = Color(0xFF382E2E)
val inverseOnSurfaceLight = Color(0xFFFFEDED)
val inversePrimaryLight = Color(0xFFFFB2B9)
val surfaceDimLight = Color(0xFFE7D6D6)
val surfaceBrightLight = Color(0xFFFFF8F7)
val surfaceContainerLowestLight = Color(0xFFFFFFFF)
val surfaceContainerLowLight = Color(0xFFFFF0F0)
val surfaceContainerLight = Color(0xFFFCEAEA)
val surfaceContainerHighLight = Color(0xFFF6E4E4)
val surfaceContainerHighestLight = Color(0xFFF0DEDF)



val primaryDark = Color(0xFFFFB2B9)
val onPrimaryDark = Color(0xFF561D26)
val primaryContainerDark = Color(0xFF72333B)
val onPrimaryContainerDark = Color(0xFFFFDADC)

val secondaryDark = Color(0xFF93D5AA)
val onSecondaryDark = Color(0xFF00391F)
val secondaryContainerDark = Color(0xFF0B5130)
val onSecondaryContainerDark = Color(0xFFAFF2C4)

val tertiaryDark = Color(0xFFBECE7F)
val onTertiaryDark = Color(0xFF2A3400)
val tertiaryContainerDark = Color(0xFF3F4C0A)
val onTertiaryContainerDark = Color(0xFFDAEA99)

val errorDark = Color(0xFFFFB4AB)
val onErrorDark = Color(0xFF690005)
val errorContainerDark = Color(0xFF93000A)
val onErrorContainerDark = Color(0xFFFFDAD6)

val backgroundDark = Color(0xFF0C0A0A)
val onBackgroundDark = Color(0xFFF0DEDF)
val surfaceDark = Color(0xFF1A1112)
val onSurfaceDark = Color(0xFFF0DEDF)
val surfaceVariantDark = Color(0xFF524344)
val onSurfaceVariantDark = Color(0xFFD7C1C2)
val outlineDark = Color(0xFF9F8C8D)
val outlineVariantDark = Color(0xFF524344)
val scrimDark = Color(0xFF000000)
val inverseSurfaceDark = Color(0xFFF0DEDF)
val inverseOnSurfaceDark = Color(0xFF382E2E)
val inversePrimaryDark = Color(0xFF8F4952)
val surfaceDimDark = Color(0xFF1A1112)
val surfaceBrightDark = Color(0xFF413737)
val surfaceContainerLowestDark = Color(0xFF140C0D)
val surfaceContainerLowDark = Color(0xFF22191A)
val surfaceContainerDark = Color(0xFF271D1E)
val surfaceContainerHighDark = Color(0xFF312828)
val surfaceContainerHighestDark = Color(0xFF3D3233)
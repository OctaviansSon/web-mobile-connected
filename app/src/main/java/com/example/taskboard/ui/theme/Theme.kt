package com.example.taskboard.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

// "Terminal" palette: deep navy + teal / blue / amber accents.
private val DevDark = darkColorScheme(
    primary = Color(0xFF2DD4BF), onPrimary = Color(0xFF00352E),
    primaryContainer = Color(0xFF0F3D3A), onPrimaryContainer = Color(0xFFB6F3EA),
    secondary = Color(0xFF60A5FA), onSecondary = Color(0xFF00264D),
    secondaryContainer = Color(0xFF16304F), onSecondaryContainer = Color(0xFFD3E4FF),
    tertiary = Color(0xFFFBBF24), onTertiary = Color(0xFF3B2A00),
    tertiaryContainer = Color(0xFF4A3500), onTertiaryContainer = Color(0xFFFFE9A8),
    error = Color(0xFFF87171), errorContainer = Color(0xFF5A1D1D), onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0B1220), onBackground = Color(0xFFE5E9F0),
    surface = Color(0xFF0B1220), onSurface = Color(0xFFE5E9F0),
    surfaceVariant = Color(0xFF172033), onSurfaceVariant = Color(0xFFA9B4C7),
    outline = Color(0xFF45516A),
    surfaceContainerLowest = Color(0xFF080E1A), surfaceContainerLow = Color(0xFF0F1727),
    surfaceContainer = Color(0xFF121B2D), surfaceContainerHigh = Color(0xFF16213A),
    surfaceContainerHighest = Color(0xFF1B2744)
)

private val DevLight = lightColorScheme(
    primary = Color(0xFF0F766E), onPrimary = Color.White,
    primaryContainer = Color(0xFFCCFBF1), onPrimaryContainer = Color(0xFF073B36),
    secondary = Color(0xFF1D4ED8), onSecondary = Color.White,
    secondaryContainer = Color(0xFFDBEAFE), onSecondaryContainer = Color(0xFF0B2A6B),
    tertiary = Color(0xFFB45309), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFEF3C7), onTertiaryContainer = Color(0xFF5A3A00),
    error = Color(0xFFB91C1C), errorContainer = Color(0xFFFEE2E2), onErrorContainer = Color(0xFF5B1010),
    background = Color(0xFFF6F8FB), onBackground = Color(0xFF0F172A),
    surface = Color(0xFFF6F8FB), onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE3E9F1), onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFF94A3B8),
    surfaceContainerLowest = Color.White, surfaceContainerLow = Color(0xFFF1F5F9),
    surfaceContainer = Color(0xFFEAF0F6), surfaceContainerHigh = Color(0xFFE2EAF2),
    surfaceContainerHighest = Color(0xFFDCE5EE)
)

private val DevMono = FontFamily.Monospace
private val DevBase = Typography()
private val DevTypography = Typography(
    displayMedium = DevBase.displayMedium.copy(fontFamily = DevMono, fontWeight = FontWeight.Bold),
    headlineMedium = DevBase.headlineMedium.copy(fontFamily = DevMono, fontWeight = FontWeight.Bold),
    titleLarge = DevBase.titleLarge.copy(fontFamily = DevMono, fontWeight = FontWeight.Bold),
    titleMedium = DevBase.titleMedium.copy(fontFamily = DevMono, fontWeight = FontWeight.SemiBold),
    titleSmall = DevBase.titleSmall.copy(fontFamily = DevMono, fontWeight = FontWeight.SemiBold)
)

@Composable
fun TaskBoardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DevDark else DevLight,
        typography = DevTypography,
        content = content
    )
}
package com.jarves.mh.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

// ═══════════════════════════════════════════════════════════════════════════════
// BRAND COLORS — Core palette
// ═══════════════════════════════════════════════════════════════════════════════
val PocketOrange = Color(0xFFF28C52)
val PocketOrangeDeep = Color(0xFFD97734)
val PocketOrangeLight = Color(0xFFFFB88A)
val PocketBlue = Color(0xFF8EA8FF)
val PocketBlueLight = Color(0xFFB8CCFF)
val PocketGreen = Color(0xFF69D69E)
val PocketGreenLight = Color(0xFF9AEEB8)
val PocketRed = Color(0xFFFF6B6B)
val PocketPurple = Color(0xFFB8A8FF)
val PocketYellow = Color(0xFFFFD93D)

// ═══════════════════════════════════════════════════════════════════════════════
// SEMANTIC COLOR ROLES — Dark theme (primary)
// ═══════════════════════════════════════════════════════════════════════════════
private val DarkColors = darkColorScheme(
    // Primary — Brand orange
    primary = PocketOrange,
    onPrimary = Color(0xFF241107),
    primaryContainer = Color(0xFF42281D),
    onPrimaryContainer = Color(0xFFFFDDCC),

    // Secondary — Brand blue
    secondary = PocketBlue,
    onSecondary = Color(0xFF001F58),
    secondaryContainer = Color(0xFF1A2A5A),
    onSecondaryContainer = Color(0xFFD6E4FF),

    // Tertiary — Brand green
    tertiary = PocketGreen,
    onTertiary = Color(0xFF00391E),
    tertiaryContainer = Color(0xFF1A4A2D),
    onTertiaryContainer = Color(0xFFB8F5CC),

    // Error
    error = PocketRed,
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFF5C1A1A),
    onErrorContainer = Color(0xFFFFDADA),

    // Background / Surface
    background = Color(0xFF0A0D12),       // Deeper, richer background
    onBackground = Color(0xFFE8EDF2),
    surface = Color(0xFF11161D),          // Card/surface base
    onSurface = Color(0xFFE8EDF2),
    surfaceVariant = Color(0xFF181E27),   // Subtle variant
    onSurfaceVariant = Color(0xFF9BA3AC),
    surfaceContainer = Color(0xFF151B24), // Elevated surface
    surfaceContainerLow = Color(0xFF131821),
    surfaceContainerHigh = Color(0xFF1A212C),
    surfaceContainerHighest = Color(0xFF1D2530),

    // Outline
    outline = Color(0xFF2E3744),
    outlineVariant = Color(0xFF252C38),

    // Scrim / Shadow
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFE8EDF2),
    inverseOnSurface = Color(0xFF11161D),
    inversePrimary = Color(0xFFD97734),
)

// ═══════════════════════════════════════════════════════════════════════════════
// SEMANTIC COLOR ROLES — Light theme
// ═══════════════════════════════════════════════════════════════════════════════
private val LightColors = lightColorScheme(
    primary = Color(0xFFD85A20),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFE0D2),
    onPrimaryContainer = Color(0xFF451A08),

    secondary = Color(0xFF3366CC),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8EEFF),
    onSecondaryContainer = Color(0xFF001F58),

    tertiary = Color(0xFF1B8A5A),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD2F5DD),
    onTertiaryContainer = Color(0xFF00391E),

    error = Color(0xFFDC2626),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFE8E8),
    onErrorContainer = Color(0xFF5C1A1A),

    background = Color(0xFFF7F9FC),
    onBackground = Color(0xFF1A1D21),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1D21),
    surfaceVariant = Color(0xFFEBEFF5),
    onSurfaceVariant = Color(0xFF5A616A),
    surfaceContainer = Color(0xFFF1F4F8),
    surfaceContainerLow = Color(0xFFFAFCFF),
    surfaceContainerHigh = Color(0xFFE8EEF4),
    surfaceContainerHighest = Color(0xFFDDE4EC),

    outline = Color(0xFFC9D0D9),
    outlineVariant = Color(0xFFD8DEE4),

    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFF1A1D21),
    inverseOnSurface = Color(0xFFF7F9FC),
    inversePrimary = Color(0xFFF28C52),
)

// ═══════════════════════════════════════════════════════════════════════════════
// TYPOGRAPHY — Material 3 type scale with custom adjustments
// ═══════════════════════════════════════════════════════════════════════════════
val AppTypography = androidx.compose.material3.Typography(
    displayLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = -0.25.sp,
    ),
    displayMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp,
    ),
    displaySmall = androidx.compose.ui.text.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp,
    ),

    headlineLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp,
    ),
    headlineMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
    ),
    headlineSmall = androidx.compose.ui.text.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
    ),

    titleLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    titleSmall = androidx.compose.ui.text.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),

    bodyLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    bodyMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    bodySmall = androidx.compose.ui.text.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),

    labelLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = androidx.compose.ui.text.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)

// ═══════════════════════════════════════════════════════════════════════════════
// SHAPES — Consistent corner radius system
// ═══════════════════════════════════════════════════════════════════════════════
val AppShapes = androidx.compose.material3.Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
)

// ═══════════════════════════════════════════════════════════════════════════════
// ELEVATION — Consistent elevation tokens
// ═══════════════════════════════════════════════════════════════════════════════
val ElevationSm = 1.dp
val ElevationMd = 2.dp
val ElevationLg = 4.dp
val ElevationXl = 8.dp
val Elevation2xl = 16.dp

// ═══════════════════════════════════════════════════════════════════════════════
// SPACING — Consistent spacing scale (4dp base)
// ═══════════════════════════════════════════════════════════════════════════════
val SpaceXs = 4.dp
val SpaceSm = 8.dp
val SpaceMd = 12.dp
val SpaceLg = 16.dp
val SpaceXl = 20.dp
val Space2xl = 24.dp
val Space3xl = 32.dp
val Space4xl = 40.dp

// ═══════════════════════════════════════════════════════════════════════════════
// RADIUS — Consistent border radius tokens
// ═══════════════════════════════════════════════════════════════════════════════
val RadiusXs = 4.dp
val RadiusSm = 8.dp
val RadiusMd = 12.dp
val RadiusLg = 16.dp
val RadiusXl = 20.dp
val Radius2xl = 24.dp
val Radius3xl = 28.dp
val RadiusPill = 999.dp

// ═══════════════════════════════════════════════════════════════════════════════
// DURATION — Consistent animation durations
// ═══════════════════════════════════════════════════════════════════════════════
val DurationXs = 50
val DurationSm = 150
val DurationMd = 250
val DurationLg = 350
val DurationXl = 500

// ═══════════════════════════════════════════════════════════════════════════════
// MAIN THEME COMPOSABLE
// ═══════════════════════════════════════════════════════════════════════════════
enum class AppThemeMode { SYSTEM, DARK, LIGHT }

@Composable
fun PocketTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val isDark = when (themeMode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !isDark
            insetsController.isAppearanceLightNavigationBars = !isDark
        }
    }

    MaterialTheme(
        colorScheme = if (isDark) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
package com.jarves.mh.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox as M3TooltipBox
import androidx.compose.material3.TooltipState
import androidx.compose.material3.TooltipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarves.mh.ui.theme.ElevationLg
import com.jarves.mh.ui.theme.ElevationMd
import com.jarves.mh.ui.theme.ElevationSm
import com.jarves.mh.ui.theme.ElevationXl
import com.jarves.mh.ui.theme.PocketGreen
import com.jarves.mh.ui.theme.PocketOrange
import com.jarves.mh.ui.theme.PocketOrangeDeep
import com.jarves.mh.ui.theme.RadiusLg
import com.jarves.mh.ui.theme.RadiusMd
import com.jarves.mh.ui.theme.RadiusPill
import com.jarves.mh.ui.theme.RadiusSm
import com.jarves.mh.ui.theme.RadiusXl
import com.jarves.mh.ui.theme.SpaceLg
import com.jarves.mh.ui.theme.SpaceMd
import com.jarves.mh.ui.theme.SpaceSm
import com.jarves.mh.ui.theme.SpaceXl
import com.jarves.mh.ui.theme.SpaceXs
import com.jarves.mh.ui.theme.Space2xl
import com.jarves.mh.ui.theme.Space3xl
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.ui.unit.Dp

// ════════════════════════════════════════════════════════════════════════════════
// GLASS CARD — subtle frosted surface with optional accent border
// ════════════════════════════════════════════════════════════════════════════════
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    elevation: Int = 0,
    accentColor: Color = Color.Unspecified,
    content: @Composable () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val hasAccent = accentColor != Color.Unspecified

    val cardElevation = when (elevation) {
        1 -> ElevationSm
        2 -> ElevationMd
        3 -> ElevationXl
        else -> 0.dp
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (hasAccent) colors.surfaceVariant.copy(alpha = 0.92f) else colors.surfaceVariant.copy(alpha = 0.88f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
        shape = RoundedCornerShape(RadiusLg),
        border = if (hasAccent) BorderStroke(1.dp, accentColor) else BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.4f))
    ) {
        content()
    }
}

// ════════════════════════════════════════════════════════════════════════════════
// APP CARD — consistent card wrapper used across screens
// ════════════════════════════════════════════════════════════════════════════════
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    variant: CardVariant = CardVariant.Elevated,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val containerColor: Color
    val borderColor: Color
    val elevation: Dp

    when (variant) {
        CardVariant.Elevated -> {
            containerColor = colors.surface
            borderColor = Color.Unspecified
            elevation = ElevationMd
        }
        CardVariant.Filled -> {
            containerColor = colors.surfaceVariant
            borderColor = Color.Unspecified
            elevation = 0.dp
        }
        CardVariant.Outlined -> {
            containerColor = colors.surface
            borderColor = colors.outlineVariant
            elevation = 0.dp
        }
        CardVariant.Glass -> {
            containerColor = colors.surfaceVariant.copy(alpha = 0.85f)
            borderColor = colors.outlineVariant.copy(alpha = 0.3f)
            elevation = ElevationSm
        }
    }

    val cardModifier = modifier.fillMaxWidth()
        .let { if (onClick != null) it.clickable(onClick = onClick) else it }

    Card(
        modifier = cardModifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        shape = RoundedCornerShape(RadiusLg),
        border = if (borderColor != Color.Unspecified) BorderStroke(1.dp, borderColor) else null
    ) {
        content()
    }
}

enum class CardVariant { Elevated, Filled, Outlined, Glass }

// ════════════════════════════════════════════════════════════════════════════════
// SECTION TITLE — consistent section headers
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
fun AppSectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(bottom = SpaceSm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium?.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.2.sp
            ) ?: TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                letterSpacing = 0.2.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        action?.invoke()
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// STATUS DOT — animated status indicator
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
fun StatusDot(
    state: StatusState,
    modifier: Modifier = Modifier,
    size: Int = 8,
    showPulse: Boolean = true,
) {
    val color = when (state) {
        StatusState.Connected -> PocketGreen
        StatusState.Connecting -> PocketOrange
        StatusState.Disconnected -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        StatusState.Error -> Color.Red
    }

    val pulseAlpha = if (showPulse && state == StatusState.Connecting) {
        val infiniteTransition = rememberInfiniteTransition(label = "connecting-status")
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0.4f,
            animationSpec = infiniteRepeatable(
                animation = tween<Float>(1000, easing = androidx.compose.animation.core.LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        ).value
    } else {
        1f
    }

    Box(
        modifier = modifier
            .size(size.dp)
            .graphicsLayer {
                alpha = pulseAlpha
            }
            .clip(RoundedCornerShape(RadiusPill))
            .background(color)
    )
}

enum class StatusState { Connected, Connecting, Disconnected, Error }

// ═══════════════════════════════════════════════════════════════════════════════
// DIVIDER — consistent dividers
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
fun AppDivider(
    modifier: Modifier = Modifier,
    vertical: Boolean = false,
    thickness: Int = 1,
) {
    val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.38f)
    if (vertical) {
        Box(
            modifier = modifier
                .width(thickness.dp)
                .fillMaxSize()
                .background(dividerColor)
        )
    } else {
        Divider(
            modifier = modifier.fillMaxWidth(),
            color = dividerColor,
            thickness = thickness.dp
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// EMPTY STATE — consistent empty/placeholder UI
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
fun EmptyState(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String? = null,
    action: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Space2xl, vertical = Space3xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        icon()
        Spacer(modifier = Modifier.padding(top = SpaceLg))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium?.copy(
                fontWeight = FontWeight.Medium
            ) ?: TextStyle(
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        subtitle?.let { sub ->
            Spacer(modifier = Modifier.padding(top = SpaceXs))
            Text(
                text = sub,
                style = MaterialTheme.typography.bodyMedium ?: TextStyle(fontSize = 14.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        action?.let { act ->
            Spacer(modifier = Modifier.padding(top = SpaceLg))
            act()
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// APP LIST ROW — consistent list item with leading/trailing content
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
fun AppListRow(
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
    title: String,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    divider: Boolean = true,
) {
    val colors = MaterialTheme.colorScheme

    val rowModifier = modifier
        .fillMaxWidth()
        .padding(horizontal = SpaceLg, vertical = SpaceMd)

    val clickableModifier = if (onClick != null) {
        rowModifier.clickable(onClick = onClick)
    } else {
        rowModifier
    }

    Row(
        modifier = clickableModifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = spacedBy(SpaceMd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leading?.invoke()
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge?.copy(
                        fontWeight = FontWeight.Normal
                    ) ?: TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal),
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                subtitle?.let { sub ->
                    Text(
                        text = sub,
                        style = MaterialTheme.typography.bodySmall ?: TextStyle(fontSize = 12.sp),
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        trailing?.invoke()
    }

    if (divider) {
        AppDivider(modifier = Modifier.padding(horizontal = SpaceLg))
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// APP TOP BAR — consistent top app bar
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    centerTitle: Boolean = false,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = SpaceLg)
            .background(backgroundColor),
        horizontalArrangement = if (centerTitle) Arrangement.Center else Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        leading?.invoke()
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge?.copy(
                fontWeight = FontWeight.Bold
            ) ?: TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        trailing?.invoke()
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// APP FAB — consistent floating action button
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
fun AppFab(
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = PocketOrange,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    extended: Boolean = false,
    text: String? = null,
) {
    if (extended && text != null) {
        ExtendedFloatingActionButton(
            onClick = onClick,
            modifier = modifier.padding(bottom = SpaceLg, end = SpaceLg),
            icon = { icon() },
            text = { Text(text = text, fontWeight = FontWeight.Bold) },
            containerColor = backgroundColor,
            contentColor = contentColor,
            shape = RoundedCornerShape(RadiusPill),
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = ElevationLg
            )
        )
    } else {
        FloatingActionButton(
            onClick = onClick,
            modifier = modifier.padding(bottom = SpaceLg, end = SpaceLg),
            containerColor = backgroundColor,
            contentColor = contentColor,
            shape = RoundedCornerShape(RadiusPill),
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = ElevationLg
            )
        ) {
            icon()
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// TOOLTIP — lightweight tooltip
// ═══════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TooltipBox(
    modifier: Modifier = Modifier,
    tooltipText: String,
    content: @Composable () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val tooltipState = remember { TooltipState() }

    Box(modifier = modifier) {
        M3TooltipBox(
            modifier = Modifier.fillMaxSize(),
            state = tooltipState,
            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
            tooltip = {
                Box(
                    modifier = Modifier.padding(SpaceMd)
                        .background(
                            color = colors.surfaceContainer.copy(alpha = 0.95f),
                            shape = RoundedCornerShape(RadiusSm)
                        )
                ) {
                    Text(
                        text = tooltipText,
                        style = MaterialTheme.typography.bodySmall ?: TextStyle(fontSize = 12.sp),
                        color = colors.onSurface,
                        maxLines = 3
                    )
                }
            }
        ) {
            content()
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// CHIP — compact selectable/filter chip
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
fun AppChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme

    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        label = {
            Row(
                horizontalArrangement = spacedBy(SpaceXs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                leadingIcon?.invoke()
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge?.copy(
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    ) ?: TextStyle(
                        fontSize = 14.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant
                )
                trailingIcon?.invoke()
            }
        },
        shape = RoundedCornerShape(RadiusPill),
        border = if (!selected) BorderStroke(1.dp, colors.outlineVariant) else null
    )
}

// ═══════════════════════════════════════════════════════════════════════════════
// LOADING INDICATOR — consistent loading states
// ══════════════════════════════════════════════════════════════════════════════
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    size: Int = 24,
    color: Color = PocketOrange,
) {
    CircularProgressIndicator(
        modifier = modifier.size(size.dp),
        color = color,
        strokeWidth = 2.5.dp,
        trackColor = color.copy(alpha = 0.2f)
    )
}

@Composable
fun LoadingOverlay(
    message: String? = null,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = spacedBy(SpaceMd)
        ) {
            LoadingIndicator(size = 36)
            message?.let { msg ->
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodyLarge?.copy(
                        fontWeight = FontWeight.Medium
                    ) ?: TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
                    color = Color.White
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// ANIMATED VISIBILITY — shared animation specs
// ═══════════════════════════════════════════════════════════════════════════════
object AnimationSpecs {
    val FadeInOut = androidx.compose.animation.core.tween<Float>(
        durationMillis = 200,
        easing = androidx.compose.animation.core.FastOutSlowInEasing
    )

    val SlideUpDown = androidx.compose.animation.core.tween<Float>(
        durationMillis = 300,
        easing = androidx.compose.animation.core.FastOutSlowInEasing
    )

    val SpringDamping = androidx.compose.animation.core.spring<Float>(
        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
        stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
    )

    val ExpandCollapse = androidx.compose.animation.core.tween<Float>(
        durationMillis = 250,
        easing = androidx.compose.animation.core.FastOutSlowInEasing
    )
}

// ═══════════════════════════════════════════════════════════════════════════════
// SCREEN SCAFFOLD — consistent screen structure
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
fun ScreenScaffold(
    topBar: @Composable () -> Unit = {},
    content: @Composable (innerPadding: androidx.compose.foundation.layout.PaddingValues) -> Unit,
    modifier: Modifier = Modifier,
    fab: (@Composable () -> Unit)? = null,
) {
    Scaffold(
        modifier = modifier,
        topBar = { topBar() },
        floatingActionButton = { fab?.invoke() },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = androidx.compose.material3.ScaffoldDefaults.contentWindowInsets
    ) { innerPadding ->
        content(innerPadding)
    }
}

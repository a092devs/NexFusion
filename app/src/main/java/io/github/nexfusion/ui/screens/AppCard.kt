package io.github.nexfusion.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.nexfusion.core.R
import io.github.nexfusion.core.compatibility.CompatibilityStatus
import io.github.nexfusion.core.compatibility.VersionComparator
import io.github.nexfusion.core.discovery.AppDiscovery
import io.github.nexfusion.core.discovery.AppItemState
import io.github.nexfusion.ui.theme.isMiuixStyle
import io.github.nexfusion.ui.theme.nexFusionCardBorder
import io.github.nexfusion.ui.theme.nexFusionCardShape
import top.yukonga.miuix.kmp.basic.Card as MiuixCard


/**
 * Modern Material 3 & Miuix application card.
 *
 * Displays live installed package status, verified compatibility badge,
 * recommended version, and module scope alerts without decorative clutter.
 */
@Composable
fun AppCard(
    app: AppItemState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val icon = remember(app.packageName) { AppDiscovery.loadAppIcon(context, app.packageName) }

    val isMiuix = isMiuixStyle
    val baseContainer = if (app.isInstalled) {
        MaterialTheme.colorScheme.surfaceContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerLowest
    }
    val containerColor = if (isMiuix) baseContainer.copy(alpha = 0.82f) else baseContainer

    if (isMiuix) {
        MiuixCard(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 5.dp),
            onClick = onClick,
        ) {
            AppCardDetails(app = app, icon = icon)
        }
    } else {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clickable(onClick = onClick),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            shape = nexFusionCardShape(),
            border = nexFusionCardBorder(),
        ) {
            AppCardDetails(app = app, icon = icon)
        }
    }
}

@Composable
private fun AppCardDetails(app: AppItemState, icon: ImageBitmap?) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            AppIconBadge(icon = icon, brandColor = app.appIconColor, appName = app.appName)

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(Modifier.size(4.dp))

                val isCompatible = app.compatibility is CompatibilityStatus.Compatible
                val shouldShowRec = app.recommendedVersion != null && (
                    !app.isInstalled || (!isCompatible && app.installedVersionName != null && VersionComparator.compare(app.installedVersionName, app.recommendedVersion) != 0)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    val installedText = if (app.isInstalled) {
                        "v${app.installedVersionName ?: "?"}"
                    } else {
                        stringResource(R.string.status_not_installed)
                    }
                    Text(
                        text = installedText,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (app.isInstalled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = if (shouldShowRec) Modifier.weight(1f, fill = false) else Modifier,
                    )

                    if (shouldShowRec) {
                        Text(
                            text = "• Rec: v${app.recommendedVersion}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                    }
                }
            }
        }

        // Evidence-based Badges row; FlowRow so badge sets wrap instead of overflowing on
        // narrow windows or scaled-text densities.
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
        ) {
            if (app.isExcluded) {
                StatusChip(
                    label = "Excluded",
                    icon = Icons.Outlined.Block,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                CompatibilityChip(app.compatibility)
            }

            if (app.hasConflicts) {
                StatusChip(
                    label = "Dual Bundle",
                    icon = Icons.Outlined.WarningAmber,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }

            app.sourceNames.take(2).forEach { srcName ->
                StatusChip(
                    label = srcName,
                    icon = null,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (app.isInstalled && !app.inScope) {
                StatusChip(
                    label = stringResource(R.string.badge_not_in_scope),
                    icon = Icons.Outlined.WarningAmber,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }
    }
}


@Composable
private fun AppIconBadge(
    icon: ImageBitmap?,
    brandColor: Int?,
    appName: String,
) {
    if (icon != null) {
        Image(
            bitmap = icon,
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp)),
        )
    } else {
        val bg = brandColor?.let { Color(it or (0xFF shl 24)) } ?: MaterialTheme.colorScheme.primaryContainer
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(bg, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = appName.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }
    }
}

@Composable
fun CompatibilityChip(status: CompatibilityStatus) {
    val isDark = isSystemInDarkTheme()
    when (status) {
        is CompatibilityStatus.Compatible -> StatusChip(
            label = if (status.isExperimental) {
                stringResource(R.string.status_compatible_experimental)
            } else {
                stringResource(R.string.status_compatible)
            },
            icon = if (status.isExperimental) Icons.Outlined.Science else Icons.Outlined.CheckCircle,
            containerColor = if (isDark) Color(0xFF10B981).copy(alpha = 0.22f) else Color(0xFF10B981).copy(alpha = 0.12f),
            contentColor = if (isDark) Color(0xFF34D399) else Color(0xFF065F46),
        )

        is CompatibilityStatus.Dynamic -> StatusChip(
            label = stringResource(R.string.status_dynamic),
            icon = Icons.Outlined.AutoAwesome,
            containerColor = if (isDark) Color(0xFF0284C7).copy(alpha = 0.22f) else Color(0xFF0284C7).copy(alpha = 0.12f),
            contentColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF075985),
        )

        is CompatibilityStatus.CompatibleUntested -> StatusChip(
            label = stringResource(R.string.status_untested, status.installedVersion),
            icon = Icons.AutoMirrored.Outlined.HelpOutline,
            containerColor = if (isDark) Color(0xFFF59E0B).copy(alpha = 0.22f) else Color(0xFFF59E0B).copy(alpha = 0.14f),
            contentColor = if (isDark) Color(0xFFFCD34D) else Color(0xFF92400E),
        )

        is CompatibilityStatus.Incompatible -> StatusChip(
            label = stringResource(R.string.status_incompatible),
            icon = Icons.Outlined.ErrorOutline,
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        )

        is CompatibilityStatus.MissingBundle -> StatusChip(
            label = stringResource(R.string.status_missing_bundle),
            icon = Icons.Outlined.WarningAmber,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        is CompatibilityStatus.NotInstalled -> StatusChip(
            label = stringResource(R.string.status_not_installed),
            icon = null,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        is CompatibilityStatus.Unknown -> StatusChip(
            label = status.reason,
            icon = Icons.AutoMirrored.Outlined.HelpOutline,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun StatusChip(
    label: String,
    icon: ImageVector?,
    containerColor: Color,
    contentColor: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .background(containerColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(13.dp),
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

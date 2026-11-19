package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.ToolActionFlow
import com.example.ui.theme.LiquidGlassButton
import com.example.ui.theme.LiquidGlassCard
import com.example.ui.theme.LiquidGlassIconBadge
import com.example.ui.theme.LiquidGlassStatusPill
import com.example.ui.theme.LocalLiquidGlassPalette
import com.example.ui.theme.MonoFontFamily
import com.example.viewmodel.MainTab
import com.example.viewmodel.MajlishUiState

@Composable
fun HomeScreen(
    uiState: MajlishUiState,
    onOpenActionFlow: (ToolActionFlow) -> Unit,
    onNavigateTab: (MainTab) -> Unit,
    onRefreshWorkspace: () -> Unit,
    onGrantStorageClick: () -> Unit,
    onSignOut: () -> Unit,
    bottomContentPadding: Dp
) {
    val palette = LocalLiquidGlassPalette.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 18.dp,
            end = 18.dp,
            top = 12.dp,
            bottom = bottomContentPadding + 20.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Brand Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF090F1E))
                            .border(1.dp, palette.accent.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.majlish_logo_1795056643980),
                            contentDescription = stringResource(R.string.cd_app_logo),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge.copy(
                                letterSpacing = 3.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = palette.textPrimary
                        )
                        Text(
                            text = "SECURE • FAST • LIQUID OS",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.accent
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LiquidGlassStatusPill(
                        text = "ONLINE",
                        dotColor = palette.success
                    )
                    IconButton(
                        onClick = onRefreshWorkspace,
                        modifier = Modifier.size(44.dp).testTag("home_refresh_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Rescan MAJLISH_TOOL",
                            tint = palette.accent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onSignOut,
                        modifier = Modifier.size(44.dp).testTag("home_sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Logout,
                            contentDescription = stringResource(R.string.cd_sign_out),
                            tint = palette.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Storage Permission Banner if not yet granted
        if (!uiState.hasStorageAccess) {
            item {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 22.dp,
                    accentTint = palette.warning,
                    elevated = true,
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Storage, null, tint = palette.warning, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Enable MAJLISH_TOOL Folder",
                                style = MaterialTheme.typography.titleSmall,
                                color = palette.warning
                            )
                        }
                        Text(
                            text = "Grant All Files Access so MAJLISH automatically creates Internal Storage/MAJLISH_TOOL and auto-detects your PAK & Lua files.",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textSecondary
                        )
                        LiquidGlassButton(
                            text = "Allow Storage Access",
                            onClick = onGrantStorageClick,
                            accentOverride = palette.warning,
                            modifier = Modifier.fillMaxWidth().testTag("home_allow_storage_btn")
                        )
                    }
                }
            }
        }

        // VIP License Hero Card
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 28.dp,
                elevated = true,
                contentPadding = PaddingValues(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Verified,
                                contentDescription = null,
                                tint = palette.accent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(7.dp))
                            Text(
                                text = stringResource(R.string.vip_access),
                                style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.4.sp),
                                color = palette.accent
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(palette.success.copy(alpha = 0.20f))
                                .border(1.dp, palette.success.copy(alpha = 0.50f), RoundedCornerShape(100.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "ACTIVE • v3.0",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.success
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    val maskedKey = uiState.activeKey.let { k ->
                        if (k.length > 10) "${k.take(6)}••••${k.takeLast(4)}" else k.ifBlank { "AUTHENTICATED" }
                    }
                    Text(
                        text = maskedKey,
                        style = MaterialTheme.typography.headlineSmall.copy(fontFamily = MonoFontFamily),
                        color = palette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TelemetryStat("EXPIRY", uiState.licenseExpiry, Modifier.weight(1f))
                        TelemetryStat("DEVICE", uiState.deviceName, Modifier.weight(1f))
                        TelemetryStat("TIME", uiState.currentTime.ifBlank { "--:--:--" }, Modifier.weight(0.85f))
                    }
                }
            }
        }

        // Auto-detected MAJLISH_TOOL Counters
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MiniCounterCard(
                    label = "PAK/",
                    value = uiState.pakFiles.size.toString(),
                    modifier = Modifier.weight(1f),
                    onClick = { onOpenActionFlow(ToolActionFlow.PickUnpackPak) }
                )
                MiniCounterCard(
                    label = "UNPACK/",
                    value = uiState.unpackedFolders.size.toString(),
                    modifier = Modifier.weight(1f),
                    onClick = { onOpenActionFlow(ToolActionFlow.PickRepackFolder) }
                )
                MiniCounterCard(
                    label = "RESULT/",
                    value = uiState.outputPaks.size.toString(),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTab(MainTab.VAULT) }
                )
                MiniCounterCard(
                    label = "LUA",
                    value = (uiState.luaOrgFiles.size + uiState.luaEditFiles.size).toString(),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTab(MainTab.LUA_STUDIO) }
                )
            }
        }

        // Section Label
        item {
            Text(
                text = "COMMAND SUITE — TAP TO AUTO-DETECT",
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.4.sp),
                color = palette.textMuted,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }

        // 2-Column Command Grid — Each tile opens the auto-detect popup picker immediately!
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CommandGlassTile(
                        title = "Unpack PAK",
                        subtitle = "Auto-detect PAK/ (${uiState.pakFiles.size})",
                        icon = Icons.Filled.Unarchive,
                        accent = palette.accent,
                        modifier = Modifier.weight(1f).testTag("cmd_unpack"),
                        onClick = { onOpenActionFlow(ToolActionFlow.PickUnpackPak) }
                    )
                    CommandGlassTile(
                        title = "Repack PAK",
                        subtitle = "Unpacked (${uiState.unpackedFolders.size})",
                        icon = Icons.Filled.Archive,
                        accent = palette.accentSecondary,
                        modifier = Modifier.weight(1f).testTag("cmd_repack"),
                        onClick = { onOpenActionFlow(ToolActionFlow.PickRepackFolder) }
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CommandGlassTile(
                        title = "Repack to Path",
                        subtitle = "Pick PAK + custom dir",
                        icon = Icons.Filled.Route,
                        accent = Color(0xFF60A5FA),
                        modifier = Modifier.weight(1f).testTag("cmd_repack_path"),
                        onClick = { onOpenActionFlow(ToolActionFlow.PickToPathFolder) }
                    )
                    CommandGlassTile(
                        title = "Custom File",
                        subtitle = "Inject EDIT/ (${uiState.pakToolEditFiles.size})",
                        icon = Icons.Filled.AddCircleOutline,
                        accent = Color(0xFF34D399),
                        modifier = Modifier.weight(1f).testTag("cmd_inject"),
                        onClick = { onOpenActionFlow(ToolActionFlow.PickInjectPak) }
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CommandGlassTile(
                        title = "Encrypt PAK",
                        subtitle = "SM4 Type 47 protect",
                        icon = Icons.Filled.Security,
                        accent = Color(0xFFF472B6),
                        modifier = Modifier.weight(1f).testTag("cmd_protect"),
                        onClick = { onOpenActionFlow(ToolActionFlow.PickProtectPak) }
                    )
                    CommandGlassTile(
                        title = "Lua Studio",
                        subtitle = "Decompile & Recompile",
                        icon = Icons.Filled.Code,
                        accent = Color(0xFFFBBF24),
                        modifier = Modifier.weight(1f).testTag("cmd_lua"),
                        onClick = { onNavigateTab(MainTab.LUA_STUDIO) }
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CommandGlassTile(
                        title = "Delete Folder",
                        subtitle = "Folders (${uiState.deletableFolders.size})",
                        icon = Icons.Filled.DeleteSweep,
                        accent = Color(0xFFFF6B81),
                        modifier = Modifier.weight(1f).testTag("cmd_delete_folder"),
                        onClick = { onOpenActionFlow(ToolActionFlow.PickDeleteFolder) }
                    )
                    CommandGlassTile(
                        title = "Glass Theme",
                        subtitle = uiState.themePreset.displayName,
                        icon = Icons.Filled.Palette,
                        accent = palette.accent,
                        modifier = Modifier.weight(1f).testTag("cmd_theme"),
                        onClick = { onNavigateTab(MainTab.VAULT) }
                    )
                }
            }
        }

        // Workspace Folder Indicator Card
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 22.dp,
                contentPadding = PaddingValues(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LiquidGlassIconBadge(icon = Icons.Filled.FolderOpen, size = 40.dp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = uiState.storagePathLabel,
                            style = MaterialTheme.typography.titleSmall.copy(fontFamily = MonoFontFamily),
                            color = palette.accent
                        )
                        Text(
                            text = "Drop files into PAK/, UNPACK/<pak>/edited/, PAK TOOL/, or LUA TOOL/ using your file manager — MAJLISH detects them automatically.",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TelemetryStat(label: String, value: String, modifier: Modifier = Modifier) {
    val palette = LocalLiquidGlassPalette.current
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = palette.textMuted
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = palette.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MiniCounterCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val palette = LocalLiquidGlassPalette.current
    LiquidGlassCard(
        modifier = modifier,
        cornerRadius = 20.dp,
        contentPadding = PaddingValues(vertical = 12.dp, horizontal = 8.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = palette.accent
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = palette.textSecondary,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CommandGlassTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val palette = LocalLiquidGlassPalette.current
    LiquidGlassCard(
        modifier = modifier,
        cornerRadius = 24.dp,
        accentTint = accent,
        contentPadding = PaddingValues(16.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            LiquidGlassIconBadge(icon = icon, tint = accent, size = 42.dp)
            Spacer(Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = palette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = palette.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

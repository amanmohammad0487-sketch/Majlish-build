package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.workspace.WorkspaceManager
import com.example.ui.components.ToolActionFlow
import com.example.ui.theme.GlassThemePreset
import com.example.ui.theme.LiquidGlassButton
import com.example.ui.theme.LiquidGlassCard
import com.example.ui.theme.LocalLiquidGlassPalette
import com.example.ui.theme.MonoFontFamily
import com.example.viewmodel.MajlishUiState

@Composable
fun VaultScreen(
    uiState: MajlishUiState,
    onSelectTheme: (GlassThemePreset) -> Unit,
    onOpenDeleteFolderPicker: () -> Unit,
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
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Vault & Settings", style = MaterialTheme.typography.headlineMedium, color = palette.textPrimary)
                    Text(
                        text = uiState.storagePathLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonoFontFamily),
                        color = palette.accent
                    )
                }
                IconButton(onClick = onRefreshWorkspace, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Filled.Refresh, "Rescan MAJLISH_TOOL", tint = palette.accent, modifier = Modifier.size(22.dp))
                }
            }
        }

        if (!uiState.hasStorageAccess) {
            item {
                LiquidGlassCard(modifier = Modifier.fillMaxWidth(), accentTint = palette.warning, elevated = true) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Storage, null, tint = palette.warning, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Storage Access Required", style = MaterialTheme.typography.titleSmall, color = palette.warning)
                        }
                        Text(
                            "Allow All Files Access so Internal Storage/MAJLISH_TOOL is created and auto-scanned.",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textSecondary
                        )
                        LiquidGlassButton(
                            text = "Grant Access",
                            onClick = onGrantStorageClick,
                            accentOverride = palette.warning,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Theme Selector (Option 8 - 6 GOAT Liquid Glass Themes)
        item {
            LiquidGlassCard(modifier = Modifier.fillMaxWidth(), elevated = true) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Palette, null, tint = palette.accent, modifier = Modifier.size(19.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.theme_selector_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.textPrimary
                        )
                    }

                    val presets = GlassThemePreset.entries
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        presets.chunked(2).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowItems.forEach { preset ->
                                    val active = uiState.themePreset == preset
                                    val shape = RoundedCornerShape(18.dp)
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(shape)
                                            .background(
                                                if (active) preset.accent.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.05f)
                                            )
                                            .border(
                                                width = if (active) 1.5.dp else 1.dp,
                                                color = if (active) preset.accent else Color.White.copy(alpha = 0.14f),
                                                shape = shape
                                            )
                                            .clickable { onSelectTheme(preset) }
                                            .padding(horizontal = 12.dp, vertical = 11.dp)
                                            .testTag("theme_option_${preset.id}"),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    Brush.linearGradient(listOf(preset.accent, preset.accentSecondary))
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (active) {
                                                Icon(
                                                    Icons.Filled.Check,
                                                    null,
                                                    tint = Color(0xFF041018),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                        Spacer(Modifier.width(9.dp))
                                        Text(
                                            text = preset.displayName,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                                            ),
                                            color = if (active) palette.textPrimary else palette.textSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Delete Folder Tool (Option 6) — launches auto-detect popup picker!
        item {
            LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("DELETE FOLDER TOOL", style = MaterialTheme.typography.labelMedium, color = Color(0xFFFF6B81))
                    Text(
                        text = "${uiState.deletableFolders.size} unpacked / custom folder(s) detected in MAJLISH_TOOL.",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary
                    )
                    LiquidGlassButton(
                        text = "Select Folder to Delete (${uiState.deletableFolders.size})",
                        onClick = onOpenDeleteFolderPicker,
                        icon = Icons.Filled.DeleteSweep,
                        accentOverride = Color(0xFFFF6B81),
                        modifier = Modifier.fillMaxWidth().testTag("vault_open_delete_picker_btn")
                    )
                }
            }
        }

        // Output PAKs Saved in RESULT/ & PAK TOOL/RESULT/
        item {
            LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("GENERATED PAKS (RESULT/)", style = MaterialTheme.typography.labelMedium, color = palette.accent)
                    if (uiState.outputPaks.isEmpty()) {
                        Text(
                            text = "No generated .pak files in MAJLISH_TOOL/RESULT/ or PAK TOOL/RESULT/ yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textMuted
                        )
                    } else {
                        uiState.outputPaks.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Filled.FolderZip, null, tint = palette.success, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text(item.name, style = MaterialTheme.typography.bodyMedium, color = palette.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(item.file.parentFile?.name.orEmpty(), style = MaterialTheme.typography.labelSmall, color = palette.textMuted)
                                    }
                                }
                                Text(WorkspaceManager.formatSize(item.sizeBytes), style = MaterialTheme.typography.labelSmall, color = palette.success)
                            }
                        }
                    }
                }
            }
        }

        // MAJLISH_TOOL Auto-Created Folders Overview
        item {
            LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("MAJLISH_TOOL FOLDERS", style = MaterialTheme.typography.labelMedium, color = palette.textSecondary)
                    uiState.workspaceFolders.forEach { folder ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "MAJLISH_TOOL/${folder.relativePath}",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonoFontFamily),
                                color = palette.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${folder.fileCount} • ${WorkspaceManager.formatSize(folder.sizeBytes)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.textMuted
                            )
                        }
                    }
                }
            }
        }

        // Session Sign-out Button
        item {
            LiquidGlassButton(
                text = stringResource(R.string.action_sign_out),
                onClick = onSignOut,
                icon = Icons.Filled.Logout,
                primary = false,
                modifier = Modifier.fillMaxWidth().testTag("vault_sign_out_btn")
            )
            Spacer(Modifier.height(6.dp))
        }
    }
}

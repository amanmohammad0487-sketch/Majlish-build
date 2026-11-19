package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.workspace.WorkspaceFileItem
import com.example.core.workspace.WorkspaceFolderInfo
import com.example.core.workspace.WorkspaceManager
import com.example.ui.components.ToolActionFlow
import com.example.ui.theme.LiquidGlassButton
import com.example.ui.theme.LiquidGlassCard
import com.example.ui.theme.LocalLiquidGlassPalette
import com.example.ui.theme.MonoFontFamily
import com.example.viewmodel.MajlishUiState
import com.example.viewmodel.PakLabMode
import java.io.File

@Composable
fun PakLabScreen(
    uiState: MajlishUiState,
    onSelectMode: (PakLabMode) -> Unit,
    onOpenActionFlow: (ToolActionFlow) -> Unit,
    onDirectPickUnpack: (File) -> Unit,
    onDirectPickRepack: (File, Boolean) -> Unit,
    onDirectPickInject: (File) -> Unit,
    onDirectPickProtect: (File) -> Unit,
    onRefreshWorkspace: () -> Unit,
    onClearLogs: () -> Unit,
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
        // Header + Rescan button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("PAK Lab", style = MaterialTheme.typography.headlineMedium, color = palette.textPrimary)
                    Text(
                        text = uiState.storagePathLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonoFontFamily),
                        color = palette.accent
                    )
                }
                IconButton(
                    onClick = onRefreshWorkspace,
                    modifier = Modifier.size(48.dp).testTag("pak_lab_refresh_btn")
                ) {
                    Icon(Icons.Filled.Refresh, "Rescan MAJLISH_TOOL", tint = palette.accent, modifier = Modifier.size(22.dp))
                }
            }
        }

        // Mode Segmented Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PakLabMode.entries.forEach { mode ->
                    val selected = uiState.pakLabMode == mode
                    val shape = RoundedCornerShape(100.dp)
                    Box(
                        modifier = Modifier
                            .clip(shape)
                            .background(if (selected) palette.accent.copy(alpha = 0.24f) else Color.White.copy(alpha = 0.06f))
                            .border(1.dp, if (selected) palette.accent else Color.White.copy(alpha = 0.16f), shape)
                            .clickable { onSelectMode(mode) }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .testTag("pak_mode_${mode.name.lowercase()}")
                    ) {
                        Text(
                            text = mode.label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (selected) palette.accent else palette.textSecondary
                        )
                    }
                }
            }
        }

        // Busy Progress Card
        item {
            AnimatedVisibility(visible = uiState.isBusy) {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 22.dp,
                    elevated = true,
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uiState.busyTitle,
                                style = MaterialTheme.typography.titleSmall,
                                color = palette.accent,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${(uiState.busyProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonoFontFamily),
                                color = palette.textPrimary
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { uiState.busyProgress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(100.dp)),
                            color = palette.accent,
                            trackColor = Color.White.copy(alpha = 0.12f)
                        )
                        if (uiState.busyDetail.isNotBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = uiState.busyDetail,
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Mode Card with Auto-Detected Files + Popup Selector CTA
        item {
            when (uiState.pakLabMode) {
                PakLabMode.UNPACK -> AutoDetectedPakCard(
                    title = "Unpack PAK",
                    folderPath = "MAJLISH_TOOL/PAK/",
                    subtitle = "Tap any auto-detected .pak file below or open the selector popup.",
                    files = uiState.pakFiles,
                    ctaText = "Select PAK to Unpack (${uiState.pakFiles.size})",
                    ctaIcon = Icons.Filled.Unarchive,
                    isBusy = uiState.isBusy,
                    onOpenPicker = { onOpenActionFlow(ToolActionFlow.PickUnpackPak) },
                    onTapItem = onDirectPickUnpack
                )

                PakLabMode.REPACK -> AutoDetectedFolderCard(
                    title = "Repack PAK",
                    folderPath = "MAJLISH_TOOL/UNPACK/<pak>/edited/",
                    subtitle = "Auto-detects unpacked PAK folders and your files in edited/.",
                    folders = uiState.unpackedFolders,
                    ctaText = "Select Unpacked PAK (${uiState.unpackedFolders.size})",
                    ctaIcon = Icons.Filled.Archive,
                    isBusy = uiState.isBusy,
                    onOpenPicker = { onOpenActionFlow(ToolActionFlow.PickRepackFolder) },
                    onTapFolder = { onDirectPickRepack(it, false) }
                )

                PakLabMode.TO_PATH -> AutoDetectedFolderCard(
                    title = "Repack to Path",
                    folderPath = "MAJLISH_TOOL/UNPACK/<pak>/edited/",
                    subtitle = "Select an unpacked PAK, then enter the target internal PAK directory.",
                    folders = uiState.unpackedFolders,
                    ctaText = "Select Unpacked PAK (${uiState.unpackedFolders.size})",
                    ctaIcon = Icons.Filled.Route,
                    isBusy = uiState.isBusy,
                    onOpenPicker = { onOpenActionFlow(ToolActionFlow.PickToPathFolder) },
                    onTapFolder = { onDirectPickRepack(it, true) }
                )

                PakLabMode.INJECT -> AutoDetectedPakCard(
                    title = "Custom File Inject",
                    folderPath = "MAJLISH_TOOL/PAK TOOL/PAK/ + EDIT/ (${uiState.pakToolEditFiles.size} file(s))",
                    subtitle = "Auto-detects .pak files and injects all files found in PAK TOOL/EDIT/.",
                    files = uiState.pakToolPakFiles,
                    ctaText = "Select PAK to Inject (${uiState.pakToolPakFiles.size})",
                    ctaIcon = Icons.Filled.AddCircleOutline,
                    isBusy = uiState.isBusy,
                    onOpenPicker = { onOpenActionFlow(ToolActionFlow.PickInjectPak) },
                    onTapItem = onDirectPickInject
                )

                PakLabMode.PROTECT -> AutoDetectedPakCard(
                    title = "SM4 PAK Protection",
                    folderPath = "MAJLISH_TOOL/PAK TOOL/PAK/",
                    subtitle = "Re-encrypts every entry with game-native SM4 Type 47 into PAK TOOL/RESULT/.",
                    files = uiState.pakToolPakFiles,
                    ctaText = "Select PAK to Protect (${uiState.pakToolPakFiles.size})",
                    ctaIcon = Icons.Filled.Security,
                    accentOverride = Color(0xFFF472B6),
                    isBusy = uiState.isBusy,
                    onOpenPicker = { onOpenActionFlow(ToolActionFlow.PickProtectPak) },
                    onTapItem = onDirectPickProtect
                )
            }
        }

        // Latest Result Card
        uiState.lastRepackSummary?.let { summary ->
            item {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 22.dp,
                    accentTint = palette.success,
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.CheckCircle, null, tint = palette.success, modifier = Modifier.size(20.dp))
                            Text(
                                text = "Saved to MAJLISH_TOOL • ${summary.modeLabel}",
                                style = MaterialTheme.typography.titleSmall,
                                color = palette.success
                            )
                        }
                        Text(
                            text = "${summary.outputFileName} • ${summary.processedFiles} processed" +
                                (if (summary.addedFiles > 0) ", ${summary.addedFiles} added" else "") +
                                (if (summary.fittedBlocks > 0) " • ${summary.fittedBlocks} blocks fitted" else ""),
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textPrimary
                        )
                        Text(
                            text = summary.outputAbsolutePath,
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonoFontFamily),
                            color = palette.textMuted,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Block Telemetry
        if (uiState.blockResults.isNotEmpty()) {
            item {
                LiquidGlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 22.dp, contentPadding = PaddingValues(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("BLOCK TELEMETRY (${uiState.blockResults.size})", style = MaterialTheme.typography.labelMedium, color = palette.accent)
                        uiState.blockResults.takeLast(16).forEach { blk ->
                            val sizeMb = blk.blockSizeBytes / (1024.0 * 1024.0)
                            val ratio = blk.compressionRatio?.let { " [${"%.1f".format(it * 100f)}%]" }.orEmpty()
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    text = "${blk.fileName} #${blk.blockIndex} (${"%.2f".format(sizeMb)} MB)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = palette.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = if (blk.fitted) "✓ FITTED$ratio" else "✗ SKIPPED",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (blk.fitted) palette.success else palette.error
                                )
                            }
                        }
                    }
                }
            }
        }

        // Live Engine Console Log
        item {
            LiquidGlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 22.dp, contentPadding = PaddingValues(16.dp)) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Terminal, null, tint = palette.accent, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "ENGINE CONSOLE",
                                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.2.sp),
                                color = palette.accent
                            )
                        }
                        if (uiState.logs.isNotEmpty()) {
                            IconButton(onClick = onClearLogs, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Filled.ClearAll, "Clear logs", tint = palette.textMuted, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    if (uiState.logs.isEmpty()) {
                        Text(
                            text = "Ready. Drop .pak files into Internal Storage/MAJLISH_TOOL/PAK/ and pick a file to begin.",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textMuted
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0x99040710))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            uiState.logs.takeLast(14).forEach { line ->
                                Text(
                                    text = line,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = MonoFontFamily,
                                        lineHeight = 16.sp
                                    ),
                                    color = when {
                                        line.startsWith("❌") -> palette.error
                                        line.startsWith("✅") || line.startsWith("✓") -> palette.success
                                        line.startsWith("⚠") || line.startsWith("!") -> palette.warning
                                        else -> palette.textSecondary
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AutoDetectedPakCard(
    title: String,
    folderPath: String,
    subtitle: String,
    files: List<WorkspaceFileItem>,
    ctaText: String,
    ctaIcon: androidx.compose.ui.graphics.vector.ImageVector,
    accentOverride: Color? = null,
    isBusy: Boolean,
    onOpenPicker: () -> Unit,
    onTapItem: (File) -> Unit
) {
    val palette = LocalLiquidGlassPalette.current
    val accent = accentOverride ?: palette.accent
    LiquidGlassCard(modifier = Modifier.fillMaxWidth(), elevated = true, accentTint = accent) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                Text(folderPath, style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonoFontFamily), color = accent)
                Spacer(Modifier.height(4.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
            }

            if (files.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.04f))
                        .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "No .pak files auto-detected in $folderPath yet. Place your .pak files there and tap Refresh.",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textMuted
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    files.take(6).forEach { item ->
                        val shape = RoundedCornerShape(16.dp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(shape)
                                .background(Color.White.copy(alpha = 0.06f))
                                .border(1.dp, Color.White.copy(alpha = 0.14f), shape)
                                .clickable(enabled = !isBusy) { onTapItem(item.file) }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Filled.FolderZip, null, tint = accent, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = palette.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = WorkspaceManager.formatSize(item.sizeBytes),
                                style = MaterialTheme.typography.labelSmall,
                                color = accent
                            )
                        }
                    }
                }
            }

            LiquidGlassButton(
                text = ctaText,
                onClick = onOpenPicker,
                icon = ctaIcon,
                accentOverride = accentOverride,
                enabled = !isBusy,
                modifier = Modifier.fillMaxWidth().testTag("pak_open_picker_btn")
            )
        }
    }
}

@Composable
private fun AutoDetectedFolderCard(
    title: String,
    folderPath: String,
    subtitle: String,
    folders: List<WorkspaceFolderInfo>,
    ctaText: String,
    ctaIcon: androidx.compose.ui.graphics.vector.ImageVector,
    isBusy: Boolean,
    onOpenPicker: () -> Unit,
    onTapFolder: (File) -> Unit
) {
    val palette = LocalLiquidGlassPalette.current
    LiquidGlassCard(modifier = Modifier.fillMaxWidth(), elevated = true) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                Text(folderPath, style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonoFontFamily), color = palette.accent)
                Spacer(Modifier.height(4.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
            }

            if (folders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.04f))
                        .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "No unpacked PAK folders detected in MAJLISH_TOOL/UNPACK/ yet. Unpack a PAK first.",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textMuted
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    folders.take(6).forEach { folder ->
                        val shape = RoundedCornerShape(16.dp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(shape)
                                .background(Color.White.copy(alpha = 0.06f))
                                .border(1.dp, Color.White.copy(alpha = 0.14f), shape)
                                .clickable(enabled = !isBusy) { onTapFolder(folder.file) }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Filled.Folder, null, tint = palette.accent, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = folder.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = palette.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${folder.fileCount} files • ${WorkspaceManager.formatSize(folder.sizeBytes)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = palette.textMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }

            LiquidGlassButton(
                text = ctaText,
                onClick = onOpenPicker,
                icon = ctaIcon,
                enabled = !isBusy,
                modifier = Modifier.fillMaxWidth().testTag("pak_open_folder_picker_btn")
            )
        }
    }
}

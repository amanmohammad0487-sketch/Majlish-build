package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.core.pak.RepackResolveMode
import com.example.core.workspace.WorkspaceFileItem
import com.example.core.workspace.WorkspaceFolderInfo
import com.example.core.workspace.WorkspaceManager
import com.example.ui.theme.LiquidGlassButton
import com.example.ui.theme.LiquidGlassTextField
import com.example.ui.theme.LocalLiquidGlassPalette
import com.example.ui.theme.MonoFontFamily
import java.io.File

data class PickerEntry(
    val title: String,
    val subtitle: String,
    val file: File,
    val isFolder: Boolean = false
)

/**
 * Interactive workflow state machine for auto-detected folder pickers and confirmation prompts
 * (matching the screenshot's popup picker + Yes/No confirmation UX in Liquid Glass style).
 */
sealed interface ToolActionFlow {
    data object None : ToolActionFlow

    // 1. UNPACK PAK
    data object PickUnpackPak : ToolActionFlow
    data class ConfirmUnpackPak(val pakFile: File) : ToolActionFlow

    // 2. REPACK PAK
    data object PickRepackFolder : ToolActionFlow
    data class ConfirmRepackFolder(val folder: File, val editedFiles: List<WorkspaceFileItem>) : ToolActionFlow

    // 3. REPACK TO PATH
    data object PickToPathFolder : ToolActionFlow
    data class ConfirmToPath(val folder: File, val editedFiles: List<WorkspaceFileItem>) : ToolActionFlow

    // 4. CUSTOM FILE (INJECT)
    data object PickInjectPak : ToolActionFlow
    data class ConfirmInject(val pakFile: File, val editFiles: List<WorkspaceFileItem>) : ToolActionFlow

    // 5. ENCRYPT PAK (PROTECT)
    data object PickProtectPak : ToolActionFlow
    data class ConfirmProtect(val pakFile: File) : ToolActionFlow

    // 6. DELETE FOLDER
    data object PickDeleteFolder : ToolActionFlow
    data class ConfirmDeleteFolder(val folder: WorkspaceFolderInfo) : ToolActionFlow

    // 7A. LUA DECOMPILE
    data object PickLuaDecompile : ToolActionFlow
    data class ConfirmLuaDecompile(val files: List<File>) : ToolActionFlow

    // 7B. LUA RECOMPILE
    data object PickLuaRecompile : ToolActionFlow
    data class ConfirmLuaRecompile(val files: List<File>) : ToolActionFlow
}

@Composable
fun LiquidGlassDialogContainer(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val palette = LocalLiquidGlassPalette.current
    val shape = RoundedCornerShape(28.dp)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .clip(shape)
                .background(Color(0xF00B1325))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.14f),
                            palette.accent.copy(alpha = 0.06f),
                            Color.White.copy(alpha = 0.04f)
                        )
                    )
                )
                .border(
                    width = 1.2.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.52f),
                            palette.accent.copy(alpha = 0.42f),
                            Color.White.copy(alpha = 0.15f)
                        )
                    ),
                    shape = shape
                )
                .padding(20.dp)
        ) {
            content()
        }
    }
}

/**
 * Auto-detected single-choice Liquid Glass Picker Modal (matches the UNPACKED PAKS dialog screenshot).
 */
@Composable
fun LiquidAutoPickerDialog(
    title: String,
    folderHint: String,
    entries: List<PickerEntry>,
    onSelect: (PickerEntry) -> Unit,
    onRefresh: () -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalLiquidGlassPalette.current

    LiquidGlassDialogContainer(onDismiss = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp
                        ),
                        color = palette.textPrimary
                    )
                    Text(
                        text = folderHint,
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonoFontFamily),
                        color = palette.accent.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.size(44.dp).testTag("dialog_refresh_btn")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Rescan folder",
                        tint = palette.accent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            if (entries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "No files detected yet",
                            style = MaterialTheme.typography.titleSmall,
                            color = palette.warning
                        )
                        Text(
                            text = "Place your files inside:\n$folderHint\nThen tap the refresh icon above.",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(entries, key = { it.file.absolutePath }) { item ->
                        val itemShape = RoundedCornerShape(16.dp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(itemShape)
                                .background(Color.White.copy(alpha = 0.07f))
                                .border(1.dp, Color.White.copy(alpha = 0.14f), itemShape)
                                .clickable { onSelect(item) }
                                .padding(horizontal = 14.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (item.isFolder) Icons.Filled.Folder else Icons.Filled.FolderZip,
                                contentDescription = null,
                                tint = palette.accent,
                                modifier = Modifier.size(21.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = palette.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (item.subtitle.isNotBlank()) {
                                    Text(
                                        text = item.subtitle,
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
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("picker_cancel_btn")
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelLarge,
                        color = palette.accent
                    )
                }
            }
        }
    }
}

/**
 * Multi-file auto-detected picker for Lua Studio (supports selecting individual files or All).
 */
@Composable
fun LiquidMultiFilePickerDialog(
    title: String,
    folderHint: String,
    files: List<WorkspaceFileItem>,
    confirmLabel: String,
    onConfirmSelection: (List<File>) -> Unit,
    onRefresh: () -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalLiquidGlassPalette.current
    var selected by remember(files) { mutableStateOf(files.map { it.file.absolutePath }.toSet()) }

    LiquidGlassDialogContainer(onDismiss = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleLarge, color = palette.textPrimary)
                    Text(folderHint, style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonoFontFamily), color = palette.accent)
                }
                IconButton(onClick = onRefresh, modifier = Modifier.size(44.dp)) {
                    Icon(Icons.Filled.Refresh, "Rescan folder", tint = palette.accent, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.height(12.dp))

            if (files.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "No .lua/.luac files found in $folderHint.\nCopy files there and tap refresh.",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${selected.size}/${files.size} selected", style = MaterialTheme.typography.labelSmall, color = palette.textMuted)
                    TextButton(
                        onClick = {
                            selected = if (selected.size == files.size) emptySet()
                            else files.map { it.file.absolutePath }.toSet()
                        }
                    ) {
                        Text(if (selected.size == files.size) "Clear All" else "Select All", color = palette.accent)
                    }
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 260.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(files, key = { it.file.absolutePath }) { item ->
                        val checked = item.file.absolutePath in selected
                        val shape = RoundedCornerShape(14.dp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(shape)
                                .background(if (checked) palette.accent.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.05f))
                                .border(1.dp, if (checked) palette.accent else Color.White.copy(alpha = 0.12f), shape)
                                .clickable {
                                    selected = if (checked) selected - item.file.absolutePath else selected + item.file.absolutePath
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = {
                                    selected = if (checked) selected - item.file.absolutePath else selected + item.file.absolutePath
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = palette.accent,
                                    checkmarkColor = Color(0xFF041018)
                                )
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, style = MaterialTheme.typography.bodyMedium, color = palette.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(WorkspaceManager.formatSize(item.sizeBytes), style = MaterialTheme.typography.labelSmall, color = palette.textMuted)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = palette.textSecondary)
                }
                Spacer(Modifier.width(8.dp))
                LiquidGlassButton(
                    text = confirmLabel,
                    onClick = {
                        val chosen = files.filter { it.file.absolutePath in selected }.map { it.file }
                        onConfirmSelection(chosen)
                    },
                    enabled = selected.isNotEmpty(),
                    modifier = Modifier.testTag("multi_picker_confirm_btn")
                )
            }
        }
    }
}

/**
 * Confirmation & options dialog ("Do you want to proceed?") before running an action.
 */
@Composable
fun LiquidConfirmActionDialog(
    title: String,
    message: String,
    confirmText: String,
    accentOverride: Color? = null,
    canConfirm: Boolean = true,
    extraContent: (@Composable () -> Unit)? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalLiquidGlassPalette.current

    LiquidGlassDialogContainer(onDismiss = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = accentOverride ?: palette.accent,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = palette.textPrimary
                )
            }

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textSecondary
            )

            extraContent?.invoke()

            Spacer(Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("confirm_cancel_btn")
                ) {
                    Text("No / Cancel", color = palette.textSecondary)
                }
                Spacer(Modifier.width(8.dp))
                LiquidGlassButton(
                    text = confirmText,
                    onClick = onConfirm,
                    enabled = canConfirm,
                    accentOverride = accentOverride,
                    modifier = Modifier.testTag("confirm_yes_btn")
                )
            }
        }
    }
}

/**
 * First-launch / missing-permission Liquid Glass prompt for Internal Storage/MAJLISH_TOOL.
 */
@Composable
fun StoragePermissionDialog(
    onGrantClick: () -> Unit,
    onLaterClick: () -> Unit
) {
    val palette = LocalLiquidGlassPalette.current
    LiquidGlassDialogContainer(onDismiss = onLaterClick) {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Storage, null, tint = palette.accent, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Create MAJLISH_TOOL Folder",
                    style = MaterialTheme.typography.titleLarge,
                    color = palette.textPrimary
                )
            }
            Text(
                text = "Allow storage access so MAJLISH automatically creates Internal Storage/MAJLISH_TOOL with PAK, UNPACK, REPACK, RESULT, EXE, PAK TOOL, and LUA TOOL folders and auto-detects your files.",
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textSecondary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onLaterClick) {
                    Text("Not Now", color = palette.textSecondary)
                }
                Spacer(Modifier.width(8.dp))
                LiquidGlassButton(
                    text = "Allow Access",
                    onClick = onGrantClick,
                    modifier = Modifier.testTag("grant_storage_access_btn")
                )
            }
        }
    }
}

@Composable
fun RepackModeSelectorBlock(
    selectedMode: RepackResolveMode,
    onSelectMode: (RepackResolveMode) -> Unit
) {
    val palette = LocalLiquidGlassPalette.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "CHOOSE REPACK METHOD",
            style = MaterialTheme.typography.labelSmall,
            color = palette.textMuted
        )
        listOf(
            Triple(RepackResolveMode.FOLDER_WISE, "1. Folder-wise Path", "Match exact folder path inside edited/"),
            Triple(RepackResolveMode.INDEX_CSV, "2. index.csv Auto", "Match filenames automatically via EXE/index.csv"),
            Triple(RepackResolveMode.IN_PLACE_BLOCK, "3. In-Place Block", "Auto-detect MINI_OBB / OBBZSDIC / GAMEPATCH")
        ).forEach { (mode, label, desc) ->
            val active = selectedMode == mode
            val shape = RoundedCornerShape(14.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(if (active) palette.accent.copy(alpha = 0.20f) else Color.White.copy(alpha = 0.05f))
                    .border(1.dp, if (active) palette.accent else Color.White.copy(alpha = 0.14f), shape)
                    .clickable { onSelectMode(mode) }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(label, style = MaterialTheme.typography.labelLarge, color = if (active) palette.accent else palette.textPrimary)
                    Text(desc, style = MaterialTheme.typography.labelSmall, color = palette.textSecondary)
                }
                if (active) {
                    Icon(Icons.Filled.CheckCircle, null, tint = palette.accent, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun TargetPathInputBlock(
    targetPath: String,
    onUpdatePath: (String) -> Unit
) {
    val palette = LocalLiquidGlassPalette.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("TARGET IN-PAK PATH", style = MaterialTheme.typography.labelSmall, color = palette.textMuted)
        LiquidGlassTextField(
            value = targetPath,
            onValueChange = onUpdatePath,
            placeholder = "Content/Lua/GameLua/Mod/BRMod/Gameplay/Core",
            leadingIcon = Icons.Filled.Route,
            monospace = true,
            modifier = Modifier.testTag("dialog_target_path_input")
        )
    }
}

@Composable
fun Sm4EncryptOptionBlock(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val palette = LocalLiquidGlassPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, palette.accent.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(Icons.Filled.Security, null, tint = palette.accent, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Column {
                Text("Encrypt with SM4 (Type 47)", style = MaterialTheme.typography.labelLarge, color = palette.textPrimary)
                Text("Game-native encryption (recommended)", style = MaterialTheme.typography.labelSmall, color = palette.textSecondary)
            }
        }
        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF041018),
                checkedTrackColor = palette.accent
            )
        )
    }
}

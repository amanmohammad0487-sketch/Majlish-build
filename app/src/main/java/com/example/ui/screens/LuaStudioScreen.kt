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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.lua.LuaBytecodeEngine
import com.example.core.workspace.WorkspaceFileItem
import com.example.core.workspace.WorkspaceManager
import com.example.ui.components.ToolActionFlow
import com.example.ui.theme.LiquidGlassButton
import com.example.ui.theme.LiquidGlassCard
import com.example.ui.theme.LiquidGlassTextField
import com.example.ui.theme.LocalLiquidGlassPalette
import com.example.ui.theme.MonoFontFamily
import com.example.viewmodel.LuaStudioMode
import com.example.viewmodel.MajlishUiState
import java.io.File

@Composable
fun LuaStudioScreen(
    uiState: MajlishUiState,
    onSelectMode: (LuaStudioMode) -> Unit,
    onOpenActionFlow: (ToolActionFlow) -> Unit,
    onRefreshWorkspace: () -> Unit,
    onOpenInEditor: (File) -> Unit,
    onCopyDecriptToEdit: (File) -> Unit,
    onUpdateEditorContent: (String) -> Unit,
    onSaveEditorToEditLua: () -> Unit,
    onSaveXorKeyHex: (String) -> Unit,
    bottomContentPadding: Dp
) {
    val palette = LocalLiquidGlassPalette.current
    var xorKeyInput by remember(uiState.customLuaXorHex) {
        mutableStateOf(uiState.customLuaXorHex.ifBlank { LuaBytecodeEngine.DEFAULT_LUA_XOR_KEY_HEX })
    }

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
                    Text("Lua Studio", style = MaterialTheme.typography.headlineMedium, color = palette.textPrimary)
                    Text(
                        text = "${uiState.storagePathLabel}/LUA TOOL",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonoFontFamily),
                        color = palette.accent
                    )
                }
                IconButton(onClick = onRefreshWorkspace, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Filled.Refresh, "Rescan LUA TOOL", tint = palette.accent, modifier = Modifier.size(22.dp))
                }
            }
        }

        // Segmented Mode Selector
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LuaStudioMode.entries.forEach { mode ->
                    val selected = uiState.luaStudioMode == mode
                    val shape = RoundedCornerShape(100.dp)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(shape)
                            .background(if (selected) palette.accent.copy(alpha = 0.24f) else Color.White.copy(alpha = 0.06f))
                            .border(1.dp, if (selected) palette.accent else Color.White.copy(alpha = 0.15f), shape)
                            .clickable { onSelectMode(mode) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode.label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (selected) palette.accent else palette.textSecondary,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        when (uiState.luaStudioMode) {
            LuaStudioMode.DECOMPILE -> {
                item {
                    LiquidGlassCard(modifier = Modifier.fillMaxWidth(), elevated = true) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column {
                                Text("Decompile Lua", style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                                Text(
                                    "MAJLISH_TOOL/LUA TOOL/Lua_org → Lua_Decript",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonoFontFamily),
                                    color = palette.accent
                                )
                            }

                            LuaAutoDetectedPreview(
                                files = uiState.luaOrgFiles,
                                emptyMessage = "No .lua/.luac files detected in MAJLISH_TOOL/LUA TOOL/Lua_org/ yet."
                            )

                            LiquidGlassButton(
                                text = "Select Files to Decompile (${uiState.luaOrgFiles.size})",
                                onClick = { onOpenActionFlow(ToolActionFlow.PickLuaDecompile) },
                                icon = Icons.Filled.Code,
                                enabled = !uiState.isBusy,
                                loading = uiState.isBusy,
                                modifier = Modifier.fillMaxWidth().testTag("run_lua_decompile_btn")
                            )
                        }
                    }
                }

                if (uiState.luaDecriptFiles.isNotEmpty()) {
                    item {
                        LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("DECOMPILED OUTPUTS (Lua_Decript)", style = MaterialTheme.typography.labelMedium, color = palette.accent)
                                uiState.luaDecriptFiles.forEach { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color.White.copy(alpha = 0.05f))
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(item.name, style = MaterialTheme.typography.bodyMedium, color = palette.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(WorkspaceManager.formatSize(item.sizeBytes), style = MaterialTheme.typography.labelSmall, color = palette.textMuted)
                                        }
                                        if (item.name.endsWith(".lua", true)) {
                                            IconButton(onClick = { onOpenInEditor(item.file) }, modifier = Modifier.size(44.dp)) {
                                                Icon(Icons.Filled.Edit, "Edit Lua", tint = palette.accent, modifier = Modifier.size(19.dp))
                                            }
                                        }
                                        IconButton(onClick = { onCopyDecriptToEdit(item.file) }, modifier = Modifier.size(44.dp)) {
                                            Icon(Icons.Filled.ContentCopy, "Copy to EDIT_LUA", tint = palette.accentSecondary, modifier = Modifier.size(19.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            LuaStudioMode.RECOMPILE -> {
                item {
                    LiquidGlassCard(modifier = Modifier.fillMaxWidth(), elevated = true) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column {
                                Text("Recompile & Encrypt Lua", style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                                Text(
                                    "MAJLISH_TOOL/LUA TOOL/EDIT_LUA → Lua_Rueslt",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonoFontFamily),
                                    color = palette.accent
                                )
                            }

                            LuaAutoDetectedPreview(
                                files = uiState.luaEditFiles,
                                emptyMessage = "No .lua/.luac files detected in MAJLISH_TOOL/LUA TOOL/EDIT_LUA/ yet."
                            )

                            LiquidGlassButton(
                                text = "Select Files to Recompile (${uiState.luaEditFiles.size})",
                                onClick = { onOpenActionFlow(ToolActionFlow.PickLuaRecompile) },
                                icon = Icons.Filled.Build,
                                enabled = !uiState.isBusy,
                                loading = uiState.isBusy,
                                modifier = Modifier.fillMaxWidth().testTag("run_lua_recompile_btn")
                            )
                        }
                    }
                }

                if (uiState.luaResultFiles.isNotEmpty()) {
                    item {
                        LiquidGlassCard(modifier = Modifier.fillMaxWidth(), accentTint = palette.success) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("GAME-READY OUTPUTS (LUA TOOL/Lua_Rueslt)", style = MaterialTheme.typography.labelMedium, color = palette.success)
                                uiState.luaResultFiles.forEach { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color.White.copy(alpha = 0.05f))
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = palette.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = WorkspaceManager.formatSize(item.sizeBytes),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = palette.success
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            LuaStudioMode.EDITOR -> {
                item {
                    LiquidGlassCard(modifier = Modifier.fillMaxWidth(), elevated = true) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column {
                                Text(
                                    text = uiState.editorFileName.ifBlank { "Lua Code Scratchpad" },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = palette.textPrimary
                                )
                                Text(
                                    "Edit decompiled Lua & save directly to MAJLISH_TOOL/LUA TOOL/EDIT_LUA/",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.textSecondary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 220.dp, max = 360.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xB8040710))
                                    .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(16.dp))
                                    .padding(14.dp)
                            ) {
                                if (uiState.editorContent.isEmpty()) {
                                    Text(
                                        text = "-- Open any decompiled .lua file or write Lua code here…",
                                        style = TextStyle(fontFamily = MonoFontFamily, fontSize = 12.5.sp),
                                        color = palette.textMuted
                                    )
                                }
                                BasicTextField(
                                    value = uiState.editorContent,
                                    onValueChange = onUpdateEditorContent,
                                    textStyle = TextStyle(
                                        fontFamily = MonoFontFamily,
                                        fontSize = 12.5.sp,
                                        lineHeight = 18.sp,
                                        color = palette.textPrimary
                                    ),
                                    cursorBrush = SolidColor(palette.accent),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            LiquidGlassButton(
                                text = "Save to EDIT_LUA/",
                                onClick = onSaveEditorToEditLua,
                                icon = Icons.Filled.Save,
                                enabled = uiState.editorContent.isNotBlank(),
                                modifier = Modifier.fillMaxWidth().testTag("save_lua_editor_btn")
                            )
                        }
                    }
                }

                item {
                    LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("LUA XOR KEY (HEX)", style = MaterialTheme.typography.labelMedium, color = palette.accent)
                            Text(
                                text = "Reads/writes MAJLISH_TOOL/LUA TOOL/lua_xor_key.txt automatically.",
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.textSecondary
                            )
                            LiquidGlassTextField(
                                value = xorKeyInput,
                                onValueChange = { xorKeyInput = it },
                                placeholder = "Hex XOR key…",
                                leadingIcon = Icons.Filled.Key,
                                monospace = true
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                LiquidGlassButton(
                                    text = "Save Key",
                                    onClick = { onSaveXorKeyHex(xorKeyInput) },
                                    icon = Icons.Filled.Save,
                                    modifier = Modifier.weight(1f)
                                )
                                LiquidGlassButton(
                                    text = "Default",
                                    onClick = {
                                        xorKeyInput = LuaBytecodeEngine.DEFAULT_LUA_XOR_KEY_HEX
                                        onSaveXorKeyHex("")
                                    },
                                    icon = Icons.Filled.Restore,
                                    primary = false,
                                    modifier = Modifier.weight(0.8f)
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
private fun LuaAutoDetectedPreview(
    files: List<WorkspaceFileItem>,
    emptyMessage: String
) {
    val palette = LocalLiquidGlassPalette.current
    if (files.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.04f))
                .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Text(emptyMessage, style = MaterialTheme.typography.bodySmall, color = palette.textMuted)
        }
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        files.take(6).forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = WorkspaceManager.formatSize(item.sizeBytes),
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.accent
                )
            }
        }
        if (files.size > 6) {
            Text(
                text = "+ ${files.size - 6} more auto-detected files",
                style = MaterialTheme.typography.labelSmall,
                color = palette.textMuted
            )
        }
    }
}

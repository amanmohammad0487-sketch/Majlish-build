package com.example

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.FolderSpecial
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.workspace.WorkspaceManager
import com.example.ui.components.LiquidAutoPickerDialog
import com.example.ui.components.LiquidConfirmActionDialog
import com.example.ui.components.LiquidMultiFilePickerDialog
import com.example.ui.components.PickerEntry
import com.example.ui.components.RepackModeSelectorBlock
import com.example.ui.components.Sm4EncryptOptionBlock
import com.example.ui.components.StoragePermissionDialog
import com.example.ui.components.TargetPathInputBlock
import com.example.ui.components.ToolActionFlow
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.LuaStudioScreen
import com.example.ui.screens.PakLabScreen
import com.example.ui.screens.VaultScreen
import com.example.ui.theme.GlassNavDestination
import com.example.ui.theme.LiquidGlassFloatingNavBar
import com.example.ui.theme.LiquidSpatialBackground
import com.example.ui.theme.LocalLiquidGlassPalette
import com.example.ui.theme.MajlishTheme
import com.example.viewmodel.MainTab
import com.example.viewmodel.MajlishUiState
import com.example.viewmodel.MajlishViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )
        setContent {
            MajlishAppRoot()
        }
    }
}

private fun requestSharedStoragePermission(
    context: Context,
    legacyLauncher: (Array<String>) -> Unit
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        runCatching { context.startActivity(intent) }.onFailure {
            runCatching { context.startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)) }
        }
    } else {
        legacyLauncher(
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )
    }
}

@Composable
fun MajlishAppRoot(viewModel: MajlishViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val legacyStorageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.checkAndInitializeStorage(showPromptIfMissing = false)
    }

    // Automatically ensure MAJLISH_TOOL folders & rescan whenever returning to the app
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkAndInitializeStorage(showPromptIfMissing = false)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.consumeSnackbar()
        }
    }

    MajlishTheme(themePreset = uiState.themePreset) {
        val palette = LocalLiquidGlassPalette.current

        LiquidSpatialBackground {
            AnimatedContent(
                targetState = uiState.isAuthenticated,
                transitionSpec = {
                    (fadeIn(tween(380)) + scaleIn(initialScale = 0.96f, animationSpec = tween(380))) togetherWith
                        fadeOut(tween(240))
                },
                label = "auth_transition"
            ) { authenticated ->
                if (!authenticated) {
                    LoginScreen(
                        uiState = uiState,
                        onKeyChange = viewModel::updateKeyInput,
                        onVerifyClick = viewModel::verifyLicenseKey
                    )
                } else {
                    BackHandler(enabled = uiState.activeTab != MainTab.HOME) {
                        viewModel.selectTab(MainTab.HOME)
                    }

                    val navDestinations = listOf(
                        GlassNavDestination(
                            id = MainTab.HOME.id,
                            label = stringResource(R.string.nav_home),
                            icon = Icons.Outlined.Home,
                            activeIcon = Icons.Filled.Home
                        ),
                        GlassNavDestination(
                            id = MainTab.PAK_LAB.id,
                            label = stringResource(R.string.nav_pak),
                            icon = Icons.Outlined.Layers,
                            activeIcon = Icons.Filled.Layers
                        ),
                        GlassNavDestination(
                            id = MainTab.LUA_STUDIO.id,
                            label = stringResource(R.string.nav_lua),
                            icon = Icons.Outlined.Code,
                            activeIcon = Icons.Filled.Code
                        ),
                        GlassNavDestination(
                            id = MainTab.VAULT.id,
                            label = stringResource(R.string.nav_vault),
                            icon = Icons.Outlined.FolderSpecial,
                            activeIcon = Icons.Filled.FolderSpecial
                        )
                    )

                    val triggerGrantStorage = {
                        requestSharedStoragePermission(context) { perms ->
                            legacyStorageLauncher.launch(perms)
                        }
                    }

                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val useRail = maxWidth >= 700.dp
                        val bottomInset = if (useRail) 16.dp else 94.dp

                        Row(modifier = Modifier.fillMaxSize()) {
                            if (useRail) {
                                NavigationRail(
                                    containerColor = Color(0xAA080E1D),
                                    modifier = Modifier.fillMaxHeight().statusBarsPadding()
                                ) {
                                    navDestinations.forEach { dest ->
                                        val selected = uiState.activeTab.id == dest.id
                                        NavigationRailItem(
                                            selected = selected,
                                            onClick = {
                                                MainTab.entries.firstOrNull { it.id == dest.id }
                                                    ?.let(viewModel::selectTab)
                                            },
                                            icon = {
                                                Icon(
                                                    imageVector = if (selected) dest.activeIcon else dest.icon,
                                                    contentDescription = dest.label
                                                )
                                            },
                                            label = { Text(dest.label) },
                                            colors = NavigationRailItemDefaults.colors(
                                                selectedIconColor = palette.accent,
                                                selectedTextColor = palette.accent,
                                                indicatorColor = palette.accent.copy(alpha = 0.22f),
                                                unselectedIconColor = palette.textSecondary,
                                                unselectedTextColor = palette.textSecondary
                                            )
                                        )
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .statusBarsPadding()
                            ) {
                                AnimatedContent(
                                    targetState = uiState.activeTab,
                                    transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(160)) },
                                    label = "tab_content"
                                ) { tab ->
                                    when (tab) {
                                        MainTab.HOME -> HomeScreen(
                                            uiState = uiState,
                                            onOpenActionFlow = viewModel::openActionFlow,
                                            onNavigateTab = viewModel::selectTab,
                                            onRefreshWorkspace = viewModel::refreshWorkspace,
                                            onGrantStorageClick = triggerGrantStorage,
                                            onSignOut = viewModel::signOut,
                                            bottomContentPadding = bottomInset
                                        )

                                        MainTab.PAK_LAB -> PakLabScreen(
                                            uiState = uiState,
                                            onSelectMode = viewModel::selectPakLabMode,
                                            onOpenActionFlow = viewModel::openActionFlow,
                                            onDirectPickUnpack = viewModel::onUnpackPakPicked,
                                            onDirectPickRepack = viewModel::onRepackFolderPicked,
                                            onDirectPickInject = viewModel::onInjectPakPicked,
                                            onDirectPickProtect = viewModel::onProtectPakPicked,
                                            onRefreshWorkspace = viewModel::refreshWorkspace,
                                            onClearLogs = viewModel::clearLogs,
                                            bottomContentPadding = bottomInset
                                        )

                                        MainTab.LUA_STUDIO -> LuaStudioScreen(
                                            uiState = uiState,
                                            onSelectMode = viewModel::selectLuaStudioMode,
                                            onOpenActionFlow = viewModel::openActionFlow,
                                            onRefreshWorkspace = viewModel::refreshWorkspace,
                                            onOpenInEditor = viewModel::openFileInLuaEditor,
                                            onCopyDecriptToEdit = viewModel::copyDecriptToEditLua,
                                            onUpdateEditorContent = viewModel::updateEditorContent,
                                            onSaveEditorToEditLua = viewModel::saveEditorToEditLua,
                                            onSaveXorKeyHex = viewModel::saveCustomLuaXorHex,
                                            bottomContentPadding = bottomInset
                                        )

                                        MainTab.VAULT -> VaultScreen(
                                            uiState = uiState,
                                            onSelectTheme = viewModel::selectTheme,
                                            onOpenDeleteFolderPicker = {
                                                viewModel.openActionFlow(ToolActionFlow.PickDeleteFolder)
                                            },
                                            onRefreshWorkspace = viewModel::refreshWorkspace,
                                            onGrantStorageClick = triggerGrantStorage,
                                            onSignOut = viewModel::signOut,
                                            bottomContentPadding = bottomInset
                                        )
                                    }
                                }

                                if (!useRail) {
                                    LiquidGlassFloatingNavBar(
                                        destinations = navDestinations,
                                        selectedId = uiState.activeTab.id,
                                        onSelect = { id ->
                                            MainTab.entries.firstOrNull { it.id == id }
                                                ?.let(viewModel::selectTab)
                                        },
                                        modifier = Modifier.align(Alignment.BottomCenter)
                                    )
                                }
                            }
                        }
                    }

                    // Auto-Detected Picker & Yes/No Confirmation Modal Host
                    ActionFlowDialogHost(uiState = uiState, viewModel = viewModel)
                }
            }

            // First-open Storage Permission Prompt (creates /storage/emulated/0/MAJLISH_TOOL immediately)
            if (uiState.showStoragePrompt && !uiState.hasStorageAccess) {
                StoragePermissionDialog(
                    onGrantClick = {
                        viewModel.dismissStoragePrompt()
                        requestSharedStoragePermission(context) { perms ->
                            legacyStorageLauncher.launch(perms)
                        }
                    },
                    onLaterClick = viewModel::dismissStoragePrompt
                )
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Color(0xEE10192E),
                    contentColor = palette.textPrimary,
                    actionColor = palette.accent
                )
            }
        }
    }
}

@Composable
private fun ActionFlowDialogHost(
    uiState: MajlishUiState,
    viewModel: MajlishViewModel
) {
    val palette = LocalLiquidGlassPalette.current
    when (val flow = uiState.actionFlow) {
        ToolActionFlow.None -> Unit

        // 1. UNPACK PAK
        ToolActionFlow.PickUnpackPak -> {
            LiquidAutoPickerDialog(
                title = "AVAILABLE PAKS",
                folderHint = "Internal Storage/MAJLISH_TOOL/PAK/",
                entries = uiState.pakFiles.map {
                    PickerEntry(it.name, WorkspaceManager.formatSize(it.sizeBytes), it.file)
                },
                onSelect = { viewModel.onUnpackPakPicked(it.file) },
                onRefresh = viewModel::refreshWorkspace,
                onDismiss = viewModel::closeActionFlow
            )
        }

        is ToolActionFlow.ConfirmUnpackPak -> {
            LiquidConfirmActionDialog(
                title = "Unpack PAK?",
                message = "Selected: ${flow.pakFile.name} (${WorkspaceManager.formatSize(flow.pakFile.length())})\n\nExtract all files, Debug_.log, edited/ & index.csv into:\nMAJLISH_TOOL/UNPACK/${flow.pakFile.nameWithoutExtension}/",
                confirmText = "Yes, Unpack",
                onConfirm = { viewModel.executeUnpackPak(flow.pakFile) },
                onDismiss = viewModel::closeActionFlow
            )
        }

        // 2. REPACK PAK
        ToolActionFlow.PickRepackFolder -> {
            LiquidAutoPickerDialog(
                title = "UNPACKED PAKS",
                folderHint = "Internal Storage/MAJLISH_TOOL/UNPACK/",
                entries = uiState.unpackedFolders.map {
                    PickerEntry(
                        title = it.name,
                        subtitle = "${it.fileCount} files • ${WorkspaceManager.formatSize(it.sizeBytes)}",
                        file = it.file,
                        isFolder = true
                    )
                },
                onSelect = { viewModel.onRepackFolderPicked(it.file, forToPath = false) },
                onRefresh = viewModel::refreshWorkspace,
                onDismiss = viewModel::closeActionFlow
            )
        }

        is ToolActionFlow.ConfirmRepackFolder -> {
            val count = flow.editedFiles.size
            val msg = if (count > 0) {
                "Detected $count edited file(s) in MAJLISH_TOOL/UNPACK/${flow.folder.name}/edited/.\nOutput will be saved to MAJLISH_TOOL/RESULT/${flow.folder.name}.pak."
            } else {
                "No edited files found in MAJLISH_TOOL/UNPACK/${flow.folder.name}/edited/.\nPlace your modified files there first and try again."
            }
            LiquidConfirmActionDialog(
                title = "Repack ${flow.folder.name}?",
                message = msg,
                confirmText = "Yes, Repack",
                canConfirm = count > 0,
                extraContent = if (count > 0) {
                    {
                        RepackModeSelectorBlock(
                            selectedMode = uiState.repackResolveMode,
                            onSelectMode = viewModel::selectRepackResolveMode
                        )
                    }
                } else null,
                onConfirm = { viewModel.executeRepackPak(flow.folder, toManualPath = false) },
                onDismiss = viewModel::closeActionFlow
            )
        }

        // 3. REPACK TO PATH (matches screenshot!)
        ToolActionFlow.PickToPathFolder -> {
            LiquidAutoPickerDialog(
                title = "UNPACKED PAKS",
                folderHint = "Internal Storage/MAJLISH_TOOL/UNPACK/",
                entries = uiState.unpackedFolders.map {
                    PickerEntry(
                        title = it.name,
                        subtitle = "${it.fileCount} files • ${WorkspaceManager.formatSize(it.sizeBytes)}",
                        file = it.file,
                        isFolder = true
                    )
                },
                onSelect = { viewModel.onRepackFolderPicked(it.file, forToPath = true) },
                onRefresh = viewModel::refreshWorkspace,
                onDismiss = viewModel::closeActionFlow
            )
        }

        is ToolActionFlow.ConfirmToPath -> {
            val count = flow.editedFiles.size
            val msg = if (count > 0) {
                "Detected $count file(s) in UNPACK/${flow.folder.name}/edited/.\nChoose the internal target directory inside the PAK:"
            } else {
                "No files found in MAJLISH_TOOL/UNPACK/${flow.folder.name}/edited/.\nPlace files there first."
            }
            LiquidConfirmActionDialog(
                title = "Repack to Path?",
                message = msg,
                confirmText = "Yes, Repack",
                canConfirm = count > 0 && uiState.targetPathInput.isNotBlank(),
                extraContent = if (count > 0) {
                    {
                        TargetPathInputBlock(
                            targetPath = uiState.targetPathInput,
                            onUpdatePath = viewModel::updateTargetPath
                        )
                    }
                } else null,
                onConfirm = { viewModel.executeRepackPak(flow.folder, toManualPath = true) },
                onDismiss = viewModel::closeActionFlow
            )
        }

        // 4. CUSTOM FILE / INJECT
        ToolActionFlow.PickInjectPak -> {
            LiquidAutoPickerDialog(
                title = "AVAILABLE PAKS (INJECT)",
                folderHint = "Internal Storage/MAJLISH_TOOL/PAK TOOL/PAK/",
                entries = uiState.pakToolPakFiles.map {
                    PickerEntry(it.name, WorkspaceManager.formatSize(it.sizeBytes), it.file)
                },
                onSelect = { viewModel.onInjectPakPicked(it.file) },
                onRefresh = viewModel::refreshWorkspace,
                onDismiss = viewModel::closeActionFlow
            )
        }

        is ToolActionFlow.ConfirmInject -> {
            val count = flow.editFiles.size
            val msg = if (count > 0) {
                "Detected $count file(s) in MAJLISH_TOOL/PAK TOOL/EDIT/.\nInject into ${flow.pakFile.name} and save to PAK TOOL/RESULT/${flow.pakFile.name}?"
            } else {
                "No files found in MAJLISH_TOOL/PAK TOOL/EDIT/.\nPlace files at their in-PAK relative path inside PAK TOOL/EDIT/ first."
            }
            LiquidConfirmActionDialog(
                title = "Inject Custom Files?",
                message = msg,
                confirmText = "Yes, Inject",
                canConfirm = count > 0,
                extraContent = if (count > 0) {
                    {
                        Sm4EncryptOptionBlock(
                            enabled = uiState.injectEncryptSm4,
                            onToggle = viewModel::toggleInjectEncryptSm4
                        )
                    }
                } else null,
                onConfirm = { viewModel.executeInjectFiles(flow.pakFile) },
                onDismiss = viewModel::closeActionFlow
            )
        }

        // 5. ENCRYPT / PROTECT PAK
        ToolActionFlow.PickProtectPak -> {
            LiquidAutoPickerDialog(
                title = "SELECT PAK TO PROTECT",
                folderHint = "Internal Storage/MAJLISH_TOOL/PAK TOOL/PAK/",
                entries = uiState.pakToolPakFiles.map {
                    PickerEntry(it.name, WorkspaceManager.formatSize(it.sizeBytes), it.file)
                },
                onSelect = { viewModel.onProtectPakPicked(it.file) },
                onRefresh = viewModel::refreshWorkspace,
                onDismiss = viewModel::closeActionFlow
            )
        }

        is ToolActionFlow.ConfirmProtect -> {
            LiquidConfirmActionDialog(
                title = "Start SM4 Protect?",
                message = "Re-encrypt EVERY entry in ${flow.pakFile.name} using game-native SM4 (Type 47)?\n\nOutput keeps the exact same filename inside:\nMAJLISH_TOOL/PAK TOOL/RESULT/${flow.pakFile.name}",
                confirmText = "Yes, Protect",
                accentOverride = Color(0xFFF472B6),
                onConfirm = { viewModel.executeProtectPak(flow.pakFile) },
                onDismiss = viewModel::closeActionFlow
            )
        }

        // 6. DELETE FOLDER
        ToolActionFlow.PickDeleteFolder -> {
            LiquidAutoPickerDialog(
                title = "AVAILABLE FOLDERS",
                folderHint = "Internal Storage/MAJLISH_TOOL/UNPACK/",
                entries = uiState.deletableFolders.map {
                    PickerEntry(
                        title = it.relativePath,
                        subtitle = "${it.fileCount} files • ${WorkspaceManager.formatSize(it.sizeBytes)}",
                        file = it.file,
                        isFolder = true
                    )
                },
                onSelect = { entry ->
                    uiState.deletableFolders.firstOrNull { it.file.absolutePath == entry.file.absolutePath }
                        ?.let(viewModel::onDeleteFolderPicked)
                },
                onRefresh = viewModel::refreshWorkspace,
                onDismiss = viewModel::closeActionFlow
            )
        }

        is ToolActionFlow.ConfirmDeleteFolder -> {
            LiquidConfirmActionDialog(
                title = "Delete Folder?",
                message = "Permanently delete ${flow.folder.relativePath} (${flow.folder.fileCount} files, ${WorkspaceManager.formatSize(flow.folder.sizeBytes)})?",
                confirmText = "Yes, Delete",
                accentOverride = palette.error,
                onConfirm = { viewModel.executeDeleteFolder(flow.folder.file) },
                onDismiss = viewModel::closeActionFlow
            )
        }

        // 7A. LUA DECOMPILE
        ToolActionFlow.PickLuaDecompile -> {
            LiquidMultiFilePickerDialog(
                title = "LUA FILES (Lua_org)",
                folderHint = "MAJLISH_TOOL/LUA TOOL/Lua_org/",
                files = uiState.luaOrgFiles,
                confirmLabel = "Continue",
                onConfirmSelection = viewModel::onLuaDecompilePicked,
                onRefresh = viewModel::refreshWorkspace,
                onDismiss = viewModel::closeActionFlow
            )
        }

        is ToolActionFlow.ConfirmLuaDecompile -> {
            LiquidConfirmActionDialog(
                title = "Decompile ${flow.files.size} File(s)?",
                message = "Decompile selected Lua file(s) from Lua_org into:\nMAJLISH_TOOL/LUA TOOL/Lua_Decript/",
                confirmText = "Yes, Decompile",
                onConfirm = { viewModel.executeDecompileLua(flow.files) },
                onDismiss = viewModel::closeActionFlow
            )
        }

        // 7B. LUA RECOMPILE
        ToolActionFlow.PickLuaRecompile -> {
            LiquidMultiFilePickerDialog(
                title = "EDIT LUA FILES (EDIT_LUA)",
                folderHint = "MAJLISH_TOOL/LUA TOOL/EDIT_LUA/",
                files = uiState.luaEditFiles,
                confirmLabel = "Continue",
                onConfirmSelection = viewModel::onLuaRecompilePicked,
                onRefresh = viewModel::refreshWorkspace,
                onDismiss = viewModel::closeActionFlow
            )
        }

        is ToolActionFlow.ConfirmLuaRecompile -> {
            LiquidConfirmActionDialog(
                title = "Recompile ${flow.files.size} File(s)?",
                message = "Recompile & XOR-encrypt selected file(s) into game-ready Lua inside:\nMAJLISH_TOOL/LUA TOOL/Lua_Rueslt/",
                confirmText = "Yes, Recompile",
                onConfirm = { viewModel.executeRecompileLua(flow.files) },
                onDismiss = viewModel::closeActionFlow
            )
        }
    }
}

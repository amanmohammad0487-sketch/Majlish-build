package com.example.core.workspace

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat
import java.io.File

data class WorkspaceFolderInfo(
    val name: String,
    val relativePath: String,
    val file: File,
    val fileCount: Int,
    val sizeBytes: Long,
    val isProtectedSystemDir: Boolean
)

data class WorkspaceFileItem(
    val name: String,
    val relativePath: String,
    val file: File,
    val sizeBytes: Long,
    val lastModified: Long
)

/**
 * Manages the shared Internal Storage folder:
 *   /storage/emulated/0/MAJLISH_TOOL
 * Automatically creates every directory from APP.py on startup and auto-detects
 * .pak, unpacked folders, edited files, and .lua/.luac scripts directly.
 */
class WorkspaceManager(private val context: Context) {

    companion object {
        const val ROOT_FOLDER_NAME = "MAJLISH_TOOL"

        fun formatSize(bytes: Long): String {
            var value = bytes.toDouble()
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            var idx = 0
            while (value >= 1024.0 && idx < units.lastIndex) {
                value /= 1024.0
                idx++
            }
            return if (idx == 0) "$bytes B" else "%.2f %s".format(value, units[idx])
        }
    }

    fun hasAllFilesAccess(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Points directly to /storage/emulated/0/MAJLISH_TOOL when storage permission is granted,
     * falling back to app external files dir only if permission hasn't been granted yet.
     */
    val rootDir: File
        get() {
            return if (hasAllFilesAccess()) {
                File(Environment.getExternalStorageDirectory(), ROOT_FOLDER_NAME)
            } else {
                val fallback = context.getExternalFilesDir(null) ?: context.filesDir
                File(fallback, ROOT_FOLDER_NAME)
            }
        }

    val displayRootPath: String
        get() = if (hasAllFilesAccess()) {
            "Internal Storage/$ROOT_FOLDER_NAME"
        } else {
            "Storage permission needed for /$ROOT_FOLDER_NAME"
        }

    val pakDir: File get() = File(rootDir, "PAK")
    val unpackDir: File get() = File(rootDir, "UNPACK")
    val repackDir: File get() = File(rootDir, "REPACK")
    val resultDir: File get() = File(rootDir, "RESULT")
    val exeDir: File get() = File(rootDir, "EXE")

    val pakToolPakDir: File get() = File(rootDir, "PAK TOOL/PAK")
    val pakToolEditDir: File get() = File(rootDir, "PAK TOOL/EDIT")
    val pakToolUnpackDir: File get() = File(rootDir, "PAK TOOL/UNPACK")
    val pakToolResultDir: File get() = File(rootDir, "PAK TOOL/RESULT")

    val luaBaseDir: File get() = File(rootDir, "LUA TOOL")
    val luaOrgDir: File get() = File(rootDir, "LUA TOOL/Lua_org")
    val luaDecriptDir: File get() = File(rootDir, "LUA TOOL/Lua_Decript")
    val luaEditDir: File get() = File(rootDir, "LUA TOOL/EDIT_LUA")
    val luaResultDir: File get() = File(rootDir, "LUA TOOL/Lua_Rueslt")

    private val protectedRootNames = setOf(
        "PAK", "UNPACK", "REPACK", "RESULT", "PAK TOOL", "EXE", "LUA TOOL"
    )

    /**
     * Creates MAJLISH_TOOL and every single subfolder from APP.py automatically.
     */
    fun ensureDirectories(): Boolean {
        val dirs = listOf(
            rootDir,
            pakDir, unpackDir, repackDir, resultDir, exeDir,
            pakToolPakDir, pakToolEditDir, pakToolUnpackDir, pakToolResultDir,
            luaOrgDir, luaDecriptDir, luaEditDir, luaResultDir
        )
        var allOk = true
        for (d in dirs) {
            if (!d.exists()) {
                val created = d.mkdirs()
                if (!created && !d.exists()) allOk = false
            }
        }
        return allOk
    }

    fun listPakFiles(dir: File): List<WorkspaceFileItem> =
        listFilesIn(dir, recursive = false)
            .filter { it.name.endsWith(".pak", ignoreCase = true) }
            .sortedBy { it.name.lowercase() }

    fun listPakToolAndMainPaks(): List<WorkspaceFileItem> {
        val primary = listPakFiles(pakToolPakDir)
        val fallback = listPakFiles(pakDir)
        return (primary + fallback).distinctBy { it.file.absolutePath }
    }

    fun listUnpackedPaks(): List<WorkspaceFolderInfo> {
        if (!unpackDir.exists()) return emptyList()
        return unpackDir.listFiles()
            ?.filter { it.isDirectory }
            ?.sortedBy { it.name.lowercase() }
            ?.map { folder ->
                val (count, size) = measureDir(folder)
                WorkspaceFolderInfo(
                    name = folder.name,
                    relativePath = "UNPACK/${folder.name}",
                    file = folder,
                    fileCount = count,
                    sizeBytes = size,
                    isProtectedSystemDir = false
                )
            } ?: emptyList()
    }

    fun listEditedFilesForUnpack(unpackFolder: File): List<WorkspaceFileItem> {
        val editedDir = File(unpackFolder, "edited").apply { if (!exists()) mkdirs() }
        return listFilesIn(editedDir, recursive = true)
            .filter {
                val lower = it.name.lowercase()
                lower !in setOf("thumbs.db", ".ds_store", "desktop.ini") && !it.name.startsWith("Debug_")
            }
    }

    fun listFilesIn(dir: File, recursive: Boolean = true): List<WorkspaceFileItem> {
        if (!dir.exists()) return emptyList()
        val seq = if (recursive) {
            dir.walkTopDown().filter { it.isFile }
        } else {
            dir.listFiles()?.asSequence()?.filter { it.isFile } ?: emptySequence()
        }
        return seq
            .sortedBy { it.name.lowercase() }
            .map { f ->
                WorkspaceFileItem(
                    name = f.name,
                    relativePath = runCatching { f.relativeTo(dir).path.replace('\\', '/') }.getOrDefault(f.name),
                    file = f,
                    sizeBytes = f.length(),
                    lastModified = f.lastModified()
                )
            }
            .toList()
    }

    fun listAllOutputPaks(): List<WorkspaceFileItem> =
        (listPakFiles(resultDir) + listPakFiles(pakToolResultDir)).sortedByDescending { it.lastModified }

    fun listWorkspaceFolders(): List<WorkspaceFolderInfo> {
        ensureDirectories()
        val dirs = listOf(
            pakDir, unpackDir, repackDir, resultDir, exeDir,
            pakToolPakDir, pakToolEditDir, pakToolUnpackDir, pakToolResultDir,
            luaOrgDir, luaDecriptDir, luaEditDir, luaResultDir
        )
        return dirs.map { d ->
            val (count, size) = measureDir(d)
            val rel = runCatching { d.relativeTo(rootDir).path.replace('\\', '/') }.getOrDefault(d.name)
            WorkspaceFolderInfo(
                name = rel,
                relativePath = rel,
                file = d,
                fileCount = count,
                sizeBytes = size,
                isProtectedSystemDir = true
            )
        }
    }

    fun listDeletableFolders(): List<WorkspaceFolderInfo> {
        ensureDirectories()
        val result = mutableListOf<WorkspaceFolderInfo>()
        rootDir.listFiles()
            ?.filter { it.isDirectory && it.name !in protectedRootNames && !it.name.startsWith(".") }
            ?.forEach { f ->
                val (count, size) = measureDir(f)
                result.add(WorkspaceFolderInfo(f.name, f.name, f, count, size, false))
            }
        result.addAll(listUnpackedPaks())
        return result.sortedBy { it.relativePath.lowercase() }
    }

    fun deleteFolder(folder: File): Boolean {
        if (!folder.exists()) return false
        val canonicalRoot = rootDir.canonicalPath
        val canonicalTarget = folder.canonicalPath
        if (!canonicalTarget.startsWith(canonicalRoot) || canonicalTarget == canonicalRoot) return false
        return folder.deleteRecursively()
    }

    private fun measureDir(dir: File): Pair<Int, Long> {
        var count = 0
        var size = 0L
        dir.walkTopDown().forEach {
            if (it.isFile) {
                count++
                size += it.length()
            }
        }
        return count to size
    }
}

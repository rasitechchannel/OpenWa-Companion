package org.rasitech.openwacompanion.engine

import android.content.Context
import android.util.Log
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicBoolean
import java.util.zip.ZipInputStream
import kotlin.concurrent.thread

/**
 * Thin JNI façade over embedded libnode.so.
 * Auth keys and Baileys payloads must never surface past the engine adapter layer.
 */
object NodeBridge {
    private const val TAG = "OpenWA-NodeBridge"
    private const val BUNDLE_ASSET = "nodejs/engine-bundle.zip"
    private const val BUNDLE_MARKER = "engine-bundle.sha1"
    private val started = AtomicBoolean(false)
    private val libraryLoaded = AtomicBoolean(false)

    /** Allows restarting the engine after a failed boot (e.g. after bugfix reinstall). */
    fun resetStartGateForTests() {
        started.set(false)
    }

    @JvmStatic
    private external fun nativeNodeCompileVersion(): String

    @JvmStatic
    private external fun nativeStartNodeWithArguments(
        arguments: Array<String>,
        workingDirectory: String,
    ): Int

    fun ensureLibraryLoaded() {
        if (libraryLoaded.compareAndSet(false, true)) {
            System.loadLibrary("openwa-node")
        }
    }

    fun compileTimeNodeVersion(): String {
        ensureLibraryLoaded()
        return nativeNodeCompileVersion()
    }

    fun prepareRuntimeDirs(context: Context): RuntimePaths {
        val files = context.filesDir
        val cache = context.cacheDir
        val noBackup = context.noBackupFilesDir
        val nodeRoot = File(files, "nodejs").apply { mkdirs() }
        val modules = File(nodeRoot, "modules").apply { mkdirs() }
        val tmp = File(cache, "node-tmp").apply { mkdirs() }
        val compileCache = File(cache, "node-compile-cache").apply { mkdirs() }
        val authRoot = File(noBackup, "accounts").apply { mkdirs() }
        val bridgeDir = File(nodeRoot, "bridge").apply { mkdirs() }
        return RuntimePaths(
            nodeRoot = nodeRoot,
            modules = modules,
            tmp = tmp,
            compileCache = compileCache,
            authRoot = authRoot,
            home = files,
            bridgeDir = bridgeDir,
        )
    }

    fun statusFile(context: Context): File =
        File(prepareRuntimeDirs(context).bridgeDir, "status.json")

    fun writeCommand(context: Context, json: String) {
        val cmd = File(prepareRuntimeDirs(context).bridgeDir, "command.json")
        cmd.writeText(json)
    }

    fun ensureEngineBundle(context: Context, paths: RuntimePaths) {
        val am = context.assets
        val marker = File(paths.nodeRoot, BUNDLE_MARKER)
        val assetSha = runCatching {
            am.open("$BUNDLE_ASSET.sha1").bufferedReader().readText().trim()
        }.getOrDefault("unversioned")

        val mainJs = File(paths.nodeRoot, "main.js")
        val needsExtract = !mainJs.exists() ||
            !File(paths.nodeRoot, "src/main.mjs").exists() ||
            !marker.exists() ||
            marker.readText().trim() != assetSha

        if (!needsExtract) return

        Log.i(TAG, "extracting engine bundle ($assetSha)")
        paths.nodeRoot.listFiles()?.forEach { child ->
            if (child.name != "bridge") {
                child.deleteRecursively()
            }
        }
        paths.nodeRoot.mkdirs()

        am.open(BUNDLE_ASSET).use { input ->
            ZipInputStream(BufferedInputStream(input)).use { zis ->
                while (true) {
                    val entry = zis.nextEntry ?: break
                    // Windows-built zips may use backslashes; normalize for Android/Linux FS.
                    val normalized = entry.name.replace('\\', '/').trimStart('/')
                    if (normalized.isEmpty() || normalized.contains("..")) {
                        zis.closeEntry()
                        continue
                    }
                    val outFile = File(paths.nodeRoot, normalized)
                    if (entry.isDirectory || normalized.endsWith('/')) {
                        outFile.mkdirs()
                    } else {
                        outFile.parentFile?.mkdirs()
                        FileOutputStream(outFile).use { output -> zis.copyTo(output) }
                    }
                    zis.closeEntry()
                }
            }
        }
        marker.writeText(assetSha)
    }

    /**
     * Starts Node on a dedicated thread. node::Start blocks until the process exits.
     */
    fun startEngineAsync(
        context: Context,
        onExit: (Int) -> Unit = {},
    ) {
        if (!started.compareAndSet(false, true)) {
            Log.w(TAG, "engine already starting/started")
            return
        }
        ensureLibraryLoaded()
        val appContext = context.applicationContext
        val paths = prepareRuntimeDirs(appContext)
        ensureEngineBundle(appContext, paths)

        thread(name = "openwa-node", isDaemon = true) {
            try {
                android.system.Os.setenv("TMPDIR", paths.tmp.absolutePath, true)
                android.system.Os.setenv("HOME", paths.home.absolutePath, true)
                android.system.Os.setenv(
                    "NODE_COMPILE_CACHE",
                    paths.compileCache.absolutePath,
                    true,
                )
                android.system.Os.setenv("NODE_COMPILE_CACHE_PORTABLE", "1", true)
                android.system.Os.setenv(
                    "NODE_OPTIONS",
                    "--max-old-space-size-percentage=25",
                    true,
                )

                val entry = File(paths.nodeRoot, "main.js").absolutePath
                val accountId = "default"
                val accountMedia = File(paths.authRoot, "$accountId/media").apply { mkdirs() }
                val args = arrayOf(
                    "node",
                    entry,
                    "--openwa-data",
                    paths.authRoot.absolutePath,
                    "--account-id",
                    accountId,
                    "--media-root",
                    accountMedia.absolutePath,
                )
                val code = nativeStartNodeWithArguments(args, paths.nodeRoot.absolutePath)
                onExit(code)
            } catch (t: Throwable) {
                Log.e(TAG, "engine failed", t)
                started.set(false)
                onExit(-1)
            }
        }
    }

    data class RuntimePaths(
        val nodeRoot: File,
        val modules: File,
        val tmp: File,
        val compileCache: File,
        val authRoot: File,
        val home: File,
        val bridgeDir: File,
    )
}

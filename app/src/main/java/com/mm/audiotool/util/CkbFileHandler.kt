package com.mm.audiotool.util

import android.content.Context
import android.net.Uri
import com.mm.audiotool.model.AudioItem
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Handles all .ckb file operations.
 *
 * A .ckb file is treated as a ZIP archive whose entries are audio files.
 * This handler:
 *  - Extracts audio entries to a temp directory.
 *  - Repacks (with optional replacements) back into a new .ckb file.
 */
object CkbFileHandler {

    private const val TEMP_DIR_NAME = "ckb_temp"

    // ── Extraction ────────────────────────────────────────────────────────────

    /**
     * Opens the .ckb URI as a ZIP stream and extracts every entry to a temp dir.
     * Returns a list of [AudioItem] objects, one per audio entry.
     */
    fun extract(context: Context, uri: Uri): List<AudioItem> {
        val tempDir = getTempDir(context)
        cleanTempDir(context)
        tempDir.mkdirs()

        val items = mutableListOf<AudioItem>()
        var index = 0

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            ZipInputStream(inputStream.buffered()).use { zip ->
                var entry: ZipEntry? = zip.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory && isAudioEntry(entry.name)) {
                        val destFile = File(tempDir, "audio_${index}_${entry.name}")
                        FileOutputStream(destFile).use { out -> zip.copyTo(out) }
                        items.add(
                            AudioItem(
                                id            = index,
                                originalName  = entry.name,
                                extractedFile = destFile
                            )
                        )
                        index++
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        } ?: throw IllegalStateException("Cannot open .ckb file from URI: $uri")

        if (items.isEmpty()) {
            throw IllegalStateException("No audio entries found in the .ckb file.")
        }

        return items
    }

    // ── Repacking ─────────────────────────────────────────────────────────────

    /**
     * Repacks the audio items (using replacements where available) into a new .ckb file.
     * Progress is reported as a Float in [0f, 1f].
     * Returns the absolute path of the output file.
     */
    fun repack(
        context: Context,
        sourceUri: Uri,
        items: List<AudioItem>,
        outputDir: File,
        onProgress: (Float) -> Unit
    ): String {
        outputDir.mkdirs()

        // Derive output filename from source
        val sourceName = getFileName(context, sourceUri) ?: "output.ckb"
        val baseName   = sourceName.removeSuffix(".ckb")
        val outputFile = File(outputDir, "${baseName}_modified.ckb")

        // Build a lookup map: originalName -> replacement file
        val replacements = items
            .filter { it.replacedFile != null && it.isSaved }
            .associate { it.originalName to it.replacedFile!! }

        val total = items.size.toFloat()
        var done  = 0

        ZipOutputStream(FileOutputStream(outputFile).buffered()).use { zos ->
            // Re-open source to preserve non-audio entries and ordering
            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                ZipInputStream(inputStream.buffered()).use { zip ->
                    var entry: ZipEntry? = zip.nextEntry
                    while (entry != null) {
                        val newEntry = ZipEntry(entry.name)
                        zos.putNextEntry(newEntry)

                        val replacement = replacements[entry.name]
                        if (replacement != null && replacement.exists()) {
                            replacement.inputStream().use { it.copyTo(zos) }
                        } else {
                            zip.copyTo(zos)
                        }

                        zos.closeEntry()
                        zip.closeEntry()

                        if (isAudioEntry(entry.name)) {
                            done++
                            onProgress(done / total)
                        }

                        entry = zip.nextEntry
                    }
                }
            }
        }

        return outputFile.absolutePath
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    fun getTempDir(context: Context): File =
        File(context.cacheDir, TEMP_DIR_NAME)

    fun cleanTempDir(context: Context) {
        getTempDir(context).deleteRecursively()
    }

    private fun isAudioEntry(name: String): Boolean {
        val lower = name.lowercase()
        return lower.endsWith(".mp3")  ||
               lower.endsWith(".wav")  ||
               lower.endsWith(".ogg")  ||
               lower.endsWith(".aac")  ||
               lower.endsWith(".flac") ||
               lower.endsWith(".m4a")  ||
               lower.endsWith(".opus")
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val idx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                if (idx >= 0) cursor.getString(idx) else null
            }
        } catch (e: Exception) {
            uri.lastPathSegment
        }
    }
}

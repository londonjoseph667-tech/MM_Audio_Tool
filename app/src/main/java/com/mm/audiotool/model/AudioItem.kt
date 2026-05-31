package com.mm.audiotool.model

import java.io.File

/**
 * Represents a single audio entry extracted from a .ckb archive.
 *
 * @param id          Unique identifier (index in the archive).
 * @param originalName  Original filename inside the archive.
 * @param extractedFile Temporary file on disk after extraction.
 * @param replacedFile  Optional replacement file chosen by the user.
 * @param isPlaying   Whether the audio is currently being previewed.
 * @param isSaved     Whether the user has confirmed the replacement.
 */
data class AudioItem(
    val id: Int,
    val originalName: String,
    val extractedFile: File,
    var replacedFile: File? = null,
    var isPlaying: Boolean = false,
    var isSaved: Boolean = false
)

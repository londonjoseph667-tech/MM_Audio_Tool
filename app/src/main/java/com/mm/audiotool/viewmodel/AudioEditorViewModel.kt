package com.mm.audiotool.viewmodel

import android.app.Application
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mm.audiotool.data.SettingsDataStore.Companion.STORAGE_EXTERNAL
import com.mm.audiotool.model.AudioItem
import com.mm.audiotool.util.CkbFileHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/** UI state for the Audio Editor screen */
sealed class EditorUiState {
    object Idle        : EditorUiState()
    object Loading     : EditorUiState()
    data class Ready(val items: List<AudioItem>) : EditorUiState()
    data class Error(val message: String)        : EditorUiState()
}

class AudioEditorViewModel(
    application: Application,
    private val ckbUri: String,
    private val storageType: String
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<EditorUiState>(EditorUiState.Idle)
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    /** Tracks the current page (0-based) for 6-items-per-view paging */
    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var playingItemId: Int = -1

    /** All extracted audio items (full list) */
    private var allItems: List<AudioItem> = emptyList()

    /** Source .ckb URI kept for repacking */
    val sourceCkbUri: String get() = ckbUri

    init {
        extractCkb()
    }

    // ── Extraction ────────────────────────────────────────────────────────────

    private fun extractCkb() {
        if (ckbUri.isBlank()) return
        _uiState.value = EditorUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val uri     = Uri.parse(ckbUri)
                val items   = CkbFileHandler.extract(context, uri)
                allItems    = items
                _uiState.value = EditorUiState.Ready(pageItems())
            } catch (e: Exception) {
                _uiState.value = EditorUiState.Error(e.message ?: "Extraction failed")
            }
        }
    }

    // ── Paging ────────────────────────────────────────────────────────────────

    private fun pageItems(): List<AudioItem> {
        val start = _currentPage.value * PAGE_SIZE
        return allItems.drop(start).take(PAGE_SIZE)
    }

    fun nextPage() {
        val maxPage = ((allItems.size - 1) / PAGE_SIZE)
        if (_currentPage.value < maxPage) {
            _currentPage.update { it + 1 }
            _uiState.value = EditorUiState.Ready(pageItems())
        }
    }

    fun prevPage() {
        if (_currentPage.value > 0) {
            _currentPage.update { it - 1 }
            _uiState.value = EditorUiState.Ready(pageItems())
        }
    }

    val totalPages: Int get() = if (allItems.isEmpty()) 1 else ((allItems.size - 1) / PAGE_SIZE) + 1

    // ── Playback ──────────────────────────────────────────────────────────────

    fun togglePlayback(item: AudioItem) {
        if (playingItemId == item.id && mediaPlayer?.isPlaying == true) {
            pausePlayback()
        } else {
            startPlayback(item)
        }
    }

    private fun startPlayback(item: AudioItem) {
        stopPlayback()
        val file = item.replacedFile ?: item.extractedFile
        if (!file.exists()) return

        mediaPlayer = MediaPlayer().apply {
            setDataSource(file.absolutePath)
            prepare()
            start()
            setOnCompletionListener { markStopped(item.id) }
        }
        playingItemId = item.id
        updateItemState(item.id) { it.copy(isPlaying = true) }
    }

    private fun pausePlayback() {
        mediaPlayer?.pause()
        updateItemState(playingItemId) { it.copy(isPlaying = false) }
    }

    private fun stopPlayback() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        if (playingItemId != -1) {
            updateItemState(playingItemId) { it.copy(isPlaying = false) }
            playingItemId = -1
        }
    }

    private fun markStopped(id: Int) {
        playingItemId = -1
        updateItemState(id) { it.copy(isPlaying = false) }
    }

    // ── Replace / Save ────────────────────────────────────────────────────────

    fun replaceAudio(itemId: Int, replacementUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            val destDir = CkbFileHandler.getTempDir(context)
            val destFile = File(destDir, "replacement_$itemId.audio")
            context.contentResolver.openInputStream(replacementUri)?.use { input ->
                destFile.outputStream().use { output -> input.copyTo(output) }
            }
            updateItemState(itemId) { it.copy(replacedFile = destFile, isSaved = false) }
        }
    }

    fun saveItem(itemId: Int) {
        updateItemState(itemId) { it.copy(isSaved = true) }
    }

    // ── Repacking ─────────────────────────────────────────────────────────────

    /**
     * Repacks the modified audio items back into a .ckb archive.
     * Returns the absolute path of the saved file.
     */
    suspend fun repack(
        context: Context,
        onProgress: (Float) -> Unit
    ): String = withContext(Dispatchers.IO) {
        val outputDir = resolveOutputDir(context)
        val sourceUri = Uri.parse(ckbUri)
        CkbFileHandler.repack(
            context    = context,
            sourceUri  = sourceUri,
            items      = allItems,
            outputDir  = outputDir,
            onProgress = onProgress
        )
    }

    private fun resolveOutputDir(context: Context): File {
        return if (storageType == STORAGE_EXTERNAL) {
            val ext = Environment.getExternalStorageDirectory()
            File(ext, ROOT_DIR_NAME).also { it.mkdirs() }
        } else {
            File(context.filesDir, ROOT_DIR_NAME).also { it.mkdirs() }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun updateItemState(id: Int, transform: (AudioItem) -> AudioItem) {
        allItems = allItems.map { if (it.id == id) transform(it) else it }
        _uiState.value = EditorUiState.Ready(pageItems())
    }

    override fun onCleared() {
        super.onCleared()
        stopPlayback()
        CkbFileHandler.cleanTempDir(getApplication())
    }

    companion object {
        const val PAGE_SIZE    = 6
        const val ROOT_DIR_NAME = "MM_Audio-Tool"
    }
}

class AudioEditorViewModelFactory(
    private val application: Application,
    private val ckbUri: String,
    private val storageType: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AudioEditorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AudioEditorViewModel(application, ckbUri, storageType) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

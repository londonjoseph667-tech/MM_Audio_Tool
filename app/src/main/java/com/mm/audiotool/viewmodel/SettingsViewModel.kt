package com.mm.audiotool.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mm.audiotool.data.SettingsDataStore
import com.mm.audiotool.data.SettingsDataStore.Companion.STORAGE_INTERNAL
import com.mm.audiotool.data.SettingsDataStore.Companion.THEME_AUTO
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val store = SettingsDataStore(application)

    /** Current theme: "Auto" | "Dark" | "White" */
    val themeMode: StateFlow<String> = store.themeMode.stateIn(
        scope         = viewModelScope,
        started       = SharingStarted.WhileSubscribed(5_000),
        initialValue  = THEME_AUTO
    )

    /** Current storage: "Internal" | "External" */
    val storageType: StateFlow<String> = store.storageType.stateIn(
        scope         = viewModelScope,
        started       = SharingStarted.WhileSubscribed(5_000),
        initialValue  = STORAGE_INTERNAL
    )

    fun setThemeMode(mode: String) {
        viewModelScope.launch { store.setThemeMode(mode) }
    }

    fun setStorageType(type: String) {
        viewModelScope.launch { store.setStorageType(type) }
    }
}

class SettingsViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

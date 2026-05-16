package com.example.studs.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.studs.data.local.LocalPdfEntity
import com.example.studs.data.repository.DriveRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: DriveRepository
) : ViewModel() {

    val recentHistory: StateFlow<List<LocalPdfEntity>> = repository.recentHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarkedFiles: StateFlow<List<LocalPdfEntity>> = repository.bookmarkedFiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lastReadPdf: StateFlow<LocalPdfEntity?> = repository.lastReadPdf
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun toggleBookmark(uiId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            repository.toggleBookmark(uiId, currentStatus)
        }
    }

    companion object {
        fun provideFactory(repository: DriveRepository): ViewModelProvider.Factory = 
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(repository) as T
                }
            }
    }
}

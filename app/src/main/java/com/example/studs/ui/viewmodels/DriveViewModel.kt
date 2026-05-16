package com.example.studs.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.studs.data.model.GitHubContent
import com.example.studs.data.repository.DriveRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

sealed class FolderUiState {
    object Loading : FolderUiState()
    data class Success(val files: List<GitHubContent>) : FolderUiState()
    data class Error(val message: String) : FolderUiState()
}

sealed class PdfUiState {
    object Idle : PdfUiState()
    object Loading : PdfUiState()
    data class Success(val file: File) : PdfUiState()
    data class Error(val message: String) : PdfUiState()
}

class DriveViewModel(
    private val repository: DriveRepository
) : ViewModel() {

    private val _folderState = MutableStateFlow<FolderUiState>(FolderUiState.Loading)
    val folderState: StateFlow<FolderUiState> = _folderState.asStateFlow()

    private val _pdfState = MutableStateFlow<PdfUiState>(PdfUiState.Idle)
    val pdfState: StateFlow<PdfUiState> = _pdfState.asStateFlow()
    
    private val _downloadStatus = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val downloadStatus: StateFlow<Map<String, Boolean>> = _downloadStatus.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    private var searchJob: Job? = null
    private var lastLoadedFolder: String = "ROOT"

    fun loadFolder(folderPath: String) {
        lastLoadedFolder = folderPath
        if (_isSearchActive.value) return // Don't reload folder if search is active

        viewModelScope.launch {
            _folderState.update { FolderUiState.Loading }
            val result = repository.getChildren(folderPath)
            result.onSuccess { files ->
                _folderState.update { FolderUiState.Success(files) }
                val statuses = files.filter { it.isPdf }.associate { it.uiId to repository.isDownloaded(it.name) }
                _downloadStatus.update { current -> current + statuses }
            }.onFailure { error ->
                _folderState.update { FolderUiState.Error(error.message ?: "Unknown error") }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        if (query.isBlank()) {
            loadFolder(lastLoadedFolder)
            return
        }

        searchJob = viewModelScope.launch {
            delay(500) // Debounce
            _folderState.update { FolderUiState.Loading }
            val result = repository.searchFiles(query)
            result.onSuccess { files ->
                _folderState.update { FolderUiState.Success(files) }
            }.onFailure { error ->
                _folderState.update { FolderUiState.Error(error.message ?: "Search failed") }
            }
        }
    }

    fun setSearchActive(active: Boolean) {
        _isSearchActive.value = active
        if (!active) {
            _searchQuery.value = ""
            loadFolder(lastLoadedFolder)
        }
    }

    fun loadPdf(downloadUrl: String, fileName: String, uiId: String, forDownload: Boolean = false) {
        viewModelScope.launch {
            _pdfState.update { PdfUiState.Loading }
            val result = repository.getPdfFile(downloadUrl, fileName, uiId, forDownload)
            result.onSuccess { file ->
                _pdfState.update { PdfUiState.Success(file) }
                if (forDownload) {
                    _downloadStatus.update { it + (uiId to true) }
                }
            }.onFailure { error ->
                _pdfState.update { PdfUiState.Error(error.message ?: "Download failed") }
            }
        }
    }
    
    fun getPdfFlow(uiId: String) = repository.getPdfFlow(uiId)

    fun clearPdfState() {
        _pdfState.update { PdfUiState.Idle }
    }

    fun updateReadingProgress(uiId: String, pageIndex: Int) {
        viewModelScope.launch {
            repository.updateReadingProgress(uiId, pageIndex)
        }
    }

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
                    return DriveViewModel(repository) as T
                }
            }
    }
}

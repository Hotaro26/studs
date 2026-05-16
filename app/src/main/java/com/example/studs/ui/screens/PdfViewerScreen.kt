package com.example.studs.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.compose.AndroidFragment
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.pdf.viewer.fragment.PdfViewerFragment
import com.example.studs.ui.viewmodels.DriveViewModel
import com.example.studs.ui.viewmodels.PdfUiState
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(
    downloadUrl: String,
    fileName: String,
    uiId: String,
    viewModel: DriveViewModel
) {
    val uiState by viewModel.pdfState.collectAsState()
    val downloadStatus by viewModel.downloadStatus.collectAsState()
    val isDownloaded = downloadStatus[uiId] == true
    
    val pdfEntity by viewModel.getPdfFlow(uiId).collectAsState(initial = null)
    val isBookmarked = pdfEntity?.isBookmarked == true

    LaunchedEffect(downloadUrl) {
        viewModel.clearPdfState()
        viewModel.loadPdf(downloadUrl, fileName, uiId, forDownload = false)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(fileName) },
                actions = {
                    IconButton(onClick = { viewModel.toggleBookmark(uiId, isBookmarked) }) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else LocalContentColor.current
                        )
                    }
                    if (!isDownloaded) {
                        IconButton(onClick = { viewModel.loadPdf(downloadUrl, fileName, uiId, forDownload = true) }) {
                            Icon(Icons.Default.Download, contentDescription = "Download PDF")
                        }
                    } else {
                        Text("Saved", modifier = Modifier.padding(end = 16.dp))
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is PdfUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is PdfUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                is PdfUiState.Success -> {
                    // Use AndroidX PDF Viewer Fragment
                    AndroidFragment<PdfViewerFragment>(
                        modifier = Modifier.fillMaxSize(),
                        onUpdate = { fragment ->
                            fragment.documentUri = Uri.fromFile(state.file)
                        }
                    )
                }
                is PdfUiState.Idle -> {}
            }
        }
    }
}

package com.example.studs.ui.screens

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.studs.ui.viewmodels.DriveViewModel
import com.example.studs.ui.viewmodels.PdfUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
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
    
    var isVerticalMode by remember { mutableStateOf(false) }

    LaunchedEffect(downloadUrl) {
        viewModel.clearPdfState()
        viewModel.loadPdf(downloadUrl, fileName, uiId, forDownload = false)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(fileName) },
                actions = {
                    IconButton(onClick = { isVerticalMode = !isVerticalMode }) {
                        Icon(
                            imageVector = if (isVerticalMode) Icons.Default.SwapHoriz else Icons.Default.SwapVert,
                            contentDescription = "Toggle View Mode"
                        )
                    }
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
                    PdfViewer(file = state.file, uiId = uiId, viewModel = viewModel, isVertical = isVerticalMode)
                }
                is PdfUiState.Idle -> {}
            }
        }
    }
}

@Composable
fun PdfViewer(file: File, uiId: String, viewModel: DriveViewModel, isVertical: Boolean) {
    var pdfRenderer by remember { mutableStateOf<PdfRenderer?>(null) }
    var pageCount by remember { mutableIntStateOf(0) }
    val pdfEntity by viewModel.getPdfFlow(uiId).collectAsState(initial = null)
    val rendererMutex = remember { Mutex() }

    DisposableEffect(file) {
        val parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        try {
            val renderer = PdfRenderer(parcelFileDescriptor)
            pdfRenderer = renderer
            pageCount = renderer.pageCount
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        onDispose {
            pdfRenderer?.close()
            parcelFileDescriptor.close()
        }
    }

    if (pageCount > 0) {
        if (isVertical) {
            val listState = rememberLazyListState()
            
            LaunchedEffect(pdfEntity?.lastPageIndex) {
                if (pdfEntity != null && pdfEntity!!.lastPageIndex > 0 && listState.firstVisibleItemIndex == 0) {
                    listState.scrollToItem(pdfEntity!!.lastPageIndex)
                }
            }

            LaunchedEffect(listState) {
                snapshotFlow { listState.firstVisibleItemIndex }
                    .distinctUntilChanged()
                    .collect { pageIndex ->
                        viewModel.updateReadingProgress(uiId, pageIndex)
                    }
            }

            LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
                items(pageCount) { index ->
                    PdfPage(pdfRenderer = pdfRenderer, rendererMutex = rendererMutex, pageIndex = index, isVertical = true)
                }
            }
        } else {
            val pagerState = rememberPagerState(pageCount = { pageCount })
            
            LaunchedEffect(pdfEntity?.lastPageIndex) {
                if (pdfEntity != null && pdfEntity!!.lastPageIndex > 0 && pagerState.currentPage == 0) {
                    pagerState.scrollToPage(pdfEntity!!.lastPageIndex)
                }
            }

            LaunchedEffect(pagerState) {
                snapshotFlow { pagerState.currentPage }
                    .distinctUntilChanged()
                    .collect { pageIndex ->
                        viewModel.updateReadingProgress(uiId, pageIndex)
                    }
            }

            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize(), beyondViewportPageCount = 1) { index ->
                PdfPage(pdfRenderer = pdfRenderer, rendererMutex = rendererMutex, pageIndex = index, isVertical = false)
            }
        }
    }
}

@Composable
fun PdfPage(pdfRenderer: PdfRenderer?, rendererMutex: Mutex, pageIndex: Int, isVertical: Boolean) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(pdfRenderer, pageIndex) {
        withContext(Dispatchers.IO) {
            rendererMutex.withLock {
                pdfRenderer?.let { renderer ->
                    try {
                        ensureActive()
                        val page = renderer.openPage(pageIndex)
                        
                        // Calculate scale - 1.5 is a good balance for tablets
                        val scale = 1.5f
                        val destWidth = (page.width * scale).toInt()
                        val destHeight = (page.height * scale).toInt()
                        
                        val destBitmap = Bitmap.createBitmap(destWidth, destHeight, Bitmap.Config.ARGB_8888)
                        destBitmap.eraseColor(android.graphics.Color.WHITE)
                        
                        ensureActive()
                        page.render(destBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        page.close()
                        bitmap = destBitmap
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(if (isVertical) Modifier.wrapContentHeight() else Modifier),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = "Page ${pageIndex + 1}",
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (isVertical) Modifier.padding(vertical = 8.dp) else Modifier.padding(8.dp)),
                contentScale = ContentScale.Fit
            )
        } else {
            Box(modifier = Modifier.height(400.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

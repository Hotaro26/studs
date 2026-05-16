package com.example.studs.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.activity.ComponentActivity
import com.example.studs.data.local.LocalPdfEntity
import com.example.studs.ui.components.MorphingSurface
import com.example.studs.ui.viewmodels.HomeViewModel
import com.example.studs.theme.PixelBugFont

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToPdf: (String, String, String) -> Unit
) {
    val lastRead by viewModel.lastReadPdf.collectAsState()
    val history by viewModel.recentHistory.collectAsState()
    val bookmarks by viewModel.bookmarkedFiles.collectAsState()
    
    val windowSizeClass = calculateWindowSizeClass(LocalContext.current as ComponentActivity)
    val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
    val isMedium = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Medium

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Studs", 
                        style = MaterialTheme.typography.headlineLarge.copy(fontFamily = PixelBugFont), 
                        fontWeight = FontWeight.Bold
                    ) 
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (isExpanded) Modifier.widthIn(max = 1200.dp) else Modifier)
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                // Continue Reading - Full Width (Constrained on Expanded)
                if (lastRead != null) {
                    Column {
                        Text("Continue Reading", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        ContinueReadingCard(
                            pdf = lastRead!!,
                            onClick = { onNavigateToPdf(lastRead!!.downloadUrl, lastRead!!.fileName, lastRead!!.uiId) }
                        )
                    }
                }

                if (isExpanded || isMedium) {
                    // Tablet Layout: Side-by-side Columns
                    Row(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(32.dp)
                    ) {
                        // Left Column: Bookmarks
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Bookmarks", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            if (bookmarks.isEmpty()) {
                                EmptyState("No bookmarks yet")
                            } else {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    items(bookmarks) { pdf ->
                                        ResponsivePdfTile(
                                            pdf = pdf,
                                            onClick = { onNavigateToPdf(pdf.downloadUrl, pdf.fileName, pdf.uiId) },
                                            onBookmarkClick = { viewModel.toggleBookmark(pdf.uiId, pdf.isBookmarked) }
                                        )
                                    }
                                }
                            }
                        }

                        // Right Column: History
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Recent History", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            if (history.isEmpty()) {
                                EmptyState("No history yet")
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    history.take(3).forEach { pdf ->
                                        ResponsivePdfTile(
                                            pdf = pdf,
                                            onClick = { onNavigateToPdf(pdf.downloadUrl, pdf.fileName, pdf.uiId) },
                                            onBookmarkClick = { viewModel.toggleBookmark(pdf.uiId, pdf.isBookmarked) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Mobile Layout: Sequential Sections
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        if (bookmarks.isNotEmpty()) {
                            item { Text("Bookmarks", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
                            item {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(24.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                ) {
                                    LazyRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentPadding = PaddingValues(16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        items(bookmarks) { pdf ->
                                            PdfCompactCard(
                                                pdf = pdf,
                                                onClick = { onNavigateToPdf(pdf.downloadUrl, pdf.fileName, pdf.uiId) },
                                                onBookmarkClick = { viewModel.toggleBookmark(pdf.uiId, pdf.isBookmarked) }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (history.isNotEmpty()) {
                            item { Text("Recent History", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
                            items(history.take(3)) { pdf ->
                                PdfListTile(
                                    pdf = pdf,
                                    onClick = { onNavigateToPdf(pdf.downloadUrl, pdf.fileName, pdf.uiId) },
                                    onBookmarkClick = { viewModel.toggleBookmark(pdf.uiId, pdf.isBookmarked) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
    }
}

@Composable
fun ResponsivePdfTile(pdf: LocalPdfEntity, onClick: () -> Unit, onBookmarkClick: () -> Unit) {
    MorphingSurface(
        onClick = onClick,
        initialCornerRadius = 16.dp, // large
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.PictureAsPdf,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pdf.fileName,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (pdf.lastPageIndex > 0) {
                    Text(
                        text = "Page ${pdf.lastPageIndex + 1}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onBookmarkClick) {
                Icon(
                    imageVector = if (pdf.isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "Bookmark",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight, 
                        contentDescription = null, 
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ContinueReadingCard(pdf: LocalPdfEntity, onClick: () -> Unit) {
    MorphingSurface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        initialCornerRadius = 28.dp, // extraLarge
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.PictureAsPdf,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pdf.fileName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Page ${pdf.lastPageIndex + 1}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
            FilledIconButton(
                onClick = onClick,
                modifier = Modifier.size(56.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    contentColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Continue", modifier = Modifier.size(32.dp))
            }
        }
    }
}

@Composable
fun PdfCompactCard(pdf: LocalPdfEntity, onClick: () -> Unit, onBookmarkClick: () -> Unit) {
    MorphingSurface(
        onClick = onClick,
        modifier = Modifier.width(180.dp).height(140.dp),
        initialCornerRadius = 16.dp, // large
        color = MaterialTheme.colorScheme.surfaceVariant,
        morphOnPress = false
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize()
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                IconButton(onClick = onBookmarkClick, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = if (pdf.isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = pdf.fileName,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun PdfListTile(pdf: LocalPdfEntity, onClick: () -> Unit, onBookmarkClick: () -> Unit) {
    MorphingSurface(
        onClick = onClick,
        initialCornerRadius = 16.dp, // large
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        ListItem(
            headlineContent = { 
                Text(text = pdf.fileName, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold) 
            },
            supportingContent = { 
                if (pdf.lastPageIndex > 0) Text("Page ${pdf.lastPageIndex + 1}") 
            },
            leadingContent = {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            },
            trailingContent = {
                IconButton(onClick = onBookmarkClick) {
                    Icon(
                        imageVector = if (pdf.isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
        )
    }
}

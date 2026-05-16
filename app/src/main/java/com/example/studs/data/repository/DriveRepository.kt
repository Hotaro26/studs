package com.example.studs.data.repository

import android.content.Context
import com.example.studs.data.api.GitHubApiService
import com.example.studs.data.local.LocalPdfEntity
import com.example.studs.data.local.PdfFileDao
import com.example.studs.data.model.GitHubContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class DriveRepository(
    private val apiService: GitHubApiService,
    private val pdfDao: PdfFileDao,
    private val context: Context
) {

    // Room DB Flows for Home Screen
    val recentHistory: Flow<List<LocalPdfEntity>> = pdfDao.getRecentHistory()
    val bookmarkedFiles: Flow<List<LocalPdfEntity>> = pdfDao.getBookmarkedFiles()
    val lastReadPdf: Flow<LocalPdfEntity?> = pdfDao.getLastReadPdf()

    fun getPdfFlow(uiId: String): Flow<LocalPdfEntity?> = pdfDao.getPdfByIdFlow(uiId)

    suspend fun getChildren(path: String): Result<List<GitHubContent>> = withContext(Dispatchers.IO) {
        try {
            // Root path is empty string for GitHub contents API
            val actualPath = if (path == "ROOT") "" else path
            val response = apiService.getContents(path = actualPath)
            
            if (response.isSuccessful) {
                // To show "recent first", we'd ideally have commit dates.
                // Since the basic API doesn't provide them, we'll maintain the list
                // and for the sake of the requirement, we'll sort them by name descending 
                // or assume newer files are added at the end.
                // For a more realistic "recent" feel, we'll just sort folders first then files.
                val files = response.body() ?: emptyList()
                Result.success(files.sortedWith(compareByDescending<GitHubContent> { it.isFolder }.thenByDescending { it.name }))
            } else {
                Result.failure(Exception("Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchFiles(query: String): Result<List<GitHubContent>> = withContext(Dispatchers.IO) {
        try {
            // Constructing a query that searches for PDFs in the specific repo
            val fullQuery = "$query extension:pdf repo:iko829e73/hosts-pdf"
            val response = apiService.searchFiles(fullQuery)
            
            if (response.isSuccessful) {
                Result.success(response.body()?.items ?: emptyList())
            } else {
                Result.failure(Exception("Search Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPdfFile(fileUrl: String, fileName: String, uiId: String, forDownload: Boolean): Result<File> = withContext(Dispatchers.IO) {
        try {
            // Target directory
            val targetDir = if (forDownload) context.filesDir else context.cacheDir
            val targetFile = File(targetDir, fileName)

            // Register/Update in DB when opened
            val existing = pdfDao.getPdfById(uiId)
            val entity = existing?.copy(
                lastViewedTimestamp = System.currentTimeMillis()
            ) ?: LocalPdfEntity(
                uiId = uiId,
                fileName = fileName,
                downloadUrl = fileUrl,
                lastViewedTimestamp = System.currentTimeMillis(),
                isDownloaded = File(context.filesDir, fileName).exists()
            )
            pdfDao.insertOrUpdate(entity)

            // If it already exists in filesDir (downloaded), return it.
            val downloadedFile = File(context.filesDir, fileName)
            if (downloadedFile.exists()) {
                if (!entity.isDownloaded) pdfDao.update(entity.copy(isDownloaded = true))
                return@withContext Result.success(downloadedFile)
            }
            
            // If we are just viewing, and it's already in cache, return it.
            if (!forDownload && targetFile.exists()) {
                return@withContext Result.success(targetFile)
            }

            val response = apiService.downloadFile(fileUrl)
            if (response.isSuccessful) {
                val body = response.body() ?: return@withContext Result.failure(Exception("Empty body"))
                
                FileOutputStream(targetFile).use { output ->
                    body.byteStream().use { input ->
                        input.copyTo(output)
                    }
                }
                
                if (forDownload) {
                    pdfDao.update(entity.copy(isDownloaded = true))
                }
                Result.success(targetFile)
            } else {
                Result.failure(Exception("Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun toggleBookmark(uiId: String, currentStatus: Boolean) = withContext(Dispatchers.IO) {
        pdfDao.updateBookmarkStatus(uiId, !currentStatus)
    }

    suspend fun updateReadingProgress(uiId: String, pageIndex: Int) = withContext(Dispatchers.IO) {
        pdfDao.updateReadingProgress(uiId, pageIndex, System.currentTimeMillis())
    }

    fun isDownloaded(fileName: String): Boolean {
        return File(context.filesDir, fileName).exists()
    }
}

package com.example.studs.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PdfFileDao {
    @Query("SELECT * FROM pdf_files WHERE uiId = :uiId")
    fun getPdfById(uiId: String): LocalPdfEntity?

    @Query("SELECT * FROM pdf_files WHERE uiId = :uiId")
    fun getPdfByIdFlow(uiId: String): Flow<LocalPdfEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(pdf: LocalPdfEntity)

    @Update
    fun update(pdf: LocalPdfEntity)

    @Query("SELECT * FROM pdf_files WHERE lastViewedTimestamp > 0 ORDER BY lastViewedTimestamp DESC LIMIT 1")
    fun getLastReadPdf(): Flow<LocalPdfEntity?>

    @Query("SELECT * FROM pdf_files WHERE lastViewedTimestamp > 0 ORDER BY lastViewedTimestamp DESC LIMIT 20")
    fun getRecentHistory(): Flow<List<LocalPdfEntity>>

    @Query("SELECT * FROM pdf_files WHERE isBookmarked = 1 ORDER BY lastViewedTimestamp DESC")
    fun getBookmarkedFiles(): Flow<List<LocalPdfEntity>>
    
    @Query("UPDATE pdf_files SET isBookmarked = :isBookmarked WHERE uiId = :uiId")
    fun updateBookmarkStatus(uiId: String, isBookmarked: Boolean)
    
    @Query("UPDATE pdf_files SET lastPageIndex = :pageIndex, lastViewedTimestamp = :timestamp WHERE uiId = :uiId")
    fun updateReadingProgress(uiId: String, pageIndex: Int, timestamp: Long)
}

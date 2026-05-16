package com.example.studs.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pdf_files")
data class LocalPdfEntity(
    @PrimaryKey
    val uiId: String,
    val fileName: String,
    val downloadUrl: String,
    val lastViewedTimestamp: Long = 0,
    val lastPageIndex: Int = 0,
    val isBookmarked: Boolean = false,
    val isDownloaded: Boolean = false
)

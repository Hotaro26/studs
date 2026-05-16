package com.example.studs.data.model

import com.google.gson.annotations.SerializedName

data class GitHubContent(
    val name: String,
    val path: String,
    val sha: String,
    val size: Long,
    val url: String,
    @SerializedName("html_url") val htmlUrl: String,
    @SerializedName("git_url") val gitUrl: String,
    @SerializedName("download_url") val downloadUrl: String?,
    val type: String,
    // Note: This field is not returned by the contents API directly, 
    // but we can use it for sorting if we fetch commit info or simulate it.
    var lastUpdated: Long = 0
) {
    val isFolder: Boolean
        get() = type == "dir"
        
    val isPdf: Boolean
        get() = type == "file" && name.endsWith(".pdf", ignoreCase = true)
        
    val uiId: String
        get() = path

    val rawUrl: String?
        get() = downloadUrl ?: "https://raw.githubusercontent.com/iko829e73/hosts-pdf/main/$path"
}

data class GitHubSearchResponse(
    @SerializedName("total_count") val totalCount: Int,
    @SerializedName("incomplete_results") val incompleteResults: Boolean,
    val items: List<GitHubContent>
)

package com.example.studs.data.api

import com.example.studs.data.model.GitHubContent
import com.example.studs.data.model.GitHubSearchResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface GitHubApiService {

    @GET("repos/{owner}/{repo}/contents/{path}")
    suspend fun getContents(
        @Path("owner") owner: String = "iko829e73",
        @Path("repo") repo: String = "hosts-pdf",
        @Path("path") path: String = ""
    ): Response<List<GitHubContent>>

    @GET("search/code")
    suspend fun searchFiles(
        @Query("q") query: String,
        @Query("per_page") perPage: Int = 100
    ): Response<GitHubSearchResponse>

    @GET
    suspend fun downloadFile(
        @Url fileUrl: String
    ): Response<ResponseBody>
}

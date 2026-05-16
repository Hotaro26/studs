package com.example.studs.di

import android.content.Context
import com.example.studs.data.local.AppDatabase
import com.example.studs.data.repository.DriveRepository
import com.example.studs.data.repository.SettingsRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppContainer(private val context: Context) {
    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(context)
    }

    private val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }
    
    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService: com.example.studs.data.api.GitHubApiService by lazy {
        retrofit.create(com.example.studs.data.api.GitHubApiService::class.java)
    }
    
    val driveRepository: DriveRepository by lazy {
        DriveRepository(apiService, database.pdfFileDao(), context)
    }
}

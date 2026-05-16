package com.example.studs

import android.app.Application
import com.example.studs.di.AppContainer

class StudsApplication : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

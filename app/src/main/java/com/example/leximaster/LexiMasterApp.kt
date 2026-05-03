package com.example.leximaster

import android.app.Application
import com.example.leximaster.di.aiModule
import com.example.leximaster.di.appModule
import com.example.leximaster.di.dataModule
import com.example.leximaster.di.presentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class LexiMasterApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@LexiMasterApp)
            modules(listOf(appModule, presentationModule, dataModule, aiModule))
        }
    }
}

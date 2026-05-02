package com.example.leximaster.di

import com.example.leximaster.data.remote.config.GenerativeModelFactory
import com.example.leximaster.data.remote.service.GeminiService
import org.koin.dsl.module

val aiModule = module {
    single<GenerativeModelFactory> { GenerativeModelFactory }
    single { GeminiService(get()) }
}

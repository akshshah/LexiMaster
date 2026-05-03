package com.example.leximaster.di

import com.example.leximaster.presentation.word.WordDiscoveryViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    // Application-level dependencies
}

val presentationModule = module {
    viewModelOf(::WordDiscoveryViewModel)
}

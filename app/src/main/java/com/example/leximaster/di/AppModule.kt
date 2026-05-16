package com.example.leximaster.di

import com.example.leximaster.presentation.dashboard.DashboardViewModel
import com.example.leximaster.presentation.library.LibraryViewModel
import com.example.leximaster.presentation.wordDiscovery.WordDiscoveryViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    // Application-level dependencies
}

val presentationModule = module {
    viewModelOf(::WordDiscoveryViewModel)
    viewModelOf(::LibraryViewModel)
    viewModelOf(::DashboardViewModel)
}

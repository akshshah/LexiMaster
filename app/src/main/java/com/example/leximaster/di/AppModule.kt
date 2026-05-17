package com.example.leximaster.di

import com.example.leximaster.presentation.ui.dashboard.DashboardViewModel
import com.example.leximaster.presentation.ui.library.LibraryViewModel
import com.example.leximaster.presentation.ui.mainscreen.MainScreenViewModel
import com.example.leximaster.presentation.ui.userprofile.UserProfileViewModel
import com.example.leximaster.presentation.ui.welcome.WelcomeViewModel
import com.example.leximaster.presentation.ui.wordDetail.WordDetailsViewModel
import com.example.leximaster.presentation.ui.wordDiscovery.WordDiscoveryViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    // Application-level dependencies
}

val presentationModule = module {
    viewModelOf(::WelcomeViewModel)
    viewModelOf(::MainScreenViewModel)
    viewModelOf(::WordDiscoveryViewModel)
    viewModelOf(::LibraryViewModel)
    viewModelOf(::DashboardViewModel)
    viewModelOf(::UserProfileViewModel)
    viewModelOf(::WordDetailsViewModel)
}

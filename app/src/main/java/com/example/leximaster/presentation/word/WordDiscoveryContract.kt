package com.example.leximaster.presentation.word

import android.os.Parcelable
import com.example.leximaster.data.remote.dto.GeminiWordResponse
import kotlinx.parcelize.Parcelize

@Parcelize
data class WordDiscoveryState(
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val isSaving: Boolean = false,
    val previewData: GeminiWordResponse? = null,
    val editedSynonyms: List<String>? = null,
    val errorMessage: String? = null,
) : Parcelable

sealed interface WordDiscoveryAction {
    data class UpdateSearchQuery(val query: String) : WordDiscoveryAction
    data object SubmitSearch : WordDiscoveryAction
    data class RemoveSynonym(val synonym: String) : WordDiscoveryAction
    data object ConfirmAndSave : WordDiscoveryAction
    data object ClearError : WordDiscoveryAction
}

sealed interface WordDiscoveryEvent {
    data class ShowSnackbar(val message: String) : WordDiscoveryEvent
    data object NavigateBack : WordDiscoveryEvent
}

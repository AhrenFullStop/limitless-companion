package com.limitless.companion.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.limitless.companion.data.local.db.TranscriptDatabase
import com.limitless.companion.data.local.db.TranscriptEntity
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class TranscriptsViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = TranscriptDatabase.getInstance(application).transcriptDao()

    val allTranscripts: StateFlow<List<TranscriptEntity>> = dao.getAllAsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResults: StateFlow<List<TranscriptEntity>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) dao.getAllAsFlow()
            else flow { emit(dao.search(query)) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
}

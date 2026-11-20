package com.lauda.lofi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lauda.lofi.data.SaavnRepository
import com.lauda.lofi.data.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SearchState(
    val query: String = "",
    val loading: Boolean = false,
    val results: List<Track> = emptyList(),
    val error: String? = null
)

class SearchViewModel : ViewModel() {
    private val repo = SaavnRepository()
    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

    fun setQuery(value: String) {
        _state.value = _state.value.copy(query = value)
    }

    fun search() {
        val q = _state.value.query.trim()
        if (q.isEmpty()) return
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            repo.search(q).fold(
                onSuccess = { _state.value = _state.value.copy(loading = false, results = it) },
                onFailure = { _state.value = _state.value.copy(loading = false, error = it.message ?: "Search failed") }
            )
        }
    }
}

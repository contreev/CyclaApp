package com.example.cyclapp.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cyclapp.data.agregarOEditarResena
import com.example.cyclapp.data.getPuntosReciclaje
import com.example.cyclapp.model.RecyclingPoint
import com.example.cyclapp.model.Review
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {
    private val _puntos = MutableStateFlow<List<RecyclingPoint>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val puntos: StateFlow<List<RecyclingPoint>> = combine(_puntos, _searchQuery) { lista, query ->
        if (query.isBlank()) {
            lista
        } else {
            lista.filter { 
                it.name.contains(query, ignoreCase = true) || 
                it.address.contains(query, ignoreCase = true) ||
                it.categories.any { cat -> cat.contains(query, ignoreCase = true) }
            }
        }
    }.let { flow ->
        val state = MutableStateFlow<List<RecyclingPoint>>(emptyList())
        viewModelScope.launch {
            flow.collect { state.value = it }
        }
        state
    }

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    init {
        escucharPuntos()
    }

    private fun escucharPuntos() {
        _isLoading.value = true
        getPuntosReciclaje { listaActualizada ->
            _puntos.value = listaActualizada
            _isLoading.value = false
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun enviarResena(puntoId: String, uid: String, resena: Review) {
        agregarOEditarResena(puntoId, uid, resena,
            onSuccess = {
                // No es necesario cargarPuntos() manualmente porque tenemos un listener en tiempo real
            },
            onError = {
                // Manejar error
            }
        )
    }
}

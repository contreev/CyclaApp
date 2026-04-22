package com.example.cyclapp.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cyclapp.data.RetrofitClient
import com.example.cyclapp.model.Article
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NewsViewModel : ViewModel() {
    private val _articles = MutableStateFlow<List<Article>>(emptyList())
    val articles: StateFlow<List<Article>> = _articles

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val API_KEY = "cbbfbdf42ba840de8199c3556b4da339"

    // Lista de temas estrictamente enfocados en reciclaje para variar los resultados
    private val searchQueries = listOf(
        "reciclaje",
        "reciclaje de plástico",
        "reciclaje de vidrio",
        "reciclaje de papel y cartón",
        "consejos para reciclar en casa",
        "reciclaje de residuos electrónicos",
        "importancia del reciclaje",
        "manualidades con reciclaje",
        "puntos de reciclaje",
        "economía circular reciclaje"
    )

    init {
        fetchNews()
    }

    fun fetchNews() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Elegimos un sub-tema de reciclaje al azar
                val randomQuery = searchQueries.random()
                
                val response = RetrofitClient.newsApiService.getRecyclingNews(
                    query = randomQuery,
                    apiKey = API_KEY
                )
                
                if (response.status == "ok") {
                    // Mezclamos los resultados y nos quedamos con los que tengan descripción/imagen si es posible
                    val filteredArticles = response.articles
                        .filter { it.title != "[Removed]" }
                        .shuffled()
                    
                    _articles.value = filteredArticles
                } else {
                    _error.value = "Error: ${response.status}"
                }
            } catch (e: Exception) {
                _error.value = "Error de conexión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
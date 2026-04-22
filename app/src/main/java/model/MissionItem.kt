package com.example.cyclapp.model

data class MissionItem(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val progreso: Int = 0,
    val meta: Int = 1,
    val recompensa: Int = 0,
    val completada: Boolean = false
)
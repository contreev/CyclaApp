package com.example.cyclapp.model

data class UserProfile(
    val uid: String = "",
    val nombre: String = "",
    val apellido: String = "",
    val correo: String = "",
    val fechaNacimiento: String = "",
    val puntos: Int = 0,
    val nivel: String = "Inicial",
    val fotoUrl: String = ""
)
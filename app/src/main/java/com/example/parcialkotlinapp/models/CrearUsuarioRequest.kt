package com.example.parcialkotlinapp.models

data class CrearUsuarioRequest(
    val nombre: String,
    val apellido: String,
    val correo: String,
    val password: String,
    val fechaNacimiento: String,   // formato yyyy-MM-dd
    val universidad: String,
    val semestre: Int
)
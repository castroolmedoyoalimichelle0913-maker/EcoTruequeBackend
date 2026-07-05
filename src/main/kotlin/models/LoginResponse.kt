package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val token: String,
    val nombre: String,
    val correo: String,
    val puntos: Int
)
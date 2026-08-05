package com.example.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdminUsuario(
    val id: Int,
    val nombre: String,
    val correo: String,
    val puntos: Int,
    val rol: String,
    val activo: Boolean,
    @SerialName("fecha_registro")
    val fechaRegistro: String
)

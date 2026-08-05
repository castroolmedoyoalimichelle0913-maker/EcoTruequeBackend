package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class Notificacion(
    val id: Int? = null,

    val usuarioId: Int,

    val tipo: String,

    val titulo: String,

    val mensaje: String,

    val leido: Int = 0,

    val fecha: String
)

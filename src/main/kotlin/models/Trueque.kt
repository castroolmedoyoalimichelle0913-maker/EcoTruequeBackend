package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class Trueque(

    val id: Int? = null,

    val usuarioId: Int,

    val materialId: Int,

    val cantidad: Int,

    val puntosGanados: Int,

    val fecha: String,

    val estado: String
)
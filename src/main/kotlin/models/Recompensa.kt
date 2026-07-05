package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class Recompensa(

    val id: Int? = null,

    val nombre: String,

    val descripcion: String,

    val puntosNecesarios: Int,

    val imagen: String? = null,

    val disponible: Boolean = true
)
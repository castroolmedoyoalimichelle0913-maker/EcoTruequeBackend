package com.example.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Insignia(
    val id: Int? = null,

    val nombre: String,

    val descripcion: String,

    val imagen: String,

    val requerimiento: String,

    val cantidadRequerida: Int,

    @SerialName("obtenida")
    val obtenida: Boolean = false,

    @SerialName("fecha_obtenida")
    val fechaObtenida: String? = null
)

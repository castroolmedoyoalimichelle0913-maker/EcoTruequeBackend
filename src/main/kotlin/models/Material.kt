package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class Material(

    val id: Int? = null,

    val nombre: String,

    val descripcion: String,

    val categoria: String,

    val puntos: Int,

    val imagen: String? = null,

    val latitud: Double? = null,

    val longitud: Double? = null
)
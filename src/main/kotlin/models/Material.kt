package com.example.models

import kotlinx.serialization.SerialName
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

    val longitud: Double? = null,

    @SerialName("usuario_id")
    val usuarioId: Int? = null,

    @SerialName("fecha_publicacion")
    val fechaPublicacion: String? = null,

    @SerialName("usuario_nombre")
    val usuarioNombre: String? = null,

    @SerialName("usuario_foto")
    val usuarioFoto: String? = null,

    @SerialName("usuario_puntos")
    val usuarioPuntos: Int? = null,

    val etiquetas: String? = null,

    val estado: String? = null
)

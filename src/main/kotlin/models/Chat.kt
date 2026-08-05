package com.example.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Chat(
    val id: Int? = null,

    @SerialName("propuesta_id")
    val propuestaId: Int? = null,

    @SerialName("usuario1_id")
    val usuario1Id: Int,

    @SerialName("usuario2_id")
    val usuario2Id: Int,

    @SerialName("fecha_creacion")
    val fechaCreacion: String,

    @SerialName("otro_nombre")
    val otroNombre: String? = null,

    @SerialName("otro_foto")
    val otroFoto: String? = null,

    @SerialName("ultimo_mensaje")
    val ultimoMensaje: String? = null,

    @SerialName("ultima_hora")
    val ultimaHora: String? = null,

    @SerialName("no_leidos")
    val noLeidos: Int = 0,

    @SerialName("material_nombre")
    val materialNombre: String? = null
)

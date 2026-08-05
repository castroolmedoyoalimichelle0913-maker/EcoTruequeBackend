package com.example.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BitacoraRegistro(
    val id: Int? = null,
    @SerialName("usuario_id")
    val usuarioId: Int? = null,
    val correo: String,
    @SerialName("tipo_usuario")
    val tipoUsuario: String,
    val accion: String,
    val ip: String? = null,
    val fecha: String
)

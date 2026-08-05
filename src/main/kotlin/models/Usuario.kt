package com.example.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Usuario(

    val id: Int? = null,

    val nombre: String,

    val correo: String,

    val password: String,

    val telefono: String? = null,

    val puntos: Int = 0,

    val fechaRegistro: String,

    @SerialName("foto_perfil")
    val fotoPerfil: String? = null,

    val rol: String = "usuario",

    val activo: Boolean = true
)

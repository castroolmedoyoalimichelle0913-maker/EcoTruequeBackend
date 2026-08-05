package com.example.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(

    val token: String,

    val id: Int? = null,

    val nombre: String,

    val correo: String,

    val puntos: Int,

    @SerialName("foto_perfil")
    val fotoPerfil: String? = null,

    val rol: String = "usuario",

    val activo: Boolean = true
)

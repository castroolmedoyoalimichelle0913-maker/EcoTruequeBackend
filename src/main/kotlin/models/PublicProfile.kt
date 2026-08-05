package com.example.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PublicProfile(
    val id: Int,
    val nombre: String,
    val correo: String,
    val puntos: Int,
    @SerialName("foto_perfil")
    val fotoPerfil: String? = null,
    @SerialName("fecha_registro")
    val fechaRegistro: String,
    @SerialName("numero_materiales")
    val numeroMateriales: Int,
    @SerialName("numero_trueques")
    val numeroTrueques: Int,
    val rol: String = "usuario",
    val activo: Boolean = true,
    @SerialName("notificaciones")
    val notificaciones: Boolean = true,
    @SerialName("numero_insignias")
    val numeroInsignias: Int = 0
)

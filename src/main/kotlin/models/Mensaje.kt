package com.example.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Mensaje(
    val id: Int? = null,

    @SerialName("chat_id")
    val chatId: Int,

    @SerialName("emisor_id")
    val emisorId: Int,

    val contenido: String,

    val hora: String,

    val leido: Int = 0,

    @SerialName("emisor_nombre")
    val emisorNombre: String? = null
)

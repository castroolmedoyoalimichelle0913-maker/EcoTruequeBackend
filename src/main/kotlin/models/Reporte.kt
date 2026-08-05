package com.example.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Reporte(
    val id: Int? = null,
    @SerialName("usuario_reporta_id")
    val usuarioReportaId: Int? = null,
    val tipo: String,
    @SerialName("referencia_id")
    val referenciaId: Int? = null,
    val motivo: String,
    val estado: String = "pendiente",
    val fecha: String
)

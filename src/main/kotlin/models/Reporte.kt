package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class Reporte(
    val id: Int? = null,
    val usuarioReportaId: Int? = null,
    val tipo: String,
    val referenciaId: Int? = null,
    val motivo: String,
    val estado: String = "pendiente",
    val fecha: String
)

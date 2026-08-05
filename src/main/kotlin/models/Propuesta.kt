package com.example.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Propuesta(
    val id: Int? = null,

    @SerialName("oferente_id")
    val oferenteId: Int,

    @SerialName("receptor_id")
    val receptorId: Int,

    @SerialName("material_oferta_id")
    val materialOfertaId: Int,

    @SerialName("material_deseado_id")
    val materialDeseadoId: Int,

    val mensaje: String? = null,

    val estado: String = "Pendiente",

    val fecha: String,

    @SerialName("oferente_nombre")
    val oferenteNombre: String? = null,

    @SerialName("material_oferta_nombre")
    val materialOfertaNombre: String? = null,

    @SerialName("material_deseado_nombre")
    val materialDeseadoNombre: String? = null
)

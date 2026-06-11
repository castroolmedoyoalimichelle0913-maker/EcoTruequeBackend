package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class Trueque(
    val id: Int,
    val producto: String,
    val usuario: String
)
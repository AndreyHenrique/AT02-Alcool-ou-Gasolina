package com.example.alcoolougasolina.data

import java.util.UUID

data class Posto(
    val id: String = UUID.randomUUID().toString(),
    val nome: String,
    val precoAlcool: Double,
    val precoGasolina: Double,
    val localizacao: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val dataCadastro: Long = System.currentTimeMillis()
)
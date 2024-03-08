package com.bk.bk1.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class LinearAcceleration(
    val Body: Body,
    val Uri: String,
    val Method: String
)

@Serializable
data class Body(
    val Timestamp: Int,
    @SerialName("ArrayAcc") val arrayAcc: List<ArrayAcc>
)

@Serializable
data class ArrayAcc(
    val x: Double,
    val y: Double,
    val z: Double
)
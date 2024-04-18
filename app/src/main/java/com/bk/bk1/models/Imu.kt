package com.bk.bk1.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
class Imu (
    val Body: ImuBody,
    val Uri: String,
    val Method: String
)

@Serializable
data class ImuBody(
    val Timestamp: Int,
    @SerialName("ArrayAcc") val arrayAcc: List<AccItem>,
    @SerialName("ArrayGyro") val arrayGyro: List<GyroItem>,
    @SerialName("ArrayMagn") val arrayMagn: List<MagnItem>

)

@Serializable
data class GyroItem(
    val x: Double,
    val y: Double,
    val z: Double
)

@Serializable
data class MagnItem(
    val x: Double,
    val y: Double,
    val z: Double
)


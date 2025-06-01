package fr.cpe.hello.model

import kotlinx.serialization.Serializable

@Serializable
data class SensorValues(
    var reading_id: Int,
    var sensor_crouscam_id: Int,
    var timestamp: String,
    var temperature: Float,
    var humidity: Float,
    var uv : Float,
    var luminosity : Float,
    var pressure : Float
)
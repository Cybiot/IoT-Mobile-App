package fr.cpe.hello.model

import kotlinx.serialization.Serializable

@Serializable
data class Sensor(
    var id: Int,
    var crouscam_id: Int,
    var sensor_configuration: String,
    var sensor_name: String,
    var last_seen: Double
) {
}

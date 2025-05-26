package fr.cpe.hello.model

import kotlinx.serialization.Serializable

@Serializable
data class Sensor(val id: Int,
                val crouscam_id: Int,
                val sensor_configuration: String,
                val sensor_name: String,
                val last_seen: Double
)

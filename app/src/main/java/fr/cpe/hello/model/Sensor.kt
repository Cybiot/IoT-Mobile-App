package fr.cpe.hello.model

import kotlinx.serialization.Serializable

@Serializable
data class Sensor(
    var db_id: Int,
    var crouscam_id: String,
    var configuration: String,
    var name: String?,
    var location: String?,
    var status: String,
    var last_seen: String
) {
}

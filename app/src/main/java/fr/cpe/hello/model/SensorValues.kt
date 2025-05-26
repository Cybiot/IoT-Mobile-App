package fr.cpe.hello.model

import kotlinx.serialization.Serializable

@Serializable
data class SensorValues(val reading_id: Int,
                        val sensor_crouscam_id: Int,
                        val timestamp: String,
                        val temperature: Double,
                        val humidity: Double,
                        val uv : Double,
                        val luminosity : Double,
                        val pressure : Double
)

//class SensorValues(json: String) : JSONObject(json) {
//    val reading_id = this.optInt("reading_id")
//    val sensor_crouscam_id = this.optString("sensor_crouscam_id")
//    val timestamp = this.optString("timestamp")
//    val temperature = this.optDouble("temperature")
//    val humidity = this.optDouble("humidity")
//    val uv = this.optDouble("uv")
//    val luminosity = this.optDouble("luminosity")
//    val pressure = this.optDouble("pressure")
//}
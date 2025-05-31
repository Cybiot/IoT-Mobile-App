package fr.cpe.hello.model

data class LevelState(
    var unitName: String = "Température",
    var unit: String = "°C",
    var value: Float = 22.3f,
    var maxValue: Float = 40f,
    var arcValue: Float = 0f,
    var inProgress: Boolean = false
)
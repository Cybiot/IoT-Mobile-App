package fr.cpe.hello.model

data class LevelState(
    val value: String = "",
    val maxValue: String = "-",
    val arcValue: Float = 0f,
    val inProgress: Boolean = false
)
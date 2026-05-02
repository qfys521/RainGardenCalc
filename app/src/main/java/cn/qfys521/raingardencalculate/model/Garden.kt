package cn.qfys521.raingardencalculate.model

data class Garden(
    val level: Int = 1,
    val pots: List<PotState> = listOf(PotState())
)

data class PotState(
    val crop: Crop? = null,
    val wateringCount: Int = 0,
    val plantedAt: Long = 0L // epoch millis
)

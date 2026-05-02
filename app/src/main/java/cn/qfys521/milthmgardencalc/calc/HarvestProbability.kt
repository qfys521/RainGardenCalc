package cn.qfys521.milthmgardencalc.calc

data class HarvestOutcome(
    val name: String,
    val yieldMultiplier: Double,
    val probability: Double
)

object HarvestProbability {
    private val table = mapOf(
        0 to listOf(
            HarvestOutcome("普通", 1.0, 0.940),
            HarvestOutcome("丰收", 2.0, 0.050),
            HarvestOutcome("大丰收", 4.0, 0.010)
        ),
        1 to listOf(
            HarvestOutcome("普通", 1.0, 0.925),
            HarvestOutcome("丰收", 2.0, 0.060),
            HarvestOutcome("大丰收", 4.0, 0.015)
        ),
        2 to listOf(
            HarvestOutcome("普通", 1.0, 0.910),
            HarvestOutcome("丰收", 2.0, 0.070),
            HarvestOutcome("大丰收", 4.0, 0.020)
        ),
        3 to listOf(
            HarvestOutcome("普通", 1.0, 0.895),
            HarvestOutcome("丰收", 2.0, 0.080),
            HarvestOutcome("大丰收", 4.0, 0.025)
        )
    )

    fun outcomes(wateringCount: Int): List<HarvestOutcome> {
        return table[wateringCount.coerceIn(0, 3)] ?: table[0]!!
    }

    fun expectedYieldMultiplier(wateringCount: Int): Double {
        return outcomes(wateringCount).sumOf { it.yieldMultiplier * it.probability }
    }
}

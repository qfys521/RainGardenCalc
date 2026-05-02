package cn.qfys521.milthmgardencalc.calc

object WateringSimulator {
    const val TIME_REDUCTION_PER_WATER = 0.20
    const val COOLDOWN_RATIO = 0.25
    const val MAX_WATERING_COUNT = 3

    private val TIME_MULTIPLIERS = doubleArrayOf(1.0, 0.80, 0.60, 0.50)

    fun maxWateringCount(growthTimeHours: Long): Int {
        if (growthTimeHours <= 0) return 0
        return MAX_WATERING_COUNT
    }

    fun effectiveGrowthTime(growthTimeHours: Long, wateringCount: Int): Double {
        if (growthTimeHours <= 0) return growthTimeHours.toDouble()
        val clampedCount = wateringCount.coerceIn(0, maxWateringCount(growthTimeHours))
        return growthTimeHours * getTimeMultiplier(clampedCount)
    }

    fun getTimeMultiplier(wateringCount: Int): Double {
        return TIME_MULTIPLIERS[wateringCount.coerceIn(0, MAX_WATERING_COUNT)]
    }

    fun wateringCooldown(growthTimeHours: Long): Double {
        return growthTimeHours * COOLDOWN_RATIO
    }
}

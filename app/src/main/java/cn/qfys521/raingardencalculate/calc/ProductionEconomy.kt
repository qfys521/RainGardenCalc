package cn.qfys521.raingardencalculate.calc

import cn.qfys521.raingardencalculate.model.Crop
import cn.qfys521.raingardencalculate.model.CropType
import kotlin.math.abs
import kotlin.math.min

object ProductionEconomy {
    private const val MAX_ITERATIONS = 64
    private const val EPSILON = 1e-9
    private val SPECIAL_CROPS = setOf("毛头鬼伞", "毛茛")

    fun materialUnitTimeCosts(
        currentLevel: Int? = null,
        includeSpecial: Boolean = false
    ): Map<CropType, Double> {
        val crops = Crop.CROPS.filter {
            it.growthTimeHours > 0 &&
                (currentLevel == null || it.unlockLevel == null || it.unlockLevel <= currentLevel) &&
                (includeSpecial || it.name !in SPECIAL_CROPS)
        }

        val costs = CropType.entries.associateWith { Double.POSITIVE_INFINITY }.toMutableMap()

        crops.asSequence()
            .filter { it.materialCosts.isEmpty() }
            .forEach { crop ->
                val unit = crop.growthTimeHours.toDouble() / crop.baseYield
                crop.types.forEach { type ->
                    costs[type] = min(costs[type] ?: Double.POSITIVE_INFINITY, unit)
                }
            }

        repeat(MAX_ITERATIONS) {
            var changed = false
            for (crop in crops) {
                val inputCost = crop.materialCosts.sumOf { material ->
                    val unit = costs[material.type] ?: Double.POSITIVE_INFINITY
                    if (!unit.isFinite()) return@sumOf Double.POSITIVE_INFINITY
                    material.amount * unit
                }
                if (!inputCost.isFinite()) continue

                val unit = (crop.growthTimeHours + inputCost) / crop.baseYield
                for (type in crop.types) {
                    val prev = costs[type] ?: Double.POSITIVE_INFINITY
                    if (unit + EPSILON < prev) {
                        costs[type] = unit
                        changed = true
                    }
                }
            }
            if (!changed) return@repeat
        }

        return costs
    }

    fun netEfficiency(crop: Crop, unitTimeCost: Map<CropType, Double>): Double {
        if (crop.growthTimeHours <= 0) return 0.0
        val materialCostTime = crop.materialCosts.sumOf { cost ->
            val unit = unitTimeCost[cost.type] ?: Double.POSITIVE_INFINITY
            if (!unit.isFinite()) return@sumOf Double.POSITIVE_INFINITY
            cost.amount * unit
        }
        val totalTime = crop.growthTimeHours + materialCostTime
        return if (totalTime.isFinite() && totalTime > 0) crop.baseYield.toDouble() / totalTime else 0.0
    }

    fun expectedGhostMushroomYieldPerHour(probability: Double = 26.0 / 194.0): Double {
        val base = Crop.CROPS.firstOrNull { it.name == "毛头鬼伞" }?.baseYield ?: 5
        val multiplier = HarvestProbability.expectedYieldMultiplier(0)
        return probability * base * multiplier
    }
}

package cn.qfys521.milthmgardencalc.calc

import cn.qfys521.milthmgardencalc.model.Crop

object GardenCalculator {

    fun netIncomePerHour(crop: Crop, wateringCount: Int): Double {
        if (crop.growthTimeHours <= 0) return crop.baseYield.toDouble()
        val effectiveTime = WateringSimulator.effectiveGrowthTime(crop.growthTimeHours, wateringCount)
        return crop.baseYield.toDouble() / effectiveTime
    }

    fun averageNetIncomePerHour(crop: Crop, wateringCount: Int): Double {
        val base = netIncomePerHour(crop, wateringCount)
        val multiplier = HarvestProbability.expectedYieldMultiplier(wateringCount)
        return base * multiplier
    }

    fun materialCostTime(crop: Crop): Double {
        if (crop.materialCosts.isEmpty()) return 0.0
        val unitCost = ProductionEconomy.materialUnitTimeCosts()
        return crop.materialCosts.sumOf { cost ->
            val unit = unitCost[cost.type] ?: return@sumOf 0.0
            if (!unit.isFinite()) return@sumOf 0.0
            cost.amount * unit
        }
    }

    fun netIncomeWithMaterialCost(crop: Crop, wateringCount: Int): Double {
        if (crop.growthTimeHours <= 0) return crop.baseYield.toDouble()
        val effectiveTime = WateringSimulator.effectiveGrowthTime(crop.growthTimeHours, wateringCount)
        val costTime = materialCostTime(crop)
        return crop.baseYield.toDouble() / (effectiveTime + costTime)
    }

    fun effectiveGrowthTimeDisplay(growthTimeHours: Long, wateringCount: Int): String {
        if (growthTimeHours <= 0) return "即时"
        val effective = WateringSimulator.effectiveGrowthTime(growthTimeHours, wateringCount)
        val hours = effective.toLong()
        val days = hours / 24
        val remainingHours = hours % 24
        return when {
            days > 0 && remainingHours > 0 -> "${days}d${remainingHours}h"
            days > 0 -> "${days}d"
            else -> "${hours}h"
        }
    }
}

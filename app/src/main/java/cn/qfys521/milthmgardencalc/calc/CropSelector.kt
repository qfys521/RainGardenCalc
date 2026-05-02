package cn.qfys521.milthmgardencalc.calc

import cn.qfys521.milthmgardencalc.model.Crop
import cn.qfys521.milthmgardencalc.model.CropType
import kotlin.math.ceil

data class CropPlan(
    val crop: Crop,
    val totalBatches: Int,
    var batchesPlanted: Int = 0,
    val wateringCount: Int,
    val effectiveGrowthTime: Double,
    val harvestLoginDelta: Int,
    val expectedYieldPerBatch: Int,
    val materialCosts: Map<CropType, Int>
)

object CropSelector {

    private val SPECIAL_CROPS = setOf("毛头鬼伞", "毛茛")

    /**
     * Compute the time cost (hours) to produce 1 unit of each material type
     * using the cheapest base crop (no material cost) at the given level.
     */
    fun computeMaterialTimeCost(currentLevel: Int): Map<CropType, Double> {
        return ProductionEconomy.materialUnitTimeCosts(
            currentLevel = currentLevel,
            includeSpecial = false
        )
    }

    /**
     * Compute net efficiency for a crop (wiki 净收益):
     * baseYield / (growthTime + sum(materialAmount * materialTimeCost))
     */
    fun netEfficiency(crop: Crop, materialTimeCost: Map<CropType, Double>): Double {
        val materialCostTime = crop.materialCosts.sumOf { cost ->
            cost.amount * (materialTimeCost[cost.type] ?: 0.0)
        }
        val totalTime = crop.growthTimeHours + materialCostTime
        if (totalTime <= 0) return 0.0
        return crop.baseYield.toDouble() / totalTime
    }

    /**
     * Select the best crop for a given CropType at the current level.
     * Uses net efficiency (including material cost time).
     */
    fun bestCropForType(
        type: CropType,
        currentLevel: Int,
        loginIntervalHours: Double
    ): Crop? {
        val mtc = computeMaterialTimeCost(currentLevel)
        return Crop.CROPS
            .filter {
                it.types.contains(type) &&
                it.growthTimeHours > 0 &&
                it.name !in SPECIAL_CROPS &&
                (it.unlockLevel == null || it.unlockLevel <= currentLevel)
            }
            .maxByOrNull { netEfficiency(it, mtc) }
    }

    /**
     * Create a CropPlan for a given crop and amount.
     */
    fun createCropPlan(
        crop: Crop,
        amount: Int,
        loginIntervalHours: Double
    ): CropPlan {
        val wateringCount = computeWateringCount(crop.growthTimeHours, loginIntervalHours)
        val effTime = WateringSimulator.effectiveGrowthTime(crop.growthTimeHours, wateringCount)
        val harvestDelta = ceil(effTime / loginIntervalHours).toInt().coerceAtLeast(1)
        val expectedYield = (crop.baseYield * HarvestProbability.expectedYieldMultiplier(wateringCount)).toInt()
            .coerceAtLeast(1)
        val batches = ceil(amount.toDouble() / expectedYield).toInt().coerceAtLeast(1)

        return CropPlan(
            crop = crop,
            totalBatches = batches,
            wateringCount = wateringCount,
            effectiveGrowthTime = effTime,
            harvestLoginDelta = harvestDelta,
            expectedYieldPerBatch = expectedYield,
            materialCosts = crop.materialCosts.associate { it.type to it.amount }
        )
    }

    /**
     * Compute watering count based on login interval and crop growth time.
     * First watering is deferred to plantTime + 2*cooldown (not same login as planting).
     */
    fun computeWateringCount(growthTimeHours: Long, loginIntervalHours: Double): Int {
        if (growthTimeHours <= 0) return 0
        val cooldown = growthTimeHours * WateringSimulator.COOLDOWN_RATIO
        var count = 0
        var effTime = growthTimeHours.toDouble()

        while (count < WateringSimulator.MAX_WATERING_COUNT) {
            val nextWaterTime = cooldown * (count + 2)
            if (nextWaterTime >= effTime) break
            count++
            effTime = growthTimeHours * WateringSimulator.getTimeMultiplier(count)
        }

        val maxByInterval = (effTime / loginIntervalHours).toInt()
        return count.coerceAtMost(maxByInterval)
    }
}

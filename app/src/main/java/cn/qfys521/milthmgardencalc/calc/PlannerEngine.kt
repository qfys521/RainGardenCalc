package cn.qfys521.milthmgardencalc.calc

import cn.qfys521.milthmgardencalc.model.Crop
import cn.qfys521.milthmgardencalc.model.CropType
import cn.qfys521.milthmgardencalc.model.FlowerPot
import cn.qfys521.milthmgardencalc.model.MaterialCost
import cn.qfys521.milthmgardencalc.model.Plan
import cn.qfys521.milthmgardencalc.model.PlantingAction

object PlannerEngine {

    /**
     * Backward-compatible overload: uses fixed login interval.
     */
    fun computePlan(
        materialNeeds: Map<CropType, Int>,
        existingInventory: Map<CropType, Int>,
        potCount: Int,
        loginIntervalHours: Double,
        currentLevel: Int,
        targetLevel: Int = currentLevel,
        isLevelMode: Boolean = false
    ): Plan {
        val loginTimes = PotSimulator.generateFixedLoginTimes(loginIntervalHours)
        return computePlan(materialNeeds, existingInventory, potCount, loginTimes, currentLevel, targetLevel, isLevelMode)
    }

    /**
     * Main overload: uses custom login timestamps.
     */
    fun computePlan(
        materialNeeds: Map<CropType, Int>,
        existingInventory: Map<CropType, Int>,
        potCount: Int,
        loginTimes: List<Double>,
        currentLevel: Int,
        targetLevel: Int = currentLevel,
        isLevelMode: Boolean = false
    ): Plan {
        val loginIntervalHours = displayLoginInterval(loginTimes)

        val netNeeds = materialNeeds.mapValues { (type, amount) ->
            (amount - existingInventory.getOrDefault(type, 0)).coerceAtLeast(0)
        }.filter { it.value > 0 }

        if (netNeeds.isEmpty()) {
            return Plan(
                steps = emptyList(),
                loginSchedule = emptyList(),
                totalTimeHours = 0.0,
                totalLoginCount = 0,
                potCount = potCount,
                loginIntervalHours = loginIntervalHours,
                materialNeeds = materialNeeds,
                existingInventory = existingInventory,
                netNeeds = emptyMap(),
                productionSummary = emptyMap(),
                loginTimes = loginTimes,
                isAlreadySatisfied = true
            )
        }

        val simResult = PotSimulator.simulate(
            productionTargets = netNeeds,
            potCount = potCount,
            loginTimes = loginTimes,
            existingInventory = existingInventory,
            startLevel = currentLevel,
            targetLevel = targetLevel,
            isLevelMode = isLevelMode
        )

        // Extract actual crop usage from simulation login schedule
        val cropStats = mutableMapOf<String, Triple<cn.qfys521.milthmgardencalc.model.Crop, Int, Int>>() // name -> (crop, planted, harvested)
        for (event in simResult.loginSchedule) {
            for (action in event.potActions) {
                val crop = action.crop ?: continue
                when (action.action) {
                    cn.qfys521.milthmgardencalc.model.PotAction.PLANT,
                    cn.qfys521.milthmgardencalc.model.PotAction.PLANT_AND_HARVEST -> {
                        val existing = cropStats[crop.name]
                        cropStats[crop.name] = Triple(
                            crop,
                            (existing?.second ?: 0) + action.batchCount,
                            (existing?.third ?: 0)
                        )
                    }
                    cn.qfys521.milthmgardencalc.model.PotAction.HARVEST -> {
                        val existing = cropStats[crop.name]
                        cropStats[crop.name] = Triple(
                            crop,
                            existing?.second ?: 0,
                            (existing?.third ?: 0) + action.batchCount
                        )
                    }
                    else -> {}
                }
            }
        }

        val steps = cropStats.values.mapNotNull { (crop, planted, harvested) ->
            if (crop.name in setOf("毛头鬼伞", "毛茛")) return@mapNotNull null
            if (harvested <= 0) return@mapNotNull null
            PlantingAction(
                crop = crop,
                batchCount = planted,
                wateringCount = 0,
                cycleTimeHours = crop.growthTimeHours.toDouble(),
                totalYield = harvested,
                loginIndex = 0
            )
        }

        // Compute per-level crop progression
        data class CropLevelKey(val level: Int, val cropName: String)
        val levelCropMap = mutableMapOf<CropLevelKey, Triple<cn.qfys521.milthmgardencalc.model.Crop, Int, Int>>()
        for (event in simResult.loginSchedule) {
            for (action in event.potActions) {
                val crop = action.crop ?: continue
                val key = CropLevelKey(event.level, crop.name)
                when (action.action) {
                    cn.qfys521.milthmgardencalc.model.PotAction.PLANT,
                    cn.qfys521.milthmgardencalc.model.PotAction.PLANT_AND_HARVEST -> {
                        val existing = levelCropMap[key]
                        levelCropMap[key] = Triple(crop, (existing?.second ?: 0) + action.batchCount, existing?.third ?: 0)
                    }
                    cn.qfys521.milthmgardencalc.model.PotAction.HARVEST -> {
                        val existing = levelCropMap[key]
                        levelCropMap[key] = Triple(crop, existing?.second ?: 0, (existing?.third ?: 0) + action.batchCount)
                    }
                    else -> {}
                }
            }
        }
        val cropProgression = levelCropMap.entries
            .filter { it.value.third > 0 && it.key.cropName !in setOf("毛头鬼伞", "毛茛") }
            .map { (key, triple) ->
                cn.qfys521.milthmgardencalc.model.LevelCropUsage(
                    level = key.level,
                    cropName = key.cropName,
                    cropTypes = triple.first.types,
                    planted = triple.second,
                    harvested = triple.third,
                    growthTimeHours = triple.first.growthTimeHours
                )
            }
            .sortedWith(compareBy<cn.qfys521.milthmgardencalc.model.LevelCropUsage> { it.level }.thenBy { it.cropName })

        return Plan(
            steps = steps,
            loginSchedule = simResult.loginSchedule,
            totalTimeHours = simResult.totalTimeHours,
            totalLoginCount = simResult.totalLoginCount,
            potCount = potCount,
            loginIntervalHours = loginIntervalHours,
            materialNeeds = materialNeeds,
            existingInventory = existingInventory,
            netNeeds = netNeeds,
            productionSummary = simResult.productionSummary,
            levelUpSpending = simResult.levelUpSpending,
            potUnlockSpending = simResult.potUnlockSpending,
            loginTimes = loginTimes,
            cropProgression = cropProgression
        )
    }

    fun potUnlockCosts(currentLevel: Int, targetLevel: Int): List<MaterialCost> {
        return FlowerPot.POTS
            .filter { it.unlockLevel in (currentLevel + 1)..targetLevel }
            .flatMap { it.materialCosts }
    }

    /**
     * Plan each level independently and merge results.
     * For each level, calculates the materials needed for that level's upgrade + pot unlock,
     * runs a simulation to produce those materials, then carries inventory forward.
     */
    fun computePlanPerLevel(
        currentLevel: Int,
        targetLevel: Int,
        existingInventory: Map<CropType, Int>,
        loginTimes: List<Double>
    ): Plan {
        val loginIntervalHours = displayLoginInterval(loginTimes)
        val inventory = existingInventory.toMutableMap().withDefault { 0 }
        val allSchedules = mutableListOf<cn.qfys521.milthmgardencalc.model.LoginScheduleEvent>()
        val allProduction = mutableMapOf<CropType, Int>().withDefault { 0 }
        val allLevelUpSpending = mutableMapOf<CropType, Int>().withDefault { 0 }
        val allPotUnlockSpending = mutableMapOf<CropType, Int>().withDefault { 0 }
        val totalMaterialNeeds = mutableMapOf<CropType, Int>().withDefault { 0 }
        var timeOffset = 0.0
        var totalLoginCount = 0

        for (level in currentLevel until targetLevel) {
            val nextLevel = level + 1

            // Calculate this level's upgrade costs
            val upgrade = cn.qfys521.milthmgardencalc.model.LevelUpgrade.UPGRADES.find { it.level == nextLevel }
            val levelCosts = upgrade?.materialCosts?.associate { it.type to it.amount } ?: emptyMap()

            // Calculate pot unlock costs at this level
            val pot = FlowerPot.POTS.find { it.unlockLevel == nextLevel }
            val potCosts = pot?.materialCosts?.associate { it.type to it.amount } ?: emptyMap()

            // Total needs for this level
            val levelNeeds = (levelCosts.keys + potCosts.keys).associate { type ->
                type to ((levelCosts[type] ?: 0) + (potCosts[type] ?: 0))
            }.filter { it.value > 0 }

            // Accumulate total material needs
            levelNeeds.forEach { (type, amount) ->
                totalMaterialNeeds[type] = totalMaterialNeeds.getValue(type) + amount
            }

            // Net needs after deducting current inventory
            val netNeeds = levelNeeds.mapValues { (type, amount) ->
                (amount - inventory.getValue(type)).coerceAtLeast(0)
            }.filter { it.value > 0 }

            // Available pots at this level
            val potCount = FlowerPot.POTS.count { it.unlockLevel <= level }.coerceAtLeast(1)

            if (netNeeds.isNotEmpty()) {
                // Filter login times to those after the current time offset
                val availableLoginTimes = loginTimes.filter { it >= timeOffset - 0.001 }

                val simResult = PotSimulator.simulate(
                    productionTargets = netNeeds,
                    potCount = potCount,
                    loginTimes = availableLoginTimes,
                    existingInventory = inventory.toMap(),
                    startLevel = level,
                    targetLevel = level, // Stay at current level for this simulation
                    isLevelMode = false
                )

                // Merge schedule events (adjust login indices)
                for (event in simResult.loginSchedule) {
                    allSchedules.add(event.copy(loginIndex = totalLoginCount + event.loginIndex))
                }

                // Update time offset
                if (simResult.loginSchedule.isNotEmpty()) {
                    val lastEvent = simResult.loginSchedule.last()
                    timeOffset = lastEvent.timeHours
                    totalLoginCount += simResult.totalLoginCount
                }

                // Merge production
                simResult.productionSummary.forEach { (type, amount) ->
                    allProduction[type] = allProduction.getValue(type) + amount
                }

                // Add produced materials to inventory
                simResult.productionSummary.forEach { (type, amount) ->
                    inventory[type] = inventory.getValue(type) + amount
                }
            }

            // Deduct level upgrade costs from inventory
            levelCosts.forEach { (type, amount) ->
                inventory[type] = (inventory.getValue(type) - amount).coerceAtLeast(0)
                allLevelUpSpending[type] = allLevelUpSpending.getValue(type) + amount
            }

            // Deduct pot unlock costs from inventory
            potCosts.forEach { (type, amount) ->
                inventory[type] = (inventory.getValue(type) - amount).coerceAtLeast(0)
                allPotUnlockSpending[type] = allPotUnlockSpending.getValue(type) + amount
            }
        }

        // Build crop progression from all schedules
        data class CropLevelKey(val level: Int, val cropName: String)
        val levelCropMap = mutableMapOf<CropLevelKey, Triple<cn.qfys521.milthmgardencalc.model.Crop, Int, Int>>()
        for (event in allSchedules) {
            for (action in event.potActions) {
                val crop = action.crop ?: continue
                val key = CropLevelKey(event.level, crop.name)
                when (action.action) {
                    cn.qfys521.milthmgardencalc.model.PotAction.PLANT,
                    cn.qfys521.milthmgardencalc.model.PotAction.PLANT_AND_HARVEST -> {
                        val existing = levelCropMap[key]
                        levelCropMap[key] = Triple(crop, (existing?.second ?: 0) + action.batchCount, existing?.third ?: 0)
                    }
                    cn.qfys521.milthmgardencalc.model.PotAction.HARVEST -> {
                        val existing = levelCropMap[key]
                        levelCropMap[key] = Triple(crop, existing?.second ?: 0, (existing?.third ?: 0) + action.batchCount)
                    }
                    else -> {}
                }
            }
        }
        val cropProgression = levelCropMap.entries
            .filter { it.value.third > 0 && it.key.cropName !in setOf("毛头鬼伞", "毛茛") }
            .map { (key, triple) ->
                cn.qfys521.milthmgardencalc.model.LevelCropUsage(
                    level = key.level,
                    cropName = key.cropName,
                    cropTypes = triple.first.types,
                    planted = triple.second,
                    harvested = triple.third,
                    growthTimeHours = triple.first.growthTimeHours
                )
            }
            .sortedWith(compareBy<cn.qfys521.milthmgardencalc.model.LevelCropUsage> { it.level }.thenBy { it.cropName })

        // Build steps from crop progression
        val steps = cropProgression.groupBy { it.cropName }.mapNotNull { (_, usages) ->
            val first = usages.first()
            val crop = Crop.CROPS.find { it.name == first.cropName } ?: return@mapNotNull null
            PlantingAction(
                crop = crop,
                batchCount = usages.sumOf { it.planted },
                wateringCount = 0,
                cycleTimeHours = crop.growthTimeHours.toDouble(),
                totalYield = usages.sumOf { it.harvested },
                loginIndex = 0
            )
        }

        val totalTime = if (allSchedules.isEmpty()) 0.0 else allSchedules.last().timeHours

        return Plan(
            steps = steps,
            loginSchedule = allSchedules,
            totalTimeHours = totalTime,
            totalLoginCount = totalLoginCount,
            potCount = FlowerPot.POTS.count { it.unlockLevel <= targetLevel }.coerceAtLeast(1),
            loginIntervalHours = loginIntervalHours,
            materialNeeds = totalMaterialNeeds,
            existingInventory = existingInventory,
            netNeeds = totalMaterialNeeds.mapValues { (type, amount) ->
                (amount - existingInventory.getOrDefault(type, 0)).coerceAtLeast(0)
            }.filter { it.value > 0 },
            productionSummary = allProduction,
            levelUpSpending = allLevelUpSpending,
            potUnlockSpending = allPotUnlockSpending,
            loginTimes = loginTimes,
            cropProgression = cropProgression
        )
    }

    private fun displayLoginInterval(loginTimes: List<Double>): Double {
        if (loginTimes.size < 2) return 8.0
        val delta = loginTimes[1] - loginTimes[0]
        return if (delta <= 0.0) 0.0 else delta
    }
}

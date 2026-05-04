package cn.qfys521.raingardencalculate.calc

import cn.qfys521.raingardencalculate.model.Crop
import cn.qfys521.raingardencalculate.model.CropType
import cn.qfys521.raingardencalculate.model.FlowerPot
import cn.qfys521.raingardencalculate.model.LoginScheduleEvent
import cn.qfys521.raingardencalculate.model.MaterialCost
import cn.qfys521.raingardencalculate.model.PotAction
import cn.qfys521.raingardencalculate.model.PotSchedule
import kotlin.math.ceil

data class PotSlot(
    val crop: Crop,
    val batchCount: Int,
    val plantTime: Double,
    val baseGrowthTime: Double,
    val cooldown: Double,
    var wateringDone: Int = 0,
    var lastWaterTime: Double = 0.0,
    var readyTime: Double
)

data class SimulationResult(
    val loginSchedule: List<LoginScheduleEvent>,
    val totalTimeHours: Double,
    val totalLoginCount: Int,
    val productionSummary: Map<CropType, Int>,
    val levelUpSpending: Map<CropType, Int> = emptyMap(),
    val potUnlockSpending: Map<CropType, Int> = emptyMap(),
    val loginTimes: List<Double> = emptyList()
)

data class PotUnlockEntry(
    val level: Int,
    val potCount: Int,
    val materialCosts: List<MaterialCost>
)

object PotSimulator {

    const val MAX_LOGINS = 2000
    private const val MAX_SIM_TIME = 10000.0

    fun simulate(
        productionTargets: Map<CropType, Int>,
        potCount: Int,
        loginIntervalHours: Double,
        existingInventory: Map<CropType, Int>,
        startLevel: Int = 1,
        targetLevel: Int = startLevel,
        isLevelMode: Boolean = false
    ): SimulationResult {
        val loginTimes = generateFixedLoginTimes(loginIntervalHours)
        return simulate(productionTargets, potCount, loginTimes, existingInventory, startLevel, targetLevel, isLevelMode)
    }

    fun simulate(
        productionTargets: Map<CropType, Int>,
        potCount: Int,
        loginTimes: List<Double>,
        existingInventory: Map<CropType, Int>,
        startLevel: Int = 1,
        targetLevel: Int = startLevel,
        isLevelMode: Boolean = false
    ): SimulationResult {
        val eventDriven = if (loginTimes.size >= 2 && loginTimes[1] < loginTimes[0]) {
            Pair(loginTimes[0], loginTimes[0] - loginTimes[1])
        } else {
            null
        }
        if (eventDriven != null) {
            return simulateEventDriven(
                productionTargets = productionTargets,
                potCount = potCount,
                startTime = eventDriven.first,
                maxHours = eventDriven.second,
                existingInventory = existingInventory,
                startLevel = startLevel,
                targetLevel = targetLevel,
                isLevelMode = isLevelMode
            )
        }

        val levelUpCosts = cn.qfys521.raingardencalculate.model.LevelUpgrade.UPGRADES
        val inventory = existingInventory.toMutableMap().withDefault { 0 }
        val produced = mutableMapOf<CropType, Int>().withDefault { 0 }
        val levelUpSpending = mutableMapOf<CropType, Int>().withDefault { 0 }
        val potUnlockSpending = mutableMapOf<CropType, Int>().withDefault { 0 }
        val schedule = mutableListOf<LoginScheduleEvent>()

        val potUnlockByLevel = if (isLevelMode) {
            FlowerPot.POTS.mapIndexedNotNull { index, pot ->
                if (pot.unlockLevel > startLevel && pot.unlockLevel <= targetLevel) {
                    pot.unlockLevel to PotUnlockEntry(pot.unlockLevel, index + 1, pot.materialCosts)
                } else null
            }.toMap()
        } else emptyMap()

        var currentLevel = startLevel
        var currentPotCount = potCount
        val pots = MutableList<PotSlot?>(currentPotCount) { null }

        fun expandPots(newCount: Int) {
            while (pots.size < newCount) pots.add(null)
            currentPotCount = newCount
        }

        data class LevelUpAction(
            val level: Int,
            val levelCosts: Map<CropType, Int>,
            val potIndex: Int = -1,
            val potCosts: Map<CropType, Int> = emptyMap()
        )

        fun computeMaterialTimeCost(): Map<CropType, Double> {
            return ProductionEconomy.materialUnitTimeCosts(
                currentLevel = currentLevel,
                includeSpecial = false
            )
        }

        var materialTimeCost = computeMaterialTimeCost()

        fun tryLevelUp(): List<LevelUpAction> {
            val actions = mutableListOf<LevelUpAction>()
            while (currentLevel < targetLevel) {
                val nextLevel = currentLevel + 1
                val upgrade = levelUpCosts.find { it.level == nextLevel }
                val levelCostMap = mutableMapOf<CropType, Int>()
                if (upgrade != null) {
                    val canAfford = upgrade.materialCosts.all { inventory.getValue(it.type) >= it.amount }
                    if (!canAfford) break
                    for (cost in upgrade.materialCosts) {
                        inventory[cost.type] = inventory.getValue(cost.type) - cost.amount
                        levelUpSpending[cost.type] = levelUpSpending.getValue(cost.type) + cost.amount
                        levelCostMap[cost.type] = cost.amount
                    }
                }
                currentLevel = nextLevel

                val potUnlock = potUnlockByLevel[nextLevel]
                var potCostMap: MutableMap<CropType, Int> = mutableMapOf()
                var potIdx = -1
                if (potUnlock != null) {
                    val canAffordPot = potUnlock.materialCosts.all { inventory.getValue(it.type) >= it.amount }
                    if (canAffordPot) {
                        for (cost in potUnlock.materialCosts) {
                            inventory[cost.type] = inventory.getValue(cost.type) - cost.amount
                            potUnlockSpending[cost.type] = potUnlockSpending.getValue(cost.type) + cost.amount
                            potCostMap[cost.type] = cost.amount
                        }
                        potIdx = potUnlock.potCount - 1
                        expandPots(potUnlock.potCount)
                    }
                }
                actions.add(LevelUpAction(nextLevel, levelCostMap, potIdx, potCostMap))
            }
            if (actions.isNotEmpty()) {
                materialTimeCost = computeMaterialTimeCost()
            }
            return actions
        }

        fun hasMaterialForCrop(crop: Crop): Boolean =
            crop.materialCosts.all { inventory.getValue(it.type) >= it.amount }

        fun deductCropCosts(crop: Crop) {
            for (cost in crop.materialCosts) {
                inventory[cost.type] = inventory.getValue(cost.type) - cost.amount
            }
        }

        fun allTargetsMet(): Boolean {
            return productionTargets.all { (type, target) ->
                produced.getValue(type) >= target
            }
        }

        data class CropTiming(
            val readyTime: Double,
            val harvestTime: Double,
            val predictedWateringCount: Int
        )

        fun predictCropTiming(
            crop: Crop,
            plantTime: Double,
            loginTimes: List<Double>,
            currentLoginIdx: Int
        ): CropTiming? {
            if (crop.growthTimeHours <= 0) return null
            val baseTime = crop.growthTimeHours.toDouble()
            val cooldown = baseTime * WateringSimulator.COOLDOWN_RATIO
            var lastWater = plantTime
            var wateringDone = 1
            var readyTime = plantTime + baseTime * WateringSimulator.getTimeMultiplier(1)

            for (i in (currentLoginIdx + 1) until loginTimes.size) {
                val nextLogin = loginTimes[i]
                if (nextLogin >= readyTime) break
                if (wateringDone >= WateringSimulator.MAX_WATERING_COUNT) break
                if (nextLogin - lastWater >= cooldown) {
                    wateringDone++
                    lastWater = nextLogin
                    readyTime = plantTime + baseTime * WateringSimulator.getTimeMultiplier(wateringDone)
                }
            }

            val harvestTime = loginTimes.drop(currentLoginIdx + 1).firstOrNull { it >= readyTime } ?: return null
            return CropTiming(
                readyTime = readyTime,
                harvestTime = harvestTime,
                predictedWateringCount = wateringDone
            )
        }

        var currentTime = 0.0
        var loginCount = 0
        var pendingBatchCount = 0
        var currentLoginIdx = 0

        run {
            val lvActions = tryLevelUp()
            if (lvActions.isNotEmpty()) {
                val potActions = lvActions.flatMap { action ->
                    buildList {
                        val costText = action.levelCosts.entries
                            .joinToString(", ") { "${it.key.displayName}×${it.value}" }
                        if (costText.isNotEmpty()) {
                            add(PotSchedule(-1, PotAction.IDLE, null, 0, "升级 Lv.${action.level} (消耗 $costText)"))
                        } else {
                            add(PotSchedule(-1, PotAction.IDLE, null, 0, "升级 Lv.${action.level}"))
                        }
                        if (action.potIndex >= 0 && action.potCosts.isNotEmpty()) {
                            val potCostText = action.potCosts.entries
                                .joinToString(", ") { "${it.key.displayName}×${it.value}" }
                            add(PotSchedule(-1, PotAction.IDLE, null, 0, "购买花盆${action.potIndex + 1} (消耗 $potCostText)"))
                        }
                    }
                }
                schedule.add(LoginScheduleEvent(loginCount, currentTime, potActions, inventory.toMap(), currentLevel))
                loginCount++
            }
        }

        while (currentLoginIdx < loginTimes.size && currentLoginIdx < MAX_LOGINS) {
            currentTime = loginTimes[currentLoginIdx]
            if (allTargetsMet() && pots.all { it == null } && pendingBatchCount == 0) break

            val potActions = mutableListOf<PotSchedule>()
            var hasActivity = false

            // Harvest matured crops
            for (j in pots.indices) {
                val slot = pots[j] ?: continue
                if (currentTime >= slot.readyTime - 0.001) {
                    val yieldAmount = (slot.crop.baseYield *
                        HarvestProbability.expectedYieldMultiplier(slot.wateringDone)).toInt()
                        .coerceAtLeast(1) * slot.batchCount
                    for (type in slot.crop.types) {
                        inventory[type] = inventory.getValue(type) + yieldAmount
                        produced[type] = produced.getValue(type) + yieldAmount
                    }
                    pendingBatchCount = (pendingBatchCount - 1).coerceAtLeast(0)
                    potActions.add(PotSchedule(j, PotAction.HARVEST, slot.crop, slot.batchCount))
                    pots[j] = null
                    hasActivity = true
                }
            }

            // Level up if needed
            run {
                val lvActions = tryLevelUp()
                for (action in lvActions) {
                    val costText = action.levelCosts.entries
                        .joinToString(", ") { "${it.key.displayName}×${it.value}" }
                    if (costText.isNotEmpty()) {
                        potActions.add(PotSchedule(-1, PotAction.IDLE, null, note = "升级 Lv.${action.level} (消耗 $costText)"))
                    } else {
                        potActions.add(PotSchedule(-1, PotAction.IDLE, null, note = "升级 Lv.${action.level}"))
                    }
                    if (action.potIndex >= 0 && action.potCosts.isNotEmpty()) {
                        val potCostText = action.potCosts.entries
                            .joinToString(", ") { "${it.key.displayName}×${it.value}" }
                        potActions.add(PotSchedule(-1, PotAction.IDLE, null, note = "购买花盆${action.potIndex + 1} (消耗 $potCostText)"))
                    }
                    hasActivity = true
                }
            }

            if (allTargetsMet() && pots.all { it == null }) {
                if (hasActivity) {
                    schedule.add(LoginScheduleEvent(loginCount, currentTime, potActions, inventory.toMap(), currentLevel))
                    loginCount++
                }
                break
            }

            // Plant crops (with retry loop to merge harvest+plant into single login)
            var plantedThisRound = true
            while (plantedThisRound) {
                plantedThisRound = false
                for (j in pots.indices) {
                    if (pots[j] != null) continue

                    val remainingNeeds = productionTargets.mapValues { (type, target) ->
                        (target - produced.getValue(type)).coerceAtLeast(0)
                    }.filter { it.value > 0 }

                    if (remainingNeeds.isEmpty()) break

                    var bestCrop: Crop? = null
                    var bestTiming: CropTiming? = null
                    var bestScore = 0.0

                    val candidates = Crop.CROPS.filter {
                        it.growthTimeHours > 0 &&
                            it.name !in setOf("毛头鬼伞", "毛茛") &&
                            (it.unlockLevel == null || it.unlockLevel <= currentLevel) &&
                            it.types.any { type -> remainingNeeds.getOrDefault(type, 0) > 0 } &&
                            hasMaterialForCrop(it)
                    }

                    for (crop in candidates) {
                        val timing = predictCropTiming(crop, currentTime, loginTimes, currentLoginIdx) ?: continue
                        val harvestDelay = timing.harvestTime - currentTime
                        if (harvestDelay <= 0.0) continue

                        val expectedYield = (crop.baseYield *
                            HarvestProbability.expectedYieldMultiplier(timing.predictedWateringCount)).toInt().coerceAtLeast(1)

                        val usefulYield = crop.types.sumOf { type ->
                            minOf(expectedYield, remainingNeeds.getOrDefault(type, 0))
                        }.toDouble()
                        if (usefulYield <= 0.0) continue

                        val materialCostTime = crop.materialCosts.sumOf { cost ->
                            val unit = materialTimeCost[cost.type] ?: Double.POSITIVE_INFINITY
                            if (!unit.isFinite()) return@sumOf Double.POSITIVE_INFINITY
                            cost.amount * unit
                        }
                        if (!materialCostTime.isFinite()) continue

                        val score = usefulYield / (harvestDelay + materialCostTime)
                        if (score > bestScore) {
                            bestScore = score
                            bestCrop = crop
                            bestTiming = timing
                        }
                    }

                    val crop = bestCrop ?: continue
                    val timing = bestTiming ?: continue
                    deductCropCosts(crop)
                    val baseTime = crop.growthTimeHours.toDouble()
                    val cooldown = WateringSimulator.wateringCooldown(crop.growthTimeHours)
                    pots[j] = PotSlot(
                        crop = crop,
                        batchCount = 1,
                        plantTime = currentTime,
                        baseGrowthTime = baseTime,
                        cooldown = cooldown,
                        wateringDone = 1,
                        lastWaterTime = currentTime,
                        readyTime = timing.readyTime
                    )
                    pendingBatchCount++
                    potActions.add(PotSchedule(j, PotAction.PLANT, crop, 1))
                    potActions.add(PotSchedule(j, PotAction.WATER, crop, note = "浇水 1/${WateringSimulator.MAX_WATERING_COUNT}"))
                    hasActivity = true
                    plantedThisRound = true
                }
            }

            // Water crops if needed
            for (j in pots.indices) {
                val slot = pots[j] ?: continue
                if (slot.baseGrowthTime <= 0) continue
                val maxWater = 3 // Max 3 waterings: 0->100%, 1->80%, 2->60%, 3->50%
                if (slot.wateringDone >= maxWater) continue
                if (currentTime >= slot.readyTime - 0.001) continue

                val timeSinceLastWater = currentTime - slot.lastWaterTime
                if (timeSinceLastWater >= slot.cooldown - 0.001) {
                    slot.wateringDone++
                    slot.lastWaterTime = currentTime
                    slot.readyTime = slot.plantTime + slot.baseGrowthTime *
                        WateringSimulator.getTimeMultiplier(slot.wateringDone)
                    potActions.add(PotSchedule(j, PotAction.WATER, slot.crop,
                        note = "浇水 ${slot.wateringDone}/$maxWater"))
                    hasActivity = true
                }
            }

            for (j in pots.indices) {
                if (potActions.none { it.potIndex == j }) {
                    potActions.add(PotSchedule(j, PotAction.IDLE, null))
                }
            }

            if (hasActivity) {
                schedule.add(LoginScheduleEvent(loginCount, currentTime, potActions, inventory.toMap(), currentLevel))
                loginCount++
            }

            currentLoginIdx++
        }

        val totalTime = if (schedule.isEmpty()) 0.0
        else pots.filterNotNull().maxOfOrNull { it.readyTime } ?: schedule.last().timeHours

        return SimulationResult(
            loginSchedule = schedule,
            totalTimeHours = totalTime,
            totalLoginCount = loginCount,
            productionSummary = produced,
            levelUpSpending = levelUpSpending,
            potUnlockSpending = potUnlockSpending,
            loginTimes = loginTimes
        )
    }

    private fun simulateCropReadyMode(
        productionTargets: Map<CropType, Int>,
        potCount: Int,
        startTime: Double,
        maxHours: Double,
        existingInventory: Map<CropType, Int>,
        startLevel: Int,
        targetLevel: Int,
        isLevelMode: Boolean
    ): SimulationResult {
        val endTime = startTime + maxHours
        val levelUpCosts = cn.qfys521.raingardencalculate.model.LevelUpgrade.UPGRADES
        val inventory = existingInventory.toMutableMap().withDefault { 0 }
        val produced = mutableMapOf<CropType, Int>().withDefault { 0 }
        val schedule = mutableListOf<LoginScheduleEvent>()
        var currentLevel = startLevel
        val pots = MutableList<PotSlot?>(potCount) { null }
        
        data class EventTime(val time: Double, val type: String, val potIndex: Int = -1)
        val events = mutableListOf<EventTime>()
        
        var currentTime = startTime
        var loginCount = 0
        for (i in pots.indices) {
            events.add(EventTime(currentTime, "PLANT", i))
        }

        // 生成所有事件的队列
        while (currentTime < endTime && loginCount < MAX_LOGINS) {
            // 推进到下一个事件时间
            if (events.isEmpty()) break
            
            currentTime = events.minOf { it.time }
            if (currentTime >= endTime) break
            
            // 处理当前时间的所有事件
            val currentEvents = events.filter { it.time <= currentTime + 0.001 }.sortedBy { it.type }
            events.removeAll { it.time <= currentTime + 0.001 }
            
            val loginActions = mutableListOf<PotSchedule>()
            
            // 处理升级
            while (currentLevel < targetLevel) {
                val nextLevel = currentLevel + 1
                val upgrade = levelUpCosts.find { it.level == nextLevel } ?: break
                if (!upgrade.materialCosts.all { inventory.getValue(it.type) >= it.amount }) break
                upgrade.materialCosts.forEach { inventory[it.type] = inventory.getValue(it.type) - it.amount }
                currentLevel = nextLevel
                val costText = upgrade.materialCosts.joinToString(", ") { "${it.type.displayName}×${it.amount}" }
                loginActions.add(PotSchedule(-1, PotAction.IDLE, null, 0, "升级至 $nextLevel 级"))
            }
            
            // 处理收获
            for (i in pots.indices) {
                val slot = pots[i] ?: continue
                if (slot.readyTime <= currentTime + 0.001) {
                    val yieldAmount = (slot.crop.baseYield * HarvestProbability.expectedYieldMultiplier(slot.wateringDone)).toInt().coerceAtLeast(1)
                    for (type in slot.crop.types) {
                        inventory[type] = inventory.getValue(type) + yieldAmount
                        produced[type] = produced.getValue(type) + yieldAmount
                    }
                    loginActions.add(PotSchedule(i, PotAction.HARVEST, slot.crop, 1))
                    pots[i] = null
                    events.add(EventTime(currentTime, "PLANT", i))
                }
            }
            
            // 处理种植
            val targetMet = productionTargets.all { (type, target) -> produced.getValue(type) >= target }
            if (!targetMet) {
                for (event in currentEvents.filter { it.type == "PLANT" }) {
                    val i = event.potIndex
                    if (pots[i] != null) continue
                    
                    val remainingNeeds = productionTargets.mapValues { (type, target) ->
                        (target - produced.getValue(type)).coerceAtLeast(0)
                    }.filter { it.value > 0 }
                    
                    if (remainingNeeds.isEmpty()) continue
                    
                    var bestCrop: Crop? = null
                    var bestScore = 0.0
                    
                    for (crop in Crop.CROPS) {
                        if (crop.growthTimeHours <= 0) continue
                        if (crop.name in setOf("毛头鬼伞", "毛茛")) continue
                        if (crop.unlockLevel != null && crop.unlockLevel > currentLevel) continue
                        if (!crop.types.any { remainingNeeds.getOrDefault(it, 0) > 0 }) continue
                        if (!crop.materialCosts.all { inventory.getValue(it.type) >= it.amount }) continue
                        val score = crop.types.sumOf { remainingNeeds.getOrDefault(it, 0) }.toDouble() / crop.growthTimeHours
                        if (score > bestScore) {
                            bestScore = score
                            bestCrop = crop
                        }
                    }
                    
                    if (bestCrop != null) {
                        bestCrop.materialCosts.forEach { inventory[it.type] = inventory.getValue(it.type) - it.amount }
                        val base = bestCrop.growthTimeHours.toDouble()
                        val cooldown = base * WateringSimulator.COOLDOWN_RATIO
                        
                        var maxWatering = 1
                        for (n in 2..WateringSimulator.MAX_WATERING_COUNT) {
                            val waterTime = currentTime + n * cooldown
                            val readyTime = currentTime + base * WateringSimulator.getTimeMultiplier(n)
                            if (waterTime > readyTime + 0.001) break
                            maxWatering = n
                        }
                        
                        val finalReadyTime = currentTime + base * WateringSimulator.getTimeMultiplier(maxWatering)
                        pots[i] = PotSlot(bestCrop, 1, currentTime, base, cooldown, maxWatering, currentTime + cooldown, finalReadyTime)
                        loginActions.add(PotSchedule(i, PotAction.PLANT, bestCrop, 1))
                        
                        // 添加浇水事件
                        for (n in 1..maxWatering) {
                            val waterTime = currentTime + n * cooldown
                            events.add(EventTime(waterTime, "WATER", i))
                        }
                        
                        // 添加收获事件
                        events.add(EventTime(finalReadyTime, "HARVEST", i))
                    }
                }
            }
            
            // 处理浇水
            for (event in currentEvents.filter { it.type == "WATER" }) {
                val i = event.potIndex
                val slot = pots[i] ?: continue
                loginActions.add(PotSchedule(i, PotAction.WATER, slot.crop, 0, "浇水"))
                slot.wateringDone++
                slot.lastWaterTime = currentTime
            }
            
            if (loginActions.isNotEmpty()) {
                schedule.add(LoginScheduleEvent(loginCount, currentTime, loginActions, inventory.toMap(), currentLevel))
                loginCount++
            }
            
            if (!targetMet && loginActions.isEmpty() && events.isEmpty()) break
            if (targetMet && pots.all { it == null }) break
        }

        val totalTime = pots.filterNotNull().maxOfOrNull { it.readyTime } ?: schedule.lastOrNull()?.timeHours ?: startTime
        return SimulationResult(
            loginSchedule = schedule,
            totalTimeHours = totalTime,
            totalLoginCount = loginCount,
            productionSummary = produced,
            loginTimes = listOf(startTime, -(maxHours / 24.0))
        )
    }

    private fun simulateEventDriven(
        productionTargets: Map<CropType, Int>,
        potCount: Int,
        startTime: Double,
        maxHours: Double,
        existingInventory: Map<CropType, Int>,
        startLevel: Int,
        targetLevel: Int,
        isLevelMode: Boolean
    ): SimulationResult {
        return simulateCropReadyMode(productionTargets, potCount, startTime, maxHours, existingInventory, startLevel, targetLevel, isLevelMode)
    }

    fun generateFixedLoginTimes(intervalHours: Double, maxLogins: Int = MAX_LOGINS): List<Double> {
        return (0 until maxLogins).map { it * intervalHours }.takeWhile { it < MAX_SIM_TIME }
    }
}

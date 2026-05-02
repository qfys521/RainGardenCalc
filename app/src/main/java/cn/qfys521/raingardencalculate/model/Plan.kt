package cn.qfys521.raingardencalculate.model

data class PlantingAction(
    val crop: Crop,
    val batchCount: Int,
    val wateringCount: Int,
    val cycleTimeHours: Double,
    val totalYield: Int,
    val loginIndex: Int = 0
)

enum class PotAction { PLANT, HARVEST, WATER, PLANT_AND_HARVEST, IDLE }

data class PotSchedule(
    val potIndex: Int,
    val action: PotAction,
    val crop: Crop?,
    val batchCount: Int = 0,
    val note: String = ""
)

data class LoginScheduleEvent(
    val loginIndex: Int,
    val timeHours: Double,
    val potActions: List<PotSchedule>,
    val inventory: Map<CropType, Int> = emptyMap(),
    val level: Int = 1
)

data class LevelCropUsage(
    val level: Int,
    val cropName: String,
    val cropTypes: List<CropType>,
    val planted: Int,
    val harvested: Int,
    val growthTimeHours: Long
)

data class Plan(
    val steps: List<PlantingAction>,
    val loginSchedule: List<LoginScheduleEvent>,
    val totalTimeHours: Double,
    val totalLoginCount: Int,
    val potCount: Int,
    val loginIntervalHours: Double,
    val materialNeeds: Map<CropType, Int>,
    val existingInventory: Map<CropType, Int>,
    val netNeeds: Map<CropType, Int>,
    val productionSummary: Map<CropType, Int>,
    val levelUpSpending: Map<CropType, Int> = emptyMap(),
    val potUnlockSpending: Map<CropType, Int> = emptyMap(),
    val loginTimes: List<Double> = emptyList(),
    val cropProgression: List<LevelCropUsage> = emptyList(),
    val isAlreadySatisfied: Boolean = false
)

package cn.qfys521.raingardencalculate.model

data class Crop(
    val name: String,
    val types: List<CropType>,
    val unlockLevel: Int?, // null means no level requirement
    val baseYield: Int,
    val growthTimeHours: Long, // -1 means instant (毛茛)
    val materialCosts: List<MaterialCost> = emptyList()
) {
    companion object {
        val CROPS = listOf(
            Crop("双孢蘑菇", listOf(CropType.MUSHROOM, CropType.FOOD), null, 8, 1),
            Crop("水蕨菜", listOf(CropType.FOOD), 3, 45, 6, listOf(MaterialCost(CropType.MUSHROOM, 5))),
            Crop("勿忘草", listOf(CropType.ORNAMENTAL), 4, 7, 12),
            Crop("水晶兰", listOf(CropType.ORNAMENTAL), 6, 20, 32, listOf(MaterialCost(CropType.ORNAMENTAL, 5))),
            Crop("蓝莓", listOf(CropType.FOOD), 8, 300, 96, listOf(MaterialCost(CropType.ORNAMENTAL, 5))),
            Crop("草菇", listOf(CropType.MUSHROOM, CropType.FOOD), 10, 18, 1, listOf(MaterialCost(CropType.MUSHROOM, 5))),
            Crop("卡宴辣椒", listOf(CropType.FUNCTIONAL), 11, 3, 6, listOf(MaterialCost(CropType.FOOD, 10))),
            Crop("铃兰", listOf(CropType.ORNAMENTAL), 13, 10, 12, listOf(MaterialCost(CropType.FUNCTIONAL, 1))),
            Crop("大柱霉草", listOf(CropType.ORNAMENTAL), 15, 23, 32, listOf(MaterialCost(CropType.FUNCTIONAL, 3))),
            Crop("草莓", listOf(CropType.FOOD), 17, 400, 96, listOf(MaterialCost(CropType.FUNCTIONAL, 3))),
            Crop("香菇", listOf(CropType.MUSHROOM, CropType.FOOD), 19, 45, 1, listOf(MaterialCost(CropType.FOOD, 30))),
            Crop("傅氏凤尾蕨", listOf(CropType.ORNAMENTAL, CropType.FOOD), 21, 5, 6, listOf(MaterialCost(CropType.FOOD, 25))),
            Crop("毛头鬼伞", listOf(CropType.MUSHROOM, CropType.FOOD), null, 5, 1),
            Crop("毛茛", listOf(CropType.ORNAMENTAL, CropType.FUNCTIONAL), null, 1, -1)
        )
    }
}

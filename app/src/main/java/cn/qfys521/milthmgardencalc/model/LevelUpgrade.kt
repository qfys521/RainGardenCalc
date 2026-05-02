package cn.qfys521.milthmgardencalc.model

data class LevelUpgrade(
    val level: Int,
    val materialCosts: List<MaterialCost>,
    val cumulativeCosts: List<MaterialCost>
) {
    companion object {
        val UPGRADES = listOf(
            LevelUpgrade(2, listOf(MaterialCost(CropType.FOOD, 5)),
                listOf(MaterialCost(CropType.FOOD, 5))),
            LevelUpgrade(3, listOf(MaterialCost(CropType.FOOD, 40)),
                listOf(MaterialCost(CropType.FOOD, 45))),
            LevelUpgrade(4, listOf(MaterialCost(CropType.FOOD, 80)),
                listOf(MaterialCost(CropType.FOOD, 125))),
            LevelUpgrade(5, listOf(MaterialCost(CropType.FOOD, 100)),
                listOf(MaterialCost(CropType.FOOD, 225))),
            LevelUpgrade(6, listOf(MaterialCost(CropType.FOOD, 150)),
                listOf(MaterialCost(CropType.FOOD, 375))),
            LevelUpgrade(7, listOf(MaterialCost(CropType.ORNAMENTAL, 5)),
                listOf(MaterialCost(CropType.FOOD, 375), MaterialCost(CropType.ORNAMENTAL, 5))),
            LevelUpgrade(8, listOf(MaterialCost(CropType.ORNAMENTAL, 20)),
                listOf(MaterialCost(CropType.FOOD, 375), MaterialCost(CropType.ORNAMENTAL, 25))),
            LevelUpgrade(9, listOf(MaterialCost(CropType.FOOD, 350)),
                listOf(MaterialCost(CropType.FOOD, 725), MaterialCost(CropType.ORNAMENTAL, 25))),
            LevelUpgrade(10, listOf(MaterialCost(CropType.ORNAMENTAL, 35)),
                listOf(MaterialCost(CropType.FOOD, 725), MaterialCost(CropType.ORNAMENTAL, 60))),
            LevelUpgrade(11, listOf(MaterialCost(CropType.FOOD, 300), MaterialCost(CropType.ORNAMENTAL, 10)),
                listOf(MaterialCost(CropType.FOOD, 1025), MaterialCost(CropType.ORNAMENTAL, 70))),
            LevelUpgrade(12, listOf(MaterialCost(CropType.FOOD, 400), MaterialCost(CropType.ORNAMENTAL, 10)),
                listOf(MaterialCost(CropType.FOOD, 1425), MaterialCost(CropType.ORNAMENTAL, 80))),
            LevelUpgrade(13, listOf(MaterialCost(CropType.FOOD, 500), MaterialCost(CropType.ORNAMENTAL, 20)),
                listOf(MaterialCost(CropType.FOOD, 1925), MaterialCost(CropType.ORNAMENTAL, 100))),
            LevelUpgrade(14, listOf(MaterialCost(CropType.FOOD, 600), MaterialCost(CropType.ORNAMENTAL, 20)),
                listOf(MaterialCost(CropType.FOOD, 2525), MaterialCost(CropType.ORNAMENTAL, 120))),
            LevelUpgrade(15, listOf(MaterialCost(CropType.ORNAMENTAL, 100)),
                listOf(MaterialCost(CropType.FOOD, 2525), MaterialCost(CropType.ORNAMENTAL, 220))),
            LevelUpgrade(16, listOf(MaterialCost(CropType.FOOD, 800)),
                listOf(MaterialCost(CropType.FOOD, 3325), MaterialCost(CropType.ORNAMENTAL, 220))),
            LevelUpgrade(17, listOf(MaterialCost(CropType.FOOD, 800)),
                listOf(MaterialCost(CropType.FOOD, 4125), MaterialCost(CropType.ORNAMENTAL, 220))),
            LevelUpgrade(18, listOf(MaterialCost(CropType.ORNAMENTAL, 80)),
                listOf(MaterialCost(CropType.FOOD, 4125), MaterialCost(CropType.ORNAMENTAL, 300))),
            LevelUpgrade(19, listOf(MaterialCost(CropType.FOOD, 800), MaterialCost(CropType.ORNAMENTAL, 30)),
                listOf(MaterialCost(CropType.FOOD, 4925), MaterialCost(CropType.ORNAMENTAL, 330))),
            LevelUpgrade(20, listOf(MaterialCost(CropType.FOOD, 1200)),
                listOf(MaterialCost(CropType.FOOD, 6125), MaterialCost(CropType.ORNAMENTAL, 330))),
            LevelUpgrade(21, listOf(MaterialCost(CropType.ORNAMENTAL, 120)),
                listOf(MaterialCost(CropType.FOOD, 6125), MaterialCost(CropType.ORNAMENTAL, 450))),
            LevelUpgrade(22, listOf(MaterialCost(CropType.ORNAMENTAL, 50), MaterialCost(CropType.FUNCTIONAL, 10)),
                listOf(MaterialCost(CropType.FOOD, 6125), MaterialCost(CropType.ORNAMENTAL, 500), MaterialCost(CropType.FUNCTIONAL, 10)))
        )
    }
}

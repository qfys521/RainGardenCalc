package cn.qfys521.raingardencalculate.model

data class FlowerPot(
    val name: String,
    val unlockLevel: Int,
    val materialCosts: List<MaterialCost> = emptyList()
) {
    companion object {
        val POTS = listOf(
            FlowerPot("1号", 1),
            FlowerPot("2号", 2, listOf(MaterialCost(CropType.MUSHROOM, 5))),
            FlowerPot("3号", 10, listOf(
                MaterialCost(CropType.FOOD, 80),
                MaterialCost(CropType.ORNAMENTAL, 20)
            )),
            FlowerPot("4号", 15, listOf(MaterialCost(CropType.FUNCTIONAL, 40)))
        )
    }
}

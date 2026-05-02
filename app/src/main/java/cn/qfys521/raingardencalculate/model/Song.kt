package cn.qfys521.raingardencalculate.model

data class Song(
    val name: String,
    val materialCosts: List<MaterialCost>
) {
    companion object {
        val SONGS = listOf(
            Song("Garden", listOf(MaterialCost(CropType.MUSHROOM, 10))),
            Song("Fantasia Sonata Botanical Garden", listOf(MaterialCost(CropType.ORNAMENTAL, 20))),
            Song("Dum! Dum!! Dum!!!", listOf(MaterialCost(CropType.FOOD, 60))),
            Song("Splash the Beat!!", listOf(MaterialCost(CropType.FOOD, 300))),
            Song("Sound of Nature", listOf(
                MaterialCost(CropType.MUSHROOM, 30),
                MaterialCost(CropType.ORNAMENTAL, 80)
            )),
            Song("conflict", listOf(
                MaterialCost(CropType.MUSHROOM, 20),
                MaterialCost(CropType.ORNAMENTAL, 25)
            )),
            Song("Melody to Heaven", listOf(MaterialCost(CropType.FOOD, 100))),
            Song("LiFE Garden", listOf(MaterialCost(CropType.FOOD, 1200))),
            Song("Habitable zone", listOf(MaterialCost(CropType.FOOD, 200))),
            Song("烽閻鸞錵", listOf(MaterialCost(CropType.ORNAMENTAL, 20))),
            Song("Fantasia Sonata Surmount Vanity Aufleben", listOf(MaterialCost(CropType.ORNAMENTAL, 20))),
            Song("furioso melodia", listOf(MaterialCost(CropType.FOOD, 200))),
            Song("Class Memories", listOf(MaterialCost(CropType.FOOD, 200))),
            Song("白色の夢で。", listOf(MaterialCost(CropType.ORNAMENTAL, 20)))
        )
    }
}

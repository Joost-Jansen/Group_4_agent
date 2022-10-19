package furhatos.app.agent.nlu


import furhatos.nlu.ComplexEnumEntity
import furhatos.nlu.EnumEntity
import furhatos.util.Language
import java.io.BufferedReader
import java.io.FileReader
import java.lang.Exception


class Ingredients : EnumEntity() {
    override fun getEnum(lang: Language): List<String> {
        return readCsv()
    }
}

class Cuisine : EnumEntity() {
    override fun getEnum(lang: Language): List<String> {
        return listOf("African", "American", "British", " Cajun", "Caribbean", "Chinese", "Eastern European", "European", "French",
            "German", "Greek", "Indian", "Irish", "Italian", "Japanese", "Jewish", "Korean", "Latin American", "Mediterranean",
            "Mexican", "Middle Eastern", "Nordic", "Southern", "Spanish", "Thai", "Vietnamese")
    }
}

class Diet : EnumEntity() {
    override fun getEnum(lang: Language): List<String> {
        return listOf("Gluten Free", "Ketogenic", "Vegetarian", "Lacto-Vegetarian", "Ovo-Vegetarian", "Vegan", "Pescetarian", "Paleo", "Primal",
            "Low FODMAP", "Whole30")
    }
}

class Intolerances : EnumEntity() {
    override fun getEnum(lang: Language): List<String> {
        return listOf("Diary", "Egg", "Gluten", "Grain", "Peanut", "Seafood", "Pescetarian", "Sesame", "Shellfish",
            "Soy", "Sulfite", "Tree Nut", "Wheat")
    }
}

class MealType : EnumEntity() {
    override fun getEnum(lang: Language): List<String> {
        return listOf("main course", "side dish", "dessert", "appetizer", "salad", "bread", "breakfast", "soup", "beverage",
            "sauce", "marinade", "fingerfood", "snack", "drink")
    }
}

class Preparation : EnumEntity() {
    override fun getEnum(lang: Language): List<String> {
        return listOf("boiled", "steamed", "baked", "fried", "grilled")
    }
}

class foodPreperation(
    val prep : Preparation? = null,
    val ingredient: Ingredients? = null) : ComplexEnumEntity() {

    override fun getEnum(lang: Language): List<String> {
        return listOf("@prep @ingredient", "@ingredient")
    }
}
fun readCsv(): MutableList<String> {
    val ingredients = mutableListOf<String>()

    try {
        val fileName = "src\\main\\Data\\ingredients.csv"
        val br = BufferedReader(FileReader(fileName))
        var line = br.readLine()
        while(line != null) {
            val ingredient = line.split(';')[0]
            ingredients.add(ingredient)
            line = br.readLine()
        }
    } catch (e:Exception) {
        e.printStackTrace()
    }

    return ingredients
}
//fun main(args: Array<String>) {
//    val base_query = "https://api.spoonacular.com?apiKey=e9eeb0d76f024efcaf7cd32ae444c899"
//    val a = options(base_query)
//    readCsv()
//}
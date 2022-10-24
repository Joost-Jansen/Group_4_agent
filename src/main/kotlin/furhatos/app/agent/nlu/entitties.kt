package furhatos.app.agent.nlu


import furhatos.app.agent.resources.*
import furhatos.nlu.ComplexEnumEntity
import furhatos.nlu.EnumEntity
import furhatos.nlu.ListEntity
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
        return getCuisines()
    }
}

class Diet : EnumEntity() {
    override fun getEnum(lang: Language): List<String> {
        return getDiets()
    }
}

class ListOfDiets : ListEntity<Diet>()

class Allergy : EnumEntity() {
    override fun getEnum(lang: Language): List<String> {
        return getIntolerances()
    }
}

class ListOfAllergies : ListEntity<Allergy>()

class MealType : EnumEntity() {
    override fun getEnum(lang: Language): List<String> {
        return getMealTypes()
    }
}

class Preparation : EnumEntity() {
    override fun getEnum(lang: Language): List<String> {
        return listOf("boiled", "steamed", "baked", "fried", "grilled")
    }
}

class FoodPreperation(
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


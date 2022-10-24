package furhatos.app.agent.nlu


import furhatos.app.agent.resources.getAllergies
import furhatos.app.agent.resources.getCuisines
import furhatos.app.agent.resources.getDiets
import furhatos.app.agent.resources.getMealTypes
import furhatos.nlu.*
import furhatos.nlu.grammar.Grammar
import furhatos.nlu.kotlin.grammar
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

class Fruit : EnumEntity()


class ListOfAllergies : ListEntity<Allergy>()



class ListOfDiets : ListEntity<Diet>()


class Allergy(
    val all : String? = null
) : GrammarEntity() {

    override fun getGrammar(lang : Language) : Grammar {
        return when (lang.main) {
            "en" -> allergies
            else -> throw InterpreterException("Language $lang not supported for ${javaClass.name}")
        }
    }
}

val allergies =
    grammar {
        rule(public = true) {
            ruleref("aller")
        }
        rule("aller") {
            +("lactose intolerant" / "milk") tag {"diary"}
            +"diary" tag {"diary"}
            +("egg" / "eggs") tag {"egg"}
            +"celiac disease" tag {"gluten"}
            +("peanut" / "peanuts") tag {"peanut"}
            +"seafood" tag {"seafood"}
            +("fish" / "shrimp"  / "mussels") tag {"seafood"}
            +"Shellfish" tag {"Shellfish"}
            +"Soy" tag {"Soy"}
            +"Sulfite" tag {"Sulfite"}
            +"Tree nut" tag {"Tree nut"}
            +("nut" / "nuts") tag {"Tree nut"}
            +"Wheat" tag {"Wheat"}
        }
    }

class Diet(
    val all : String? = null
) : GrammarEntity() {

    override fun getGrammar(lang : Language) : Grammar {
        return when (lang.main) {
            "en" -> Diets
            else -> throw InterpreterException("Language $lang not supported for ${javaClass.name}")
        }
    }
}

val Diets =
    grammar {
        rule(public = true) {
            ruleref("aller")
        }
        rule("aller") {
            +("Gluten Free") tag {"Gluten Free"}
            +"Ketogenic" tag {"Ketogenic"}
            +("high fat"  / "low carb" / "low carbohydrates") tag {"Ketogenic"}
            +"Vegetarian" tag {"Vegetarian"}
            +"Lacto-Vegetarian" tag {"Lacto-Vegetarian"}
            +"Ovo-Vegetarian" tag {"Ovo-Vegetarian"}
            +"Vegan" tag {"Vegan"}
            +"Pescetarian" tag {"Pescetarian"}
            +"Paleo" tag {"Paleo"}
            +"Liver king" tag {"Paleo"}
            +"Primal" tag {"Primal"}
            +"Low FODMAP" tag {"Low FODMAP"}
            +"Whole30" tag {"Whole30"}
            +"whole thrifty" tag {"Whole30"}
        }
    }



//        "Gluten Free",
//        "Ketogenic",
//        "Vegetarian",
//        "Lacto-Vegetarian",
//        "Ovo-Vegetarian",
//        "Vegan",
//        "Pescetarian",
//        "Paleo",
//        "Primal",
//        "Low FODMAP",
//        "Whole30"
//






//class MealType : EnumEntity() {
//    override fun getEnum(lang: Language): List<String> {
//        listOf("main course", "dinner", "side dish", "dessert", "appetizer",
//        "breakfast", "brunch", "supper")
//    }
//}

class MealT(
    val meal : String? = null
) : GrammarEntity() {

    override fun getGrammar(lang : Language) : Grammar {
        return when (lang.main) {
            "en" -> mealType
            else -> throw InterpreterException("Language $lang not supported for ${javaClass.name}")
        }
    }
}

val mealType =
    grammar {
        rule(public = true) {
            ruleref("meal")
        }
        rule("meal") {
            +("supper" / "dinner") tag {"main course"}
            +"main course" tag {"main course"}
            +"side dish" tag {"side dish"}
            +("dessert" / "desert") tag {"dessert"}
            +"appetizer" tag {"appetizer"}
            +"breakfast" tag {"breakfast"}
            +("lunch" / "brunch") tag {"breakfast"}
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


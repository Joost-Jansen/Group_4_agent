package furhatos.app.agent.nlu


import furhatos.nlu.ComplexEnumEntity
import furhatos.nlu.EnumEntity
import furhatos.nlu.ListEntity
import furhatos.nlu.WildcardEntity
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

class Cuisine(
    val cuis : String? = null
) : GrammarEntity() {

    override fun getGrammar(lang : Language) : Grammar {
        return when (lang.main) {
            "en" -> kuisine
            else -> throw InterpreterException("Language $lang not supported for ${javaClass.name}")
        }
    }
}

val kuisine =
    grammar {
        rule(public = true) {
            ruleref("aller")
        }
        rule("aller") {
            +"African" tag {"African"}
            +("North african" / "North africa" / "Africa") tag {"African"}
            +"American" tag {"American"}
            +("United states" / "North america") tag {"American"}
            +"British" tag {"British"}
            +("English" / "Scottish" / "England" / "Scotland" / "Dutch") tag {"British"}
            +"Cajun" tag {"Cajun"}
            +"Caribbean" tag {"Caribbean"}
            +"Chinese" tag {"Chinese"}
            +("Asian" / "Cina") tag {"Chinese"}
            +"Eastern European" tag {"Eastern European"}
            +("Polish" / "Poland" / "Romanian" / "Hungarian") tag {"Eastern European"}
            +"European" tag {"European"}
            +"French" tag {"French"}
            +("France") tag {"French"}
            +"German" tag {"German"}
            +("Germany") tag {"German"}
            +"Greek" tag {"Greek"}
            +("Greece" / "Turkish" / "Turkey") tag {"Greek"}
            +"Indian" tag {"Indian"}
            +("India") tag {"Indian"}
            +"Irish" tag {"Irish"}
            +("Ireland" / "North Ireland") tag {"Irish"}
            +"Italian" tag {"Italian"}
            +("Italy") tag {"Italian"}
            +"Jewish" tag {"Jewish"}
            +("Kosher") tag {"Jewish"}
            +"Korean" tag {"Korean"}
            +("Korea") tag {"Korean"}
            +"Latin American" tag {"Latin American"}
            +("Columbian" / "Columbia" / "Argentinian" / "Argentinian") tag {"Latin American"}
            +"Mediterranean" tag {"Mediterranean"}
            +"Mexican" tag {"Mexican"}
            +("Tex-mex" / "Mexico") tag {"Mexican"}
            +"Middle Eastern" tag {"Middle Eastern"}
            +"Nordic" tag {"Nordic"}
            +("Scandinavian" / "Swedish") tag {"Nordic"}
            +"Southern" tag {"Southern"}
            +("Surinam" / "Suriname") tag {"Southern"}
            +"Spanish" tag {"Spanish"}
            +("Spain") tag {"Spanish"}
            +"Thai" tag {"Thai"}
            +("Thailand") tag {"Thai"}
            +"Vietnamese" tag {"Vietnamese"}
            +("Vietnam") tag {"Vietnamese"}

        }
    }

class ListOfAllergies : ListEntity<Allergy>()



class ListOfDiets : ListEntity<Diet>()


class Allergy(
    val alterAll : String? = null
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
            +"Gluten" tag {"Gluten"}
        }
    }

class Diet(
    val alterDiet : String? = null
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
            +("lunch" / "brunch") tag {"breakfast|salad|soup|"}
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

class textEntity: WildcardEntity("textInput", wildCardIntent())

class negativeWildCardEntity(val textInput : String? = null) : ComplexEnumEntity() {
    override fun getEnum(lang: Language): List<String> {
        return listOf(
            "no @textInput",
            "nah @textInput"
        )
    }
}


class positiveWildCardEntity(val textInput : String? = null) : ComplexEnumEntity() {
    override fun getEnum(lang: Language): List<String> {
        return listOf(
            "yes @textInput",
            "yeah @textInput"
        )
    }
}
//fun main(args: Array<String>) {
//    val base_query = "https://api.spoonacular.com?apiKey=e9eeb0d76f024efcaf7cd32ae444c899"
//    val a = options(base_query)
//    readCsv()
//}


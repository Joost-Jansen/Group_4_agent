package furhatos.app.agent.nlu

import furhatos.nlu.Intent
import furhatos.nlu.TextGenerator
import furhatos.util.Language

open class TellPersonalInformation : Intent(), TextGenerator {
    var diets: ListOfDiets? = null
    var allergies: ListOfAllergies? = null

    override fun getExamples(lang: Language): List<String> {
        return listOf(
            "I am following a @diets diet",
            "I am allergic to @allergies"
        )
    }

    override fun toText(lang : Language) : String {
        return generate(lang, "[allergic to $allergies] [following the $diets diet]")
    }

    override fun toString(): String {
        return toText()
    }
}

class TellDiets : Intent() {
    var diets: ListOfDiets? = null

    override fun getExamples(lang: Language): List<String> {
        return listOf("I am following a @diets diet")
    }
}

class TellAllergies : Intent() {
    var allergies: ListOfAllergies? = null

    override fun getExamples(lang: Language): List<String> {
        return listOf("I am allergic to @allergies")
    }
}

open class RemovePersonalInformation : Intent(), TextGenerator {
    var diets: ListOfDiets? = null
    var allergies: ListOfAllergies? = null

    override fun getExamples(lang: Language): List<String> {
        return listOf(
            "I am not following a @diets diet",
            "I am not allergic to @allergies"
        )
    }

    override fun toText(lang : Language) : String {
        return generate(lang, "[not allergic to $allergies] [not following the $diets diet]")
    }

    override fun toString(): String {
        return toText()
    }
}


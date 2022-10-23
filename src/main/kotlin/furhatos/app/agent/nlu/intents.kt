package furhatos.app.agent.nlu

import furhatos.nlu.Intent
import furhatos.nlu.TextGenerator
import furhatos.util.Language

open class TellPersonalInformation : Intent(), TextGenerator {
    var diets: ListOfDiets? = null
    var allergies: ListOfAllergies? = null

    override fun getExamples(lang: Language): List<String> {
        return listOf(
            "I am following a @diets diet and I am allergic to @allergies",
            "I am allergic to @allergies and I am following a @diets diet",
            "I am on the @diets diet and my allergies are @allergies",
            "My allergies are @allergies and I am on the @diets diet"
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
        return listOf(
            "I am following a @diets diet",
            "I am following the @diets diet",
            "I am on a @diets diet",
            "I am on the @diets diet",
            "@diets"
        )
    }
}

class TellAllergies : Intent() {
    var allergies: ListOfAllergies? = null

    override fun getExamples(lang: Language): List<String> {
        return listOf(
            "I am allergic to @allergies",
            "I am intolerant to @allergies",
            "my allergy is @allergies",
            "my allergies are @allergies",
            "my intolerance is @allergies",
            "my intolerances are @allergies",
            "@allergies"
        )
    }
}

open class RemovePersonalInformation : Intent(), TextGenerator {
    var diets: ListOfDiets? = null
    var allergies: ListOfAllergies? = null

    override fun getExamples(lang: Language): List<String> {
        return listOf(
            "not following a @diets diet",
            "not on the @diets diet",
            "not allergic to @allergies",
            "not intolerant to @allergies",
            "@diets is not my diet",
            "@diets are not my diets",
            "@allergies is not my allergy",
            "@allergies are not my allergies",
        )
    }

    override fun toText(lang : Language) : String {
        return generate(lang, "[not allergic to $allergies] [not following the $diets diet]")
    }

    override fun toString(): String {
        return toText()
    }
}


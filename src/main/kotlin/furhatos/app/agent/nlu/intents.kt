package furhatos.app.agent.nlu

import furhatos.nlu.Intent
import furhatos.nlu.TextGenerator
import furhatos.util.Language

// For trying out the agent (TO BE REMOVED)
class GoToPIM : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("Go to the personal information model.")
    }
}

open class PersonalInformationIntent : Intent(), TextGenerator {
    var diets: ListOfDiets? = null
    var allergies: ListOfIntolerances? = null

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
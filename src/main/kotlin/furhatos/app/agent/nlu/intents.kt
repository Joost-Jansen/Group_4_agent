package furhatos.app.agent.nlu

import furhatos.nlu.Intent
import furhatos.nlu.common.PersonName
import furhatos.util.Language

class User_identification(
    val name : PersonName? = null
) : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("@name", "I am @name", "they call me @name", "my name is @name", "this is @name", "@name is my name", "@name, is what I am")
    }

    override fun getNegativeExamples(lang: Language?): List<String> {
        return listOf("I am not @name", "they don't call me @name", "my name is not @name", "this is not @name")
    }
}

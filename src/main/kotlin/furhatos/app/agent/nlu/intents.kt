package furhatos.app.agent.nlu

import furhatos.nlu.Intent
import furhatos.util.Language

class User_identification(val person : Person? = null) : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("person", "I am person", "My name is person")
    }
}

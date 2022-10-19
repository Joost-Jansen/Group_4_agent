package furhatos.app.agent.nlu

import furhatos.nlu.EnumEntity
import furhatos.util.Language

class Person : EnumEntity(stemming = true, speechRecPhrases = true) {
    override fun getEnum(lang: Language): List<String> {
        return listOf("Mike", "Zeger", "Jeron", "Joost", "Jochem", "Jim")
    }
}

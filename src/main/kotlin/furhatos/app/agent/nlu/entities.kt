package furhatos.app.agent.nlu

import furhatos.nlu.EnumEntity
import furhatos.util.Language
import furhatos.app.agent.resources.getCuisines
import furhatos.app.agent.resources.getDiets
import furhatos.app.agent.resources.getIntolerances
import furhatos.app.agent.resources.getMealTypes

/**
 * Entities are defined here, which can be used for the dialog and to query in Spoonacular
 **/

class Cuisine : EnumEntity(stemming = true, speechRecPhrases = true) {
    override fun getEnum(lang: Language): List<String> {
        return getCuisines()
    }
}

class Diet : EnumEntity(stemming = true, speechRecPhrases = true) {
    override fun getEnum(lang: Language): List<String> {
        return getDiets()
    }
}

class Intolerance : EnumEntity(stemming = true, speechRecPhrases = true) {
    override fun getEnum(lang: Language): List<String> {
        return getIntolerances()
    }
}

class MealType : EnumEntity(stemming = true, speechRecPhrases = true) {
    override fun getEnum(lang: Language): List<String> {
        return getMealTypes()
    }
}




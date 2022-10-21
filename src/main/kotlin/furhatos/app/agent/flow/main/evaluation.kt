package furhatos.app.agent.flow.main

import Meal
import furhatos.app.agent.current_user
import furhatos.app.agent.flow.Parent
import furhatos.app.agent.flow.recipes.FoodJoke
import furhatos.app.agent.userUpdates
import furhatos.flow.kotlin.*
import furhatos.gestures.Gestures
import furhatos.nlu.ComplexEnumEntity
import furhatos.nlu.EnumEntity
import furhatos.nlu.Intent
import furhatos.nlu.ListEntity
import furhatos.nlu.common.DontKnow
import furhatos.nlu.common.No
import furhatos.nlu.common.Yes
import furhatos.util.Language

val Evaluation : State = state(Parent) {
    onEntry {
        furhat.ask("Welcome to the evaluation module. Would you like to continue?")
    }

    onResponse<No> {
        furhat.say("Alright, then I will tell you a joke.")
        goto(FoodJoke)
    }

    onResponse<Yes> {
        furhat.say("great.")
        goto(mealEvaluation)
    }
}
var lastMeal: Meal = Meal(-1, "", -1,"", "")

val mealEvaluation : State = state(Parent){
    onEntry {
        val meal = userUpdates.findLastMeal(current_user.meals)
        if (meal != null) {
            lastMeal = meal
            furhat.ask(
                random(
                    "The last time I saw you, I recommended you to eat ${lastMeal.name} for ${lastMeal.course}. Did you like that meal?"
                    ,"Did you like  that ${lastMeal.name} for ${lastMeal.course}? I recommended it you last time."
                )
            )
        }
        else{
            goto(DayPreference)
        }
    }


    onResponse<DontKnow> {
        furhat.say("That's alright. I have that sometimes too.")
        furhat.say("We'll talk about something else then.")
        goto(Evaluation)
    }

    onResponse<No> {
        furhat.gesture(Gestures.ExpressSad)
        furhat.say(random(
            "That's unfortunate.",
            "That's too bad.")
        )

        goto(negativeMealEvaluation)
    }

    onPartialResponse<No> {
        furhat.gesture(Gestures.ExpressSad)
        raise(it, it.secondaryIntent)
    }

    onResponse<Yes> {
        furhat.gesture(Gestures.Smile)
        furhat.say(
            random(
                "It's always nice to hear that people love my recommendations.",
                "That's great to hear!"
            )
        )
        goto(positiveMealEvaluation)
    }

    onPartialResponse<Yes> {
        furhat.gesture(Gestures.Smile)
        raise(it, it.secondaryIntent)
    }

    onResponse<negativeFlavourMeal> {
        print(it.intent.flavours)
        if(it.intent.flavours != null) {
            furhat.say(
                random(
                    "Alright, I get that you find the ${lastMeal.name} ${it.intent.flavours}. I'll keep that in mind for the next time",
                    "Too bad that ${lastMeal.name} was ${it.intent.flavours}. I'll try to remember that for your next meal."
                )
            )
        }else{
            furhat.say("Too bad that you didn't like the ${lastMeal.name}. I'll try to remember that for your next meal.")
        }
        furhat.gesture(Gestures.Blink)

        // update likes of meal
        lastMeal.likes -= 2
        userUpdates.updateLikes(lastMeal, current_user.meals, lastMeal.likes)
        goto(Evaluation)
    }


    onResponse<positiveFlavourMeal> {
        if(it.intent.flavours != null){
                furhat.say("Great to hear that ${lastMeal.name} were ${it.intent.flavours}. I'll keep that in mind for the next time")

        }
//        else if(it.intent.ingredients != null){
//            furhat.say("Great to hear that ${lastMeal.name} had such nice ingredients. I'll keep that in mind for the next time")
//        }
        else{
            furhat.say("It's always nice to hear that people love my recommendations. I'll try to remember that for your next meal.")
        }

        lastMeal.likes += 2
        userUpdates.updateLikes(lastMeal, current_user.meals, lastMeal.likes)
        furhat.gesture(Gestures.Blink)
        goto(Evaluation)
    }

    onNoResponse { reentry() }
}

val positiveMealEvaluation : State = state(Evaluation){
    onEntry {
        furhat.ask(
            random(
                "What did you like about the meal?",
                "What did you like about the ${lastMeal.name}?"
            )
        )
    }

    onResponse<positiveFlavourMeal> {
        println(it.intent.flavours)
        if(it.intent.flavours == null || !it.intent.flavours!!.isEmpty ){
            furhat.say("Good. I'll try to remember that for your next meal.")
        } else{
            furhat.say("Great to hear that ${lastMeal.name} were ${it.intent.flavours}. I'll keep that in mind for the next time")
        }
        furhat.gesture(Gestures.Blink)
        lastMeal.likes += 2
        userUpdates.updateLikes(lastMeal, current_user.meals, lastMeal.likes)
        goto(Evaluation)
    }

    onResponse<DontKnow> {
        furhat.say("That's alright. I have that sometimes too.")
        furhat.say("We'll talk about something else then.")
        lastMeal.likes += 1
        userUpdates.updateLikes(lastMeal, current_user.meals, lastMeal.likes)
        goto(Evaluation)
    }

}

val negativeMealEvaluation : State = state(Evaluation){
    onEntry {
        random(
            furhat.ask("What didn't you like about the meal?"),
            furhat.ask("What didn't you like about the ${lastMeal.name}?")
        )

    }

    onResponse<negativeFlavourMeal> {
        if(it.intent.flavours != null) {
            furhat.say(
                random(
                    "Alright, I get that you find the ${lastMeal.name} ${it.intent.flavours}. I'll keep that in mind for the next time",
                    "Too bad that ${lastMeal.name} was ${it.intent.flavours}. I'll try to remember that for your next meal."
                )
            )
        }else{
            furhat.say("Too bad that you didn't like the ${lastMeal.name}. I'll try to remember that for your next meal.")
        }
        furhat.gesture(Gestures.Blink)
        lastMeal.likes -= 2
        userUpdates.updateLikes(lastMeal, current_user.meals, lastMeal.likes)
        goto(Evaluation)
    }

    onResponse<DontKnow> {
        furhat.say("That's alright. I have that sometimes too.")
        furhat.say("We'll talk about something else then.")
        lastMeal.likes -= 1
        userUpdates.updateLikes(lastMeal, current_user.meals, lastMeal.likes)
        goto(Evaluation)
    }

}
class positiveFlavourMeal(var flavours : flavourListPostive? = null) : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("I liked the taste", "I like the taste",  "@flavours", "I like the @flavours", "I loved the @flavours",
            "I think it was @flavours", "I like the @flavours flavour", "It was @flavours","What I liked was that @flavours",
            "It was @flavours", "It had a @flavours taste"
        )
    }
}

//class positiveIngredientsAndFlavours(
//    val ingredients : String? = null,
//    var flavours : flavourListNegative? = null,
//) : ComplexEnumEntity() {
//
//    override fun getEnum(lang: Language): List<String> {
//        return listOf("I liked the @ingredients","I like the @ingredients", "The @ingredients were @flavours",
//            "The @flavours of the @ingredients", "What I liked was that the @ingredients were @flavours")
//    }
//}

class negativeFlavourMeal(var flavours : flavourListNegative? = null) : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("I didn't like the taste", "@flavours", "I did not like the @flavours", "I found the @flavours not nice",
            "I think it was @flavours", "I like the @flavours flavour", "It was @flavours", "What I didn't like was that @flavours",
            "It was @flavours", "It had a @flavours taste"  )
    }
}

//class negativeIngredientsAndFlavours(
//    val ingredients : String? = null,
//    var flavours : flavourListNegative? = null,
//    ) : ComplexEnumEntity() {
//
//    override fun getEnum(lang: Language): List<String> {
//        return listOf("I didn't like the @ingredients", "The @ingredients were @flavours",
//            "The @flavours of the @ingredients", "What I  didn't like was that the @ingredients were @flavours")
//    }
//}

class flavourListPostive : ListEntity<QuantifiedFlavourPositive>()
class flavourListNegative : ListEntity<QuantifiedFlavourNegative>()

class flavour : EnumEntity(stemming = true, speechRecPhrases = true) {
    override fun getEnum(lang: Language): List<String> {
        return listOf("sweet","sweetness", "sour","sourness", "spicy", "spicyness", "chili", "salty","bitter","bitterness", "umami")
    }
}

class adjectivePositive : EnumEntity(stemming = true, speechRecPhrases = true) {
    override fun getEnum(lang: Language): List<String> {
        return listOf("very", "truly", "really")
    }
}

class adjectiveNegative : EnumEntity(stemming = true, speechRecPhrases = true) {
    override fun getEnum(lang: Language): List<String> {
        return listOf("too", "a bit", "too much", "a lot")
    }
}

class QuantifiedFlavourPositive(
    val adjective: adjectivePositive? = null,
    val flavour : flavour? = null) : ComplexEnumEntity() {

    override fun getEnum(lang: Language): List<String> {
        return listOf("@adjective @flavour", "@flavour", "@flavour and @flavour", "@adjective @flavour and @adjective @flavour", "@adjective @flavour and @flavour", "@flavour and @adjective @flavour")
    }

    override fun toText(): String {
        return generate("$adjective $flavour")
    }
}

class QuantifiedFlavourNegative(
    val adjective: adjectiveNegative? = null,
    val flavour : flavour? = null) : ComplexEnumEntity() {

    override fun getEnum(lang: Language): List<String> {
        return listOf("@adjective @flavour", "@flavour", "@flavour and @flavour", "@adjective @flavour and @adjective @flavour", "@adjective @flavour and @flavour", "@flavour and @adjective @flavour")
    }

    override fun toText(): String {
        return generate("$adjective $flavour")
    }
}

//
//class compliment : EnumEntity(stemming = true, speechRecPhrases = true) {
//    override fun getEnum(lang: Language): List<String> {
//        return listOf("nice", "like", "sweet")
//    }
//}
//class compliment(
//    val compliment : String? = null, ) : ComplexEnumEntity() {
//
//    override fun getEnum(lang: Language): List<String> {
//        return listOf("I liked the @compliment", "")
//    }
//}



//val positiveMealEvaluation : State = state(Evaluation){
//    onEntry {
//        furhat.ask("What did you like about the meal?")
//    }
//
//    onResponse<No> {
//        furhat.say("That's unfortunate.")
//    }
//}
//
//val negativeMealEvaluation : State = state(Evaluation){
//    onEntry {
//        furhat.ask("What did you not like about the meal?")
//    }
//
//    onResponse<No> {
//        furhat.say("That's unfortunate.")
//    }
//}

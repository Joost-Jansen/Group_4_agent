package furhatos.app.agent.flow.main

import Ingredient
import furhatos.app.agent.current_user
import furhatos.app.agent.flow.Parent
import furhatos.app.agent.flow.recipes.Recommendation
import furhatos.app.agent.flow.recipes.query
import furhatos.app.agent.nlu.Ingredients
import furhatos.app.agent.nlu.Preparation
import furhatos.flow.kotlin.*
import furhatos.gestures.Gestures
import furhatos.nlu.ComplexEnumEntity
import furhatos.nlu.EnumEntity
import furhatos.nlu.Intent
import furhatos.nlu.ListEntity
import furhatos.nlu.common.No
import furhatos.nlu.common.Yes
import furhatos.util.Language


//class mainCourse(val m: Ingredients? = null, val mCourse: String? = null) : Intent() {
//
//    override fun getExamples(lang: Language): List<String> {
//        return listOf(
//            "I wanted to eat @mCourse", "I am going to eat @mCourse", "I will eat @mCourse", "We will eat @mCourse",
//            "I am going to have @mCourse for dinner"
//        )
//    }
//}

//class meal() : Intent(var breakfast: niceMeal? = null) {
//
//    var lunch: niceMeal? = null
//
//    override fun getExamples(lang: Language): List<String> {
//        return listOf(
//            "today i had @breakfast", "this morning i had @breakfast", "for lunch i had @lunch", "I ate an @breakfast today",
//            "I had an @lunch for lunch", "I lunched with an @lunch", "Today I had a @breakfast and @lunch",
//            "for breakfast I ate @breakfast, and for lunch I had @lunch", "for breakfast I had @breakfast and for lunch I ate @lunch"
//        )
//    }
//}
class ListOffIngredients : ListEntity<Ingredients>()
class MealType : EnumEntity() {
    override fun getEnum(lang: Language): List<String> {
        return listOf("main course", "side dish", "dessert", "appetizer", "salad", "bread", "breakfast", "soup", "beverage",
            "sauce", "marinade", "fingerfood", "snack", "drink")
    }
}

class requestMealType(val m: MealType? = null) : Intent() {
    override fun getConfidenceThreshold(): Double? {
        return 0.5
    }
    override fun getExamples(lang: Language): List<String> {
        return listOf(
            "I want a @m", "I am looking for @m recipe", "I would like to get a @m recommendation", "give me a @m", "I need an @m",
            "Can you please give me @m recommendation", "I am hungry for a @m", "I am thirsty for a @m", "I am looking for a @m", "I would like to get a @m",
            "I would like to get @m", "I would like to have a @m", "I would like to get a @m"
        )
    }
}

class cravings(val craves : ListOffIngredients? = null) : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf(
            "I am hungry for an apple", "I want rice", "Do you have something with milk", "Something with milk would be nice", "I was hoping to eat some milk", "I would like to have pasta",
            "I am hungry for an apple and cheese", "I want rice and salt", "Do you have something with milk and a pepper", "Something with milk or yogurt would be nice", "I was hoping to eat some milk with salt",
            "I am hungry for an apple banana, and pear", "I want rice salt mushrooms and pepper", "Do you have something with milk cheese or yogurt", "Something with milk flower or salt would be nice", "I was hoping to eat some milk salt pepper",
        )
    }
}

class leftOvers(val ingreds : ListOffIngredients? = null) : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf(
            "I still have rice", "In my fridge I have milk", "rice", "I have rice and milk", "rice and milk", " i do have rice, milk, chicken and bananas",
            "I have cheese milk bananas", "I have some milk left over"
        )
    }
}


val DayPreference : State = state(Parent) {
    onEntry {
        furhat.ask(
            random(
                "For what kind of course would you like to get a recipe?",
                "What kind of meal are you hungry for?",
                "What kind of recipe are you interested in?"
            )
        )
        furhat.gesture(Gestures.Smile)
    }

    onNoResponse {reentry() }


    onResponse<requestMealType> {
        if (!it.intent.isEmpty) {
            val course = it.intent.m.toString()
            furhat.gesture(Gestures.Blink)
            furhat.say("Okay, but before I give you an ${course} recommendation I would like to have a bit more information")

            goto(askLeftOver)
        }
    }
}
//            val mainInquire = listOf("side dish", "dessert", "appetizer", "sauce", "marinade")
//            val lunchQ = listOf("main course", "salad", "bread", "soup", "fingerfood", "snack")
//            if (mainInquire.contains(course)) {
//                goto(askLeftOver)
//            }
//            if (lunchQ.contains(course)) {
//                random(
//                    furhat.ask("What is the last thing you have eaten"),
//                    furhat.ask("What did you have for you last meal")
//                )
//            }

//val askMain : State = state {
//    onEntry {
//        furhat.ask(
//            random(
//                "What are you planning to have for main course",
//                "What is the main course you want to eat with it"
//            )
//        )
//    }
//    onResponse<mainCourse> {
//        print(it.intent.mCourse)
//        Thread.sleep(10000)
//        if(it.intent.mCourse != null) {
//            val result = query(it.intent.mCourse.toString(),"complexSearch", "recipes")
//            print(result.javaClass.name)
//        }
//    }
//}

val askAppitite : State = state(Parent) {
    onEntry {
        furhat.ask(
            random(
                "At last I would like to know if you already had some cravings?",
                "Are there some ingredients you would like to see in the meal?",
                "Did you perhaps have your mind on something specific?"
            )
        )
        furhat.gesture(Gestures.Roll(duration = 0.7))
    }

    onResponse<cravings> {
        if(it.intent.craves != null) {
            val x = it.intent.craves as ListEntity<Ingredients>
            val left = x.list as ArrayList<Ingredients>
            val prefs = current_user.preferences
            for(i in left) {
                val c = i.toString()

                if (!prefs.contains(c)) {
                    prefs.add(c)
                }
            }
            print(current_user.preferences)
        }
        furhat.gesture(Gestures.Smile)
        furhat.say(
            random(
                "That does sound good, lets see what we could do with that",
                "I think I have enough information to find you a suitable recipe"
            )
        )
        goto(Recommendation)
    }

    onPartialResponse<Yes> {
        furhat.gesture(Gestures.Smile)
        raise(it, it.secondaryIntent)
    }



    onResponse<Yes> {
        furhat.ask(
            random(
                "Please tell me and I will take it into account",
                "Okay, what is it that you crave"
            )
        )
        goto(Recommendation)
    }

    onResponse<No> {
        furhat.say(
            random(
                "No problem, I will find something you like",
                "In that case I will go find you something"
            )
        )
        furhat.gesture(Gestures.BrowRaise(strength = 0.3))
        goto(Recommendation)
    }

}

val askLeftOver : State = state(Parent) {
    onEntry {
        furhat.ask(
            random(
                "Do you still have some left over ingredients",
                "Are there some left over ingredients you want to use"
            )
        )
    }

    onPartialResponse<Yes> {
        furhat.gesture(Gestures.Smile)
        raise(it, it.secondaryIntent)
    }

    onResponse<leftOvers> {
        if(it.intent.ingreds != null) {
            val x = it.intent.ingreds as ListEntity<Ingredients>
            val left = x.list as ArrayList<Ingredients>
            val converted = ArrayList<Ingredient>()
            for(i in left) {
                converted.add(Ingredient(i.toString(), 0))
            }
            val right = current_user.left_overs as ArrayList<Ingredient>
            right.addAll(converted)
            furhat.gesture(Gestures.Smile)
            furhat.say(
                random(
                    "Nice, I will keep it in mind when finding a recipe",
                    "Thanks, That will help narrowing the search space"
                )
            )
            goto(askAppitite)
        }
    }

    onResponse<Yes> {
        furhat.say(
            random(
                "What are those ingredients",
                "Can you tell me what those ingredients are"
            )
        )
    }

    onResponse<No> {
        furhat.say(
            random(
                "Okay, that means you can cook with some nice fresh ingredients",
                "You have been very effective with your groceries I see"
            )
        )
        goto(askAppitite)
    }
}



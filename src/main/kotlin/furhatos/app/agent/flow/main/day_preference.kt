package furhatos.app.agent.flow.main

import furhatos.app.agent.flow.memory.data.Meal
import furhatos.app.agent.flow.memory.data.Ingredient
import furhatos.app.agent.current_user
import furhatos.app.agent.flow.Parent
import furhatos.app.agent.flow.recipes.Recommendation
import furhatos.app.agent.nlu.Ingredients
import furhatos.app.agent.nlu.MealT
import furhatos.app.agent.nlu.Preparation
import furhatos.flow.kotlin.*
import furhatos.gestures.Gestures
import furhatos.nlu.EnumEntity
import furhatos.nlu.Intent
import furhatos.nlu.ListEntity
import furhatos.nlu.common.No
import furhatos.nlu.common.Number
import furhatos.nlu.common.Yes
import furhatos.util.Language
import java.time.LocalDate


class ListOffIngredients : ListEntity<Ingredients>()
class MealType : EnumEntity() {
    override fun getEnum(lang: Language): List<String> {
        return listOf("main course", "side dish", "dessert", "appetizer", "salad", "bread", "breakfast", "soup", "beverage",
            "sauce", "marinade", "fingerfood", "snack", "drink")
    }
}

class requestMealType(val m: MealT? = null) : Intent() {
    override fun getConfidenceThreshold(): Double? {
        return 0.5
    }
    override fun getExamples(lang: Language): List<String> {
        return listOf(
            "I want -a @m -recipe", "I am looking for @m -recipe", "I would like to get -a @m -recommendation", "give me -a @m", "I need -an @m",
            "Can you -please give me @m -recommendation", "I am hungry for -a @m", "I am thirsty for -a @m", "I am looking for a @m", "I would like to get a @m",
            "I would like to get @m", "I would like to have a @m", "I would like to get a @m", "-A @m",
            "I would like to get @m", "I would like to have a @m", "I would like to get a @m", "-A @m", "I want @m", "@m"
        )
    }
}

class cookingTime() : Intent() {
    val n: Number? = Number(1)
    override fun getExamples(lang: Language): List<String> {
        return listOf(
            "I have @n minutes", "I -only have @n minutes", "I need @n", "I can cook for @n minutes", "I will cook for @n minutes", "I can only cook for @n minutes", "Under @n minutes", "Under @n minutes would be fine"
        )
    }
}

class cookingTimeLong() : Intent() {
    val n: Number? = Number(1)
    override fun getExamples(lang: Language): List<String> {
        return listOf(
            "I have @n hours", "I -only have @n hours", "@n hours", "I will cook for @n hours", "I can cook for @n hours", "I want to cook for @n hours", "Under @n hours", "Under @n minutes would be fine"
        )
    }
}

class cravings(val craves : ListOffIngredients? = null) : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf(
            "I am hungry for -an apple", "I want rice", "Do you have -something with milk", "Something with milk -would -be -nice", "I was hoping to eat some milk", "I would like to have pasta",
            "I am hungry -for -an apple and cheese", "I want rice and salt", "-Do you -have something with milk and a pepper", "Something with milk or yogurt -would -be -nice", "I was -hoping to eat -some milk with salt",
            "I am hungry -for -an apple banana, and pear", "I want rice salt mushrooms -and pepper", "Do -you have -something with milk cheese or yogurt", "Something with milk flower -or salt -would -be -nice", "I -was -hoping -to eat some milk salt pepper",
            "rice", "rice salt pasta", "salt and rice"
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
                "For which course would you like to get a recipe?",
                "What course are you hungry for?",
                "What kind of recipe are you interested in?"
            )
        )
    }

    onNoResponse {reentry() }

    onResponse<requestMealType> {
        if (!it.intent.isEmpty) {
            val course = it.intent.m
            if(course != null) {
//                print(course.meal)
            }
            furhat.say("Okay, but before I give you an ${course} recommendation I would like to have a bit more information")

            goto(askLeftOver)
        }
    }
}

val askAppitite : State = state(Parent) {
    onEntry {
        furhat.ask(
            random(
                "At last I would like to know if you already had some cravings for some specific ingredients?",
                "Are there some ingredients you would like to see in the meal?",
                "Did you perhaps have your mind on some ingredients?"
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

val askTime : State = state(Parent) {
    onEntry {
        val date = LocalDate.now().dayOfWeek.toString()

        if(date == "SUNDAY" || date == "SATURDAY") {
           furhat.ask (
               random(
                   "Since it is weekend I was wondering if you would like to make a elaborate meal?",
                   "It is weekend, would you like to challenge yourself with a bigger meal?"
               )
           )
        } else {
            furhat.ask(
                random(
                    "Since it is a weekday would you prefer easy to make meal?",
                    "Would you prefer a meal that you can quickly prepare?"
                )
            )
        }
    }

    onResponse<Yes> {
        furhat.say("Okay")
        val date = LocalDate.now().dayOfWeek.toString()

        if(date == "SUNDAY" || date == "SATURDAY") {
            goto(askLong)
        } else {
            goto(askShort)
        }
    }

    onResponse<No> {
        furhat.say("Okay")
        val date = LocalDate.now().dayOfWeek.toString()

        if(date == ("SUNDAY") || date == "SATURDAY") {
            goto(askShort)
        } else {
            goto(askLong)
        }
    }
}

val askShort : State = state(askTime) {
    onEntry {
        furhat.ask(
            random(
                "What is the maximum amount of minutes you would want to spend cooking?",
                "How many minutes can you spare for making a meal"
            )
        )
    }
    onResponse<cookingTime> {
        if(it.intent.n != null) {
            val x = it.intent.n
            print(x.toString().toInt())
            goto(askAppitite)
        }
    }
}

val askLong : State = state(askTime) {
    onEntry {
        furhat.ask(
            random(
                "What is the maximum amount of hours you would want to spend cooking?",
                "For how much hours would you like to cook?"
            )
        )
    }
    onResponse<cookingTimeLong> {
        if(it.intent.n != null) {
            val x = it.intent.n
            print(x.toString().toInt() * 60)
            goto(askAppitite)
        }
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
            goto(askTime)
        }
    }

    onResponse<Yes> {
        furhat.ask(
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
        goto(askTime)
    }
}



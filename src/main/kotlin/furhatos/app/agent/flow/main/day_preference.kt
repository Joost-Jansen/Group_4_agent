package furhatos.app.agent.flow.main

import furhatos.app.agent.flow.memory.data.Ingredient
import furhatos.app.agent.current_user
import furhatos.app.agent.flow.Parent
import furhatos.app.agent.flow.recipes.Recommendation
import furhatos.app.agent.nlu.Cuisine
import furhatos.app.agent.nlu.Ingredients
import furhatos.app.agent.nlu.MealT
import furhatos.flow.kotlin.*
import furhatos.gestures.Gestures
import furhatos.nlu.Intent
import furhatos.nlu.ListEntity
import furhatos.nlu.common.No
import furhatos.nlu.common.Number
import furhatos.nlu.common.Yes
import furhatos.util.Language
import java.time.LocalDate
import kotlin.math.max


class ListOffIngredients : ListEntity<Ingredients>()

class requestMealType(val m: MealT? = null) : Intent() {
    override fun getConfidenceThreshold(): Double {
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
    val n: Number = Number(1)
    override fun getExamples(lang: Language): List<String> {
        return listOf(
            "I have @n minutes", "I -only have @n minutes", "I need @n", "I can cook for @n minutes", "I will cook for @n minutes", "I can only cook for @n minutes", "Under @n minutes", "Under @n minutes would be fine", "@n"
        )
    }
}

class cookingTimeLong() : Intent() {
    val n: Number = Number(1)
    override fun getExamples(lang: Language): List<String> {
        return listOf(
            "I have @n hours", "I -only have @n hours", "@n hours", "I will cook for @n hours", "I can cook for @n hours", "I want to cook for @n hours", "Under @n hours", "Under @n minutes would be fine", "@n"
        )
    }
}

class respondCuisine(val cuisin : Cuisine? = null) : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf(
            "I would like something @cuisin", "I would like something from @cuisin", "I want @cuisin food", "I need @cuisin", "@cuisin food", "@cuisin", "I would like to eat @cuisin", "I want to eat some @cuisin food", "I was hoping to eat something @cuisin",
            "I am hungry for @cuisin", "I am hungry for @cuisin food", "I want @cuisin", "I want @cuisin food", "Do you have some @cuisin recipes", "Do you have some @cuisin food", "Some @cuisin food", " Something @cuisin", " Some @cuisin",
            "I would like to have @cuisin", "I would like something from the @cuisin cuisine" , "I would like a @cuisin meal", "I want to eat some @cuisin food", "I was hoping to eat something from @cuisin",
            "I want an @cuisin meal", "Do you have some @cuisin recipes", "Do you have some @cuisin recipes", "Some @cuisin meal", " Something from @cuisin",
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
        current_user.last_step = "day_preference"
        furhat.ask(
            random(
                "For which course of the day would you like to get a recipe?",
                "What course are you hungry for?",
                "What kind of recipe are you interested in?"
            )
        )
        furhat.gesture(Gestures.Smile)
    }

    onResponse<requestMealType> {
        print(current_user.preferred_meal_type)
        if (!it.intent.isEmpty) {
            val course = it.intent.m!!.meal.toString()
            current_user.preferred_meal_type = course
            furhat.gesture(Gestures.Blink)
            furhat.say("Okay, but before I give you a $course recommendation I would like to have a bit more information")
            print(current_user.preferred_meal_type)
            goto(AskLeftOver)
        }
    }
}

val AskAppitite : State = state(Parent) {
    onEntry {
        current_user.last_step = "ask_appitite"
        furhat.ask(
            random(
                "Are you hungry for a certain cuisine today?",
                "At last I wanted to know if there maybe was a cuisine are you hungry for?",
                "Did you have some food in mind from a certain region or cuisine?"
            )
        )
        furhat.gesture(Gestures.Smile)
    }

    onResponse<respondCuisine> {
        print(current_user.prefered_cuisine)
        var cuisine: String? = null
        if(it.intent.cuisin != null) {
            cuisine = it.intent.cuisin!!.cuis.toString()
            current_user.prefered_cuisine = cuisine
        }
        if(cuisine != null) {
            for(kitchen in current_user.cuisines) {
                if (kitchen.name == cuisine) {
                    kitchen.likes = max(10, kitchen.likes + 1)
                }
            }
        }
        print(current_user.prefered_cuisine)
        furhat.gesture(Gestures.Smile)
        furhat.say(
            random(
                "That does sound good, lets see what we could do with that",
                "Good idea, ${it.intent} I will take it into account when finding you a recipe",
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
                "Okay, what cuisine did you have in mind?"
            )
        )
        goto(Recommendation)
    }

    onResponse<No> {
        furhat.say(
            random(
                "No problem, I will find something you like",
                "In that case I will go find you something",
                "I see that you are keeping your options open, that makes it more interesting for me"
            )
        )
        furhat.gesture(Gestures.BrowRaise(strength = 0.3))
        goto(Recommendation)
    }
}

val AskTime : State = state(Parent) {
    onEntry {
        current_user.last_step = "ask_time"
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
                    "Since it is a weekday would you prefer a meal that is easy to make?",
                    "Would you prefer a meal that you can quickly prepare?"
                )
            )
        }
    }

    onResponse<Yes> {
        furhat.say("Okay")
        val date = LocalDate.now().dayOfWeek.toString()

        if(date == "SUNDAY" || date == "SATURDAY") {
            goto(AskLong)
        } else {
            goto(AskShort)
        }
    }

    onResponse<No> {
        furhat.say("Okay")
        val date = LocalDate.now().dayOfWeek.toString()

        if(date == ("SUNDAY") || date == "SATURDAY") {
            goto(AskShort)
        } else {
            goto(AskLong)
        }
    }
}

val AskShort : State = state(AskTime) {
    onEntry {
        current_user.last_step = "ask_short"
        furhat.ask(
            random(
                "What is the maximum amount of minutes you would want to spend cooking?",
                "How many minutes can you spare for making a meal"
            )
        )
    }
    onResponse<cookingTime> {
        print(current_user.time)
        if(it.intent.n != null) {
            val timeToCook = it.intent.n.toString().toInt()
            current_user.time = timeToCook
            print(current_user.time)
            goto(AskAppitite)
        }
    }
}

val AskLong : State = state(AskTime) {
    onEntry {
        current_user.last_step = "ask_long"
        furhat.ask(
            random(
                "What is the maximum amount of hours you would want to spend cooking?",
                "For how much hours would you like to cook?"
            )
        )
    }
    onResponse<cookingTimeLong> {
        print(current_user.time)
        if(it.intent.n != null) {
            val timeToCook = it.intent.n.toString().toInt() * 60
            current_user.time = timeToCook
            print(current_user.time)
            goto(AskAppitite)
        }
    }
}

val AskLeftOver : State = state(Parent) {
    onEntry {
        current_user.last_step = "ask_leftover"
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
        print(current_user.left_overs)
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
            print(current_user.left_overs)
            goto(AskTime)
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
        goto(AskTime)
    }
}



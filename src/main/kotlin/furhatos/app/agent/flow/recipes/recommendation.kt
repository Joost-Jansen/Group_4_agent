package furhatos.app.agent.flow.recipes

import Meal
import furhatos.app.agent.flow.Parent
import furhatos.app.agent.flow.main.Idle
import furhatos.flow.kotlin.State
import furhatos.flow.kotlin.furhat
import furhatos.flow.kotlin.onResponse
import furhatos.flow.kotlin.state
import furhatos.nlu.common.No
import furhatos.nlu.common.Yes

val BASE_URL = "https://api.spoonacular.com" // Spoonacular API url
val API_KEY = "e9eeb0d76f024efcaf7cd32ae444c899" // Key to free account
val TIMEOUT = 5000 // 5 seconds
var recommendations: MutableList<Meal> = mutableListOf()

// Start state containing everything except the query to the API
val Recommendation : State = state(Parent) {
    onEntry {
        recommendations = call(query("recipes", "search")) as MutableList<Meal>

        if (recommendations.isEmpty()) {
            furhat.say("Sorry, I could not find a recipe.")
            goto(Idle)
        } else {
            furhat.ask("I have some ideas on which recipes you might like. Would you like to know?")
        }
    }

    onResponse<Yes> {
        goto(GiveRecommendation)
    }

    onResponse<No> {
        furhat.say("Alright, I'll be available if you need me.")
        goto(Idle)
    }
}

val GiveRecommendation = state(Parent) {
    onEntry {
        if (recommendations.isNotEmpty()) {
            val recipe = recommendations.first()
            recommendations = recommendations.drop(1) as MutableList<Meal>

            // Propose recipe and explain
            furhat.say("I think you might like "+ recipe.name)
            furhat.say("It takes " + recipe.prepTime + " minutes to cook.")
            furhat.say("The instructions are available through this link: " + recipe.link)

            goto(evaluateRecommendation(recipe))
        } else {
            furhat.say("Unfortunately, I'm out of recommended recipes.")
            goto(Idle)
        }
    }
}

fun evaluateRecommendation(recipe: Meal) : State = state(Parent) {
    onEntry {
        val like = furhat.askYN("Do you like it?")

        if (like!! && like) {
            furhat.say("Awesome!")
            goto(EndRecommendation)
        } else {
            val another = furhat.askYN("Too bad, would you like another recipe?")

            if (another!! && another) {
                furhat.say("Okay, let me see.")
            } else {
                furhat.say("Okay, I'll be here if you need me.")
                goto(Idle)
            }
        }
    }
}

val EndRecommendation : State = state(Parent) {
    onEntry {
        furhat.say("Enjoy your meal!")
        goto(Idle)
    }
}
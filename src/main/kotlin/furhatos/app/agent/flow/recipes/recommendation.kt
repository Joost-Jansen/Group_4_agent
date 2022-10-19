package furhatos.app.agent.flow.recipes

import furhatos.app.agent.flow.Parent
import furhatos.app.agent.flow.main.Evaluation
import furhatos.app.agent.flow.main.Idle
import furhatos.nlu.common.*
import furhatos.nlu.common.Number as NumberNLU
import furhatos.flow.kotlin.*

val BASE_URL = "https://api.spoonacular.com" // Spoonacular API url
val API_KEY = "e9eeb0d76f024efcaf7cd32ae444c899" // Key to free account
val TIMEOUT = 5000 // 4 seconds

// Start state containing everything except the query to the API
val Recommendation : State = state(Parent) {
    onEntry {
        furhat.ask("Welcome at the recommendation module. Would you like a random recommendation?")
    }

    onResponse<Yes>{
        furhat.say("Going to random recommendation.")
        goto(RandomRecommendation)
    }

    onResponse<No>{
        furhat.say("Going to search based recommendation.")
        goto(SearchBasedRecommendation)
    }

    onResponse {
        furhat.say("Going to evaluation module.")
        goto(Evaluation)
    }

}

val RandomRecommendation : State = state(Parent) {
    onEntry {
        furhat.ask("Would you like a new inspiration for a recipe?")
    }

    // Ask how many recipes the user wants to suggest
    onResponse<Yes>{
        furhat.ask("Okay! How many recipes would you like to see?")
    }

    // If user does not want random one, recommend search based
    onResponse<No>{
        furhat.say("Okay, lets go to search based recommendation.")
        goto(SearchBasedRecommendation)
    }

    onResponse<NumberNLU>{
        goto(randomRecipes(it.text))
    }
}

fun randomRecipes(input : String) = state(Parent) {
    onEntry {
        // Get responses
        val results = call(query(input, "random", "recipes")) as List<String>

        if (results.isEmpty()) {
            furhat.say("I could connect to my brain or did not find anything")
            goto(Idle)
        } else {
            // Tell the response
            furhat.say("I have found some recipes!")
            for (recipe in results) furhat.say(recipe)

            // Here the user can choose which recipe he likes
            val recipe = call(AcceptFromList) as String
            if (recipe.isEmpty()) reentry()
            else {
                furhat.say("You have chosen the recipe: " + recipe)
                goto(Idle)
            }
        }
    }
}

val SearchBasedRecommendation : State = state(Parent) {
    onEntry {
        furhat.ask("Would you like to search for a recipe?")
    }

    // Ask how many recipes the user wants to suggest
    onResponse<Yes>{
        furhat.ask("Perfect! What would you like to eat?")
    }

    // If user does not want random one, recommend search based
    onResponse<No>{
        furhat.say("Okay, lets go to random recommendation.")
        goto(RandomRecommendation)
    }

    onResponse {
        call(searchRecipes(it.text))
    }
}

fun searchRecipes(input : String) : State = state(Parent) {
    onEntry {
        // Get responses
        val results = call(query(input, "search", "recipes")) as List<String>

        if (results.isEmpty()) {
            furhat.say("I could connect to my brain or did not find anything")
            goto(Idle)
        } else {
            // Tell the response
            furhat.say("I have found some recipes!")
            for (recipe in results) furhat.say(recipe)

            // Here the user can choose which recipe he likes
            val recipe = call(AcceptFromList) as String
            if (recipe.isEmpty()) reentry()
            else {
                furhat.say("You have chosen the recipe: " + recipe)
                goto(Idle)
            }
        }
    }
}
package furhatos.app.agent.flow.recipes

import furhatos.app.agent.flow.Parent
import furhatos.app.agent.flow.main.Idle
import furhatos.nlu.common.*
import furhatos.nlu.common.Number as NumberNLU
import furhatos.flow.kotlin.*
import kotlin.random.Random

val BASE_URL = "https://api.spoonacular.com" // Spoonacular API url
val API_KEY = "e9eeb0d76f024efcaf7cd32ae444c899" // Key to free account
val TIMEOUT = 5000 // 4 seconds

// Start state containing everything except the query to the API
fun Recommendation(states: List<State>) : State = state(Parent) {
    onEntry {
        furhat.ask("Hi there! Would you like to eat something?")
    }

    onResponse<Yes>{
        // All recommendation states available
        val allStates = listOf(RandomRecommendation, SearchBasedRecommendation)

        if (states.isEmpty()) {
            // If no states available to visit, get random one from total
            val rand = Random.nextInt(0, allStates.size)
            goto(allStates[rand])
        } else {
            // Get state from non-visited states
            val rand = Random.nextInt(0, states.size)
            goto(states[rand])
        }
    }

    onResponse<No>{
        furhat.say("Okay, no problem.")
        goto(FoodJoke)
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
        furhat.say("Okay, no worries.")
        goto(Recommendation(listOf(SearchBasedRecommendation)))
    }

    onResponse<NumberNLU>{
        goto(RandomRecipes(it.text))
    }
}

fun RandomRecipes(input : String) = state(Parent) {
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
        furhat.say("Okay, no worries.")
        goto(Recommendation(listOf(RandomRecommendation)))
    }

    onResponse {
        call(SearchRecipes(it.text))
    }
}
fun SearchRecipes(input : String) : State = state(Parent) {
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
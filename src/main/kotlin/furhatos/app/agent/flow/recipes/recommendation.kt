package furhatos.app.agent.flow.recipes

import furhatos.app.agent.flow.Parent
import furhatos.app.agent.flow.main.Idle
import furhatos.nlu.common.*
import furhatos.nlu.common.Number as NumberNLU
import furhatos.flow.kotlin.*
import furhatos.gestures.Gestures
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
        val results = call(query(input, "random")) as List<String>

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
        val results = call(query(input, "search")) as List<String>

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

val AcceptFromList: State = state(Parent) {
    onEntry {
        furhat.ask("Do you like any of them?")
    }

    // If the user likes any of them
    onResponse<Yes>{
        furhat.ask("Which one do you like?")
    }

    // If not, ask if the user wants new ones
    onResponse<No>{
        furhat.say("I will search for new ones.")
        terminate("")
    }

    // If not
    onResponse{
        // TODO recognize which one the user likes
        terminate(it.text)
    }
}

// Query to the API
fun query(user_input: String, query_type: String) = state {
    onEntry {
        // While waiting for API
        furhat.say(async = true) {
            +"Let's see"
            +Gestures.GazeAway
        }

        val base_query = "$BASE_URL/recipes/$query_type?apiKey=${API_KEY}"
        var result = listOf<String>()
        when (query_type) {
            "random" -> {
                val query = base_query.plus("&number=${user_input}")

                // Get the title of the dish from the response
                val objects = khttp.get(query).jsonObject.getJSONArray("recipes")
                for (i in 0 until user_input.toInt()) {
                    result += (objects.getJSONObject(i).getString("title"))
                }

                terminate(result)
            }
            "search"-> {
                // Parse the query
                val question = user_input.replace("+", " plus ").replace(" ", "+")
                val query = base_query.plus("&query=${question}")

                // Get the title of the dish from the response
                val objects = khttp.get(query).jsonObject.getJSONArray("results")
                for (i in 0 until 5) {
                    result += (objects.getJSONObject(i).getString("title"))
                }

            }
            else -> {
                print("Nothing here yet")
                // TODO what to do when query_type not defined
            }
        }

        // Return the response
        terminate(result)
    }

    onTime(TIMEOUT) {
        // If timeout is reached, we return nothing
        terminate(emptyList<String>())
    }
}

val FoodJoke : State = state(Parent) {
    onEntry {
        furhat.ask("Would you like to hear a joke?")
    }
    // TODO foodjoke connect to API
}
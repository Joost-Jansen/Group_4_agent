package furhatos.app.agent.flow.main

import furhatos.app.agent.flow.Parent
import furhatos.nlu.common.*
import furhatos.flow.kotlin.*
import furhatos.gestures.Gestures

val BASE_URL = "https://api.spoonacular.com/recipes/search" // Spoonacular API url
val API_KEY = "e9eeb0d76f024efcaf7cd32ae444c899" // Key to free account
val TIMEOUT = 4000 // 4 seconds

// Start state containing everything except the query to the API
val start : State = state(Parent) {

    onEntry {
        furhat.ask("Hi there! Would you like to eat something?")
    }

    onResponse<Yes>{
        furhat.ask("What kind of recipe do you prefer?")
    }

    onResponse<No>{
        furhat.say("Okay, no worries")
        goto(Idle)
    }

    onResponse {
        // While waiting for API
        furhat.say(async = true) {
            +"Let's see"
            +Gestures.GazeAway
        }

        // Retrieve API response
        val response = call(query(it.text)) as String
        // Tell the response
        furhat.say(response)

        furhat.ask("Anything else?")
    }
}

// Query to the API
fun query(str: String) = state {

    onEntry {
        // Parse the query
        val question = str.replace("+", " plus ").replace(" ", "+")
        val query = "$BASE_URL?apiKey=${API_KEY}&query=${question}"

        // Get the title of the dish from the response
        val response = call {
            khttp.get(query).jsonObject.getJSONArray("results").getJSONObject(0).getString("title")
        } as String

        // Return the response
        terminate(response)
    }

    onTime(TIMEOUT) {
        // If timeout is reached, we return nothing
        terminate("I'm having issues connecting to my brain. Try again later!")
    }
}
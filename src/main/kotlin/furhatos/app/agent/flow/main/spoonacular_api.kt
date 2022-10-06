package furhatos.app.agent.flow.main

import furhatos.app.agent.flow.Parent
import furhatos.nlu.common.*
import furhatos.flow.kotlin.*
import furhatos.gestures.Gestures
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

val BASE_URL = "https://api.spoonacular.com/recipes/search" // Endpoint for Wolfram Alpha's API with answers tailored for spoken interactions
val API_KEY = "e9eeb0d76f024efcaf7cd32ae444c899" // Test account, feel free to use it for testing.
val FAILED_RESPONSES = listOf("No spoken result available", "Wolfram Alpha did not understand your input")
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
        // Filler speech and gesture
        furhat.say(async = true) {
            +"Let's see"
            +Gestures.GazeAway
        }

        // Query done in query state below, with its result saved here since we're doing a call
        val response = call(query(it.text)) as String

        furhat.say(response)

        furhat.ask("Anything else?")
    }
}

// State to conduct the query to the API
fun query(question: String) = state {
    onEntry {
        val question = question.replace("+", " plus ").replace(" ", "+")
        val query = "$BASE_URL?apiKey=${API_KEY}&query=${question}"

        /* Call to WolframAlpha API made in an anynomous substate (https://docs.furhat.io/flow/#calling-anonymous-states)
         to allow our timeout below to stop the call if it takes to long. Note that you explicitly has to cast the result to a String.
          */
        val response = call {
            khttp.get(query).jsonObject.getJSONArray("results").getJSONObject(0).getString("title") //jsonObject.getString('Title')
        } as String


//        // Reply to user depending on the returned response
//        val reply = when {
//            FAILED_RESPONSES.contains(response) -> {
//                println("No answer to question: $question")
//                "Sorry bro, can't answer that"
//            }
//            else -> response
//        }
        // Return the response
        terminate(response)
    }

    onTime(TIMEOUT) {
        println("Issues connecting to Wolfram alpha")
        // If timeout is reached, we return nothing
        terminate("I'm having issues connecting to my brain. Try again later!")
    }
}
package furhatos.app.agent.flow.recipes

import furhatos.flow.kotlin.furhat
import furhatos.flow.kotlin.state
import furhatos.gestures.Gestures
import khttp.get

// Query to the API
fun query(user_input: String, query_type: String, query_field: String) = state {
    onEntry {
        // While waiting for API
        furhat.say(async = true) {
            +"Let's see"
            +Gestures.GazeAway
        }

        val base_query = "$BASE_URL/$query_field/$query_type?apiKey=${API_KEY}"
        var result = listOf<String>()
        when (query_type) {
            "random" -> {
                val query = base_query.plus("&number=${user_input}")

                // Get the title of the dish from the response
                val objects = get(query).jsonObject.getJSONArray("recipes")
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
                val objects = get(query).jsonObject.getJSONArray("results")
                for (i in 0 until 5) {
                    result += (objects.getJSONObject(i).getString("title"))
                }
            }
            "jokes/random"-> {
                // Get the title of the dish from the response
                result += get(base_query).jsonObject.getString("text")
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
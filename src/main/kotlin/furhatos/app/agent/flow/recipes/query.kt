package furhatos.app.agent.flow.recipes

import Meal
import furhatos.flow.kotlin.furhat
import furhatos.flow.kotlin.state
import furhatos.gestures.Gestures
import khttp.get
import khttp.post
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.time.LocalDate

// Query to the API
fun query(user_input: String, query_type: String, query_field: String) = state {
    onEntry {
        // While waiting for API
        furhat.say(async = true) {
            +"Let's see"
            +Gestures.GazeAway
        }

        val base_query = "$BASE_URL/$query_field/$query_type?apiKey=${API_KEY}"
        val result = mutableListOf<Meal>()
        when (query_type) {
            "random" -> {
                val query = base_query.plus("&number=${user_input}")

                // Get the title of the dish from the response
                val objects = get(query).jsonObject.getJSONArray("recipes")
                println(objects)
                for (i in 0 until user_input.toInt()) {
                    val meal = JSONObjectToMeal(objects.getJSONObject(i))
                    result += meal

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
                    val meal = JSONObjectToMeal(objects.getJSONObject(i))
                    result += meal
                }
            }
            "jokes/random"-> {
                // Get the title of the dish from the response
//                result += get(base_query).jsonObject.getString("text")
            }
            "complexSearch" -> {
                //search recipe based on title
                val question = user_input.replace("+", " plus ").replace(" ", "+")
                val query = base_query.plus("&titleMatch=${question}")

                val objects = get(query).jsonObject.getJSONArray("results")
                if(objects.length() != 0) {
                    val meal = JSONObjectToMeal(objects.getJSONObject(0))
                    result += meal
                }

//                https://api.spoonacular.com/recipes/complexSearch?apiKey=e9eeb0d76f024efcaf7cd32ae444c899&titleMatch=pasta
//                https://api.spoonacular.com/recipe/complexSearch?apiKey=e9eeb0d76f024efcaf7cd32ae444c899&titleMatch=pasta
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

fun getMeal(t: String) {
    val q = "${BASE_URL}/food/detect?apiKey=${API_KEY}&cheese"

    URLEncoder.encode("I like tomatoes")
    val x = post(q, data="i like tom")
    print(x.text)
    print(x.raw)
}

fun JSONObjectToMeal(recipe: JSONObject): Meal {
    val recipeId = recipe.getInt("id")
    val name = recipe.get("title") as String
    val ingredients = mutableListOf<String>()
    for (i in recipe.getJSONArray("extendedIngredients")) {
        val json: JSONObject = i as JSONObject
        ingredients.add(json.get("nameClean").toString())
    }
    val dishTypes = recipe.get("dishTypes") as JSONArray
    val dishType = dishTypes.get(0).toString()
    val likes = 0
    val lastSelected = LocalDate.now().toString()

    val meal = Meal(recipeId, name, ingredients, dishType, likes, lastSelected)
    return meal
}

fun queryRecipe(recipe_id: Int): Meal {
    val query = "$BASE_URL/recipes/$recipe_id/information?apiKey=${API_KEY}&includeNutrion=false"
    val recipe = get(query).jsonObject
    val name = recipe.get("title") as String
    val ingredients = mutableListOf<String>()
    for ( i in recipe.getJSONArray("extendedIngredients")){
        val json : JSONObject = i as JSONObject
        println(json)
        ingredients.add(json.get("nameClean").toString())
    }
    val dishTypes = recipe.get("dishTypes") as JSONArray
    val dishType = dishTypes.get(0).toString()
    val likes = 0
    val lastSelected = LocalDate.now().toString()

    val meal = Meal(recipe_id, name, ingredients, dishType, likes, lastSelected)
    return meal
}

/**
 * Input string
 * Returns: JSONArray with
 * label_0: NEGATIVE
 * label_1: NEUTRAL
 * label_2: POSTIVE
 */
fun queryHuggingFace(text: String): JSONArray {
    val API_URL = "https://api-inference.huggingface.co/models/cardiffnlp/twitter-roberta-base-sentiment"
    val headers = mapOf("Authorization" to "Bearer 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")
    val output = mapOf("inputs" to  text)
    val query = get(API_URL, headers, json=output).jsonArray.getJSONArray(0)
    print(query)
    return query
    // Get the title of the dish from the response
//    val objects = get(query).jsonObject.getJSONArray("recipes")
//    val response = requests.post(API_URL, headers=headers, json=payload)
//    return response.json()

}

//val id: Int, // id in spoonacular can request by getID
//val name: String, // name of meal
//var likes: Int, // amount of likes or dislikes (when negative)
//var last_selected: String, // last time this meal was selected. Needs to be parsed with LocalDate (cannot do it beforehand. Makes difficulties with readinf and writing
//var course: String // type of meal eg. desert
fun main(args: Array<String>) {
    val sentimentQuery = queryHuggingFace("I hate you")
    val negative = sentimentQuery.getJSONObject(0).get("score").toString().toFloat()
    val neutral = sentimentQuery.getJSONObject(1).get("score").toString().toFloat()
    val positive = sentimentQuery.getJSONObject(2).get("score").toString().toFloat()

//    queryRecipe(716429)
//    getMeal("tedt")
//    val dm = DataManager()
//    print(dm.dfUsers.head())
//    val user = dm.getUserByName("joost")
//    if (user != null) {
//        dm.writeUser(user)
//    }
//    print(user?.ingredients)

}

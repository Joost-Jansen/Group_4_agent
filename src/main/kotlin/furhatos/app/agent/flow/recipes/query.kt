package furhatos.app.agent.flow.recipes

import Meal
import furhatos.app.agent.current_user
import furhatos.flow.kotlin.furhat
import furhatos.flow.kotlin.state
import furhatos.gestures.Gestures
import khttp.get
import khttp.post
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.time.LocalDate
import kotlin.math.min
import kotlin.random.Random

// Query to the API
fun query(query_field: String, query_type: String, user_input: String = "") = state {
    onEntry {
        // While waiting for API
        furhat.say(async = true) {
            +"Let's see"
            +Gestures.GazeAway
        }

        var query = "$BASE_URL/$query_field/$query_type?" +
                "apiKey=${API_KEY}" +
                "&diet=${current_user.diet.joinToString(",")}" +
                "&intolerances=${current_user.allergies.joinToString(",")}" +
                "&number=10" +
                "&type=${current_user.preferred_meal_type}" +
                "&cuisine=${getPreferredCuisines()}" +
                "&includeIngredients=${getPreferredIngredients()}"
        println(query)

        val result = mutableListOf<Meal>()
        when (query_type) {
            "search" -> {
                // Get the title of the dish from the response
                val objects = get(query).jsonObject.getJSONArray("results")
                print(objects)

                for (i in 0 until 10) {
                    val meal = JSONObjectToMeal(objects.getJSONObject(i))
                    result += meal
                }
            }
            "jokes/random"-> {
                // Get the title of the dish from the response
                val joke_query = "$BASE_URL/$query_field/$query_type?apiKey=${API_KEY}"
                terminate(get(joke_query).jsonObject.getString("text"))
            }
            "complexSearch" -> {
                //search recipe based on title
                val question = user_input.replace("+", " plus ").replace(" ", "+")
                query += "&titleMatch=${question}"

                val objects = get(query).jsonObject.getJSONArray("results")
                if(objects.length() != 0) {
                    val meal = JSONObjectToMeal(objects.getJSONObject(0))
                    result += meal
                }
            }

            else -> {
                print("Query type not defined")
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

fun getPreferredCuisines(): String {
    current_user.cuisines.sortByDescending { it.likes }
    return if (current_user.cuisines.size >= 3) {
        current_user.cuisines.take(3).joinToString { it.name }
    } else {
        println(current_user.cuisines.first().name)
        current_user.cuisines.first().name
    }
}

fun getPreferredIngredients(): String {
    var res = ""

    // Include all mentioned ingredients of the day preference module
    res += current_user.preferred_ingredients.joinToString { "," }

    // Take one of the highest ranked ingredients (with some randomness)
    current_user.ingredients.sortByDescending { it.likes }
    val rankedIngredients = current_user.ingredients.filter { it.likes > 0 }
    if (rankedIngredients.isNotEmpty()) {
        val i = Random.nextInt(0, min(3, rankedIngredients.size))
        res += rankedIngredients[i].name
    }

    // Take on of the left-overs
    if (current_user.left_overs.isNotEmpty()) {
        val i = Random.nextInt(0, current_user.left_overs.size)
        res += current_user.left_overs[i].name
    }

    return res
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
    return queryRecipe(recipeId)
}

fun queryRecipe(recipe_id: Int): Meal {
    val query = "$BASE_URL/recipes/$recipe_id/information?apiKey=${API_KEY}&includeNutrion=false"
    val recipe = get(query).jsonObject
    val name = recipe.get("title") as String
    val ingredients = mutableListOf<String>()
    for ( i in recipe.getJSONArray("extendedIngredients")){
        val json : JSONObject = i as JSONObject
        ingredients.add(json.get("nameClean").toString())
    }
    val dishTypes = recipe.get("dishTypes") as JSONArray
    val dishType = dishTypes.get(0).toString()
    val likes = 0
    val lastSelected = LocalDate.now().toString()
    val link = recipe.getString("sourceUrl")
    val prepTime = recipe.getInt("readyInMinutes")

    val meal = Meal(recipe_id, name, ingredients, dishType, likes, lastSelected, link, prepTime)
    return meal
}
//val id: Int, // id in spoonacular can request by getID
//val name: String, // name of meal
//var likes: Int, // amount of likes or dislikes (when negative)
//var last_selected: String, // last time this meal was selected. Needs to be parsed with LocalDate (cannot do it beforehand. Makes difficulties with readinf and writing
//var course: String // type of meal eg. desert
fun main(args: Array<String>) {
    queryRecipe(716429)
//    getMeal("tedt")
//    val dm = DataManager()
//    print(dm.dfUsers.head())
//    val user = dm.getUserByName("joost")
//    if (user != null) {
//        dm.writeUser(user)
//    }
//    print(user?.ingredients)

}

package furhatos.app.agent.flow.recipes

import DataManager
import Meal
import furhatos.app.agent.resources.getMealTypes
import furhatos.flow.kotlin.furhat
import furhatos.flow.kotlin.state
import furhatos.gestures.Gestures
import khttp.get
import khttp.post
import org.jetbrains.kotlinx.dataframe.api.head
import org.json.JSONArray
import java.net.URLEncoder
import java.time.LocalDate
import kotlin.reflect.typeOf

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
            "complexSearch" -> {
                //search recipe based on title
                val question = user_input.replace("+", " plus ").replace(" ", "+")
                val query = base_query.plus("&titleMatch=${question}")

                val objects = get(query).jsonObject.getJSONArray("results")
                if(objects.length() != 0) {
                    val recipeId = objects.getJSONObject(0).getInt("id")
                    result += recipeId.toString()
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



fun queryRecipe(recipe_id: Int): Meal {
    val query = "$BASE_URL/recipes/$recipe_id/information?apiKey=${API_KEY}&includeNutrion=false"

    val recipe = get(query).jsonObject
    val name = recipe.get("title") as String
    val likes = 0
    val lastSelected = LocalDate.now().toString()
    val dishTypes = recipe.get("dishTypes") as JSONArray
    val dishType = dishTypes.get(0).toString()

    val meal = Meal(recipe_id, name, likes, lastSelected, dishType)
    return meal
}
//val id: Int, // id in spoonacular can request by getID
//val name: String, // name of meal
//var likes: Int, // amount of likes or dislikes (when negative)
//var last_selected: String, // last time this meal was selected. Needs to be parsed with LocalDate (cannot do it beforehand. Makes difficulties with readinf and writing
//var course: String // type of meal eg. desert
fun main(args: Array<String>) {
//    queryRecipe(716429)
    val date = LocalDate.now().dayOfWeek.toString()
    print(date)
//    val dm = DataManager()
//    print(dm.dfUsers.head())
//    val user = dm.getUserByName("joost")
//    if (user != null) {
//        dm.writeUser(user)
//    }
//    print(user?.ingredients)

}

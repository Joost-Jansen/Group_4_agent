//package furhatos.app.agent.flow.memory

import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.*
import org.jetbrains.kotlinx.dataframe.io.readJson
import org.jetbrains.kotlinx.dataframe.io.writeJson
import java.time.LocalDateTime
/**
 *  User class with all data known from user. Contains short-term, long-term and one-shot memory of the user
 */
data class User(
    val id: Int, val name: String, val diet: MutableList<String>, val allergies: MutableList<String>,
    val previous_recommendations: MutableList<String>, val favourite_meals: MutableList<String>,
    val favourite_ingredients: MutableList<String>, val least_favourite_ingredients: MutableList<String>,
    val preferences: MutableList<String>, val prior_meal: String, val time: LocalDateTime, val left_overs: MutableList<String>)


/**
 *  Class Datamanger:
 *  Handles storage of long term and one_shot data.
 *  Can read and write data
 */
class DataManager () {
        val oneShot =  DataFrame.readJson("src/main/kotlin/furhatos/app/agent/flow/memory/one_shot.json")
        val longTerm =  DataFrame.readJson("src/main/kotlin/furhatos/app/agent/flow/memory/long_term.json")
        val dfUsers = oneShot.leftJoin(longTerm){ "user_id" match "user_id"}

    /**
     * Creates new user with newID.
     * NOTE: not added to memory yet. Use writeUser to add.
     * input: all User variables except ID
     * return: returns new User
     */
    fun newUser(name: String,diet: MutableList<String>, allergies: MutableList<String>,
                previous_recommendations: MutableList<String>, favourite_meals: MutableList<String>,
                favourite_ingredients: MutableList<String>, least_favourite_ingredients: MutableList<String>,
                preferences: MutableList<String>, prior_meal: String, time: LocalDateTime, left_overs: MutableList<String>): User {
        val id = dfUsers.maxBy("user_id")["user_id"].toString().toInt() + 1
        return User(id, name, diet, allergies, previous_recommendations, favourite_meals, favourite_ingredients, least_favourite_ingredients, preferences, prior_meal, time, left_overs)
    }

    /**
     * Get existing user by name returns null if not found
     *  input: User string
     *  return: User or null
     */
    fun getUserByName(username: String): User? {
        val dfUser = dfUsers.firstOrNull { it["name"] == username }
        if (dfUser != null){
            return User(
                dfUser["user_id"].toString().toInt(),
                dfUser["name"].toString(),
                stringToList(dfUser["diet"].toString()),
                stringToList(dfUser["allergies"].toString()),
                stringToList(dfUser["previous_recommendations"].toString()),
                stringToList(dfUser["favourite_meals"].toString()),
                stringToList(dfUser["favourite_ingredients"].toString()),
                stringToList(dfUser["least_favourite_ingredients"].toString()),
                mutableListOf(),
                "",
                LocalDateTime.now(),
                mutableListOf()
            )
        }
        else {
            return null
        }
    }


    /**
     * input: string of type "[x,y,z]"
     * return: listOf<String>
     */
    fun stringToList(str: String): MutableList<String>{
        return str.drop(1).dropLast(1).split(",").toMutableList()
    }

    /**
     * At the end of the session write information back to one_shot and long_term
     * input: User
     * return: nothing
     */
    fun writeUser(user: User){
        print(oneShot.head())
        val oneShotNames = listOf("user_id", "name", "diet", "allergies")
        val oneShotValues = listOf(user.id, user.name, user.diet, user.allergies)
        val oneShotUser = dataFrameOf(oneShotNames, oneShotValues)
        val oneShotDropped = oneShot.drop{ it["user_id"] == user.id}
        val newOneShot = oneShotDropped.concat(oneShotUser).sortBy("user_id")
        print(newOneShot.head())
        newOneShot.writeJson("src/main/kotlin/furhatos/app/agent/flow/memory/one_shot.json", prettyPrint = true)

        print(longTerm.head())
        val longTermNames = listOf("user_id","previous_recommendations","favourite_meals","favourite_ingredients", "least_favourite_ingredients")
        val longTermValues = listOf(user.id, user.previous_recommendations, user.favourite_meals, user.favourite_ingredients, user.least_favourite_ingredients)
        val longTermUser = dataFrameOf(longTermNames, longTermValues)
        val longTermDropped = longTerm.drop{ it["user_id"] == user.id}
        val newLongTerm = longTermDropped.concat(longTermUser).sortBy("user_id")
        print(newLongTerm.head())
        newLongTerm.writeJson("src/main/kotlin/furhatos/app/agent/flow/memory/long_term.json", prettyPrint = true)
    }
}

/**
 * Small main for testinng datamanager class
 * and examples
 */
fun main(args: Array<String>) {
    val dm = DataManager()

    // example of user check
    val inputName = "jochem"
    // Example check if user joost already exists
    val user = dm.getUserByName(inputName)
    if (user != null) {
        // if yes edit some things during sessions and continue
        user.least_favourite_ingredients.add("milk")
        user.favourite_meals.remove("sandwich ham cheese")
        // at the end of the session write back to long term and one-shor
        dm.writeUser(user)
    }
    // New user
    else {
        // Retrieve information
        // first one-shot
        val name = inputName
        val diet = mutableListOf("")
        val allergies = mutableListOf("peanuts")
        // long term
        val previous_recommendations = mutableListOf<String>()
        val favourite_meals = mutableListOf<String>()
        val favourite_ingredients = mutableListOf<String>()
        val least_favourite_ingredients = mutableListOf<String>()
        // one-shot
        val preferences = mutableListOf<String>()
        val prior_meal =  ""
        val time = LocalDateTime.now()
        val left_overs = mutableListOf<String>()
        val newUser = dm.newUser(name, diet, allergies, previous_recommendations, favourite_meals, favourite_ingredients,
            least_favourite_ingredients, preferences, prior_meal, time , left_overs )

        // Write new user to file but forget short term memory
        dm.writeUser(newUser)
    }

}
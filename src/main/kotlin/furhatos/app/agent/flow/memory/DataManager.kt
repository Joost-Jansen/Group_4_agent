//package furhatos.app.agent.flow.memory
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.*
import org.jetbrains.kotlinx.dataframe.io.readJson
import org.jetbrains.kotlinx.dataframe.io.writeJson
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*

/**
 *  User class with all data known from user. Contains short-term, long-term and one-shot memory of the user
 */
data class User(
    // one shot
    val user_id: Int,
    var name: String,
    var diet: MutableList<String>,
    var allergies: MutableList<String>,
    // long term
    var meals: MutableList<Meal>,
    var ingredients: MutableList<Ingredient>,
    var cuisines: MutableList<Cuisine>,
    // short term
    var time: LocalDate,
    var preferences: MutableList<String>,
    var left_overs: MutableList<Ingredient>)


data class Meal(
    val id: Int, // id in spoonacular can request by getID
    val name: String, // name of meal
    var likes: Int, // amount of likes or dislikes (when negative)
    var last_selected: LocalDate, // last time this meal was selected
    var course: String // type of meal eg. desert
) : Comparable<Meal> {
    override fun compareTo(other: Meal) = compareValuesBy(this, other) { it.likes }
}

data class Ingredient(
    val name: String,
    var likes: Int
) : Comparable<Ingredient> {
    override fun compareTo(other: Ingredient) = compareValuesBy(this, other) { it.likes }
}

data class Cuisine(
    val name: String,
    var likes: Int
) : Comparable<Cuisine> {
    override fun compareTo(other: Cuisine) = compareValuesBy(this, other) { it.likes }
}

class UserUpdates {
    fun findLikedMeal(list: MutableList<Meal>): Meal? {
        val max = list.maxOrNull()
        return if (max != null && max.likes > 0) {
            val diff: Long = ChronoUnit.DAYS.between(max.last_selected, LocalDate.now())
            if (diff < 3 && list.remove(max)) findLikedMeal(list) else max
        } else max
    }

    // Update last selected as meal
    fun updateMealDate(meal : Meal, list: MutableList<Meal>, date: LocalDate): MutableList<Meal> {
        list?.find { it.id == meal.id }?.last_selected = date
        return list
    }

    // Update meal likes
    fun updateLikes(meal : Meal, list: MutableList<Meal>, rating: Int): MutableList<Meal> {
        list?.find { it.id == meal.id }?.likes?.plus(rating)
        return list
    }

    // Ingredient likes
    fun updateLikes(meal : Ingredient, list: MutableList<Ingredient>, rating: Int): MutableList<Ingredient> {
        list?.find { it.name == meal.name }?.likes?.plus(rating)
        return list
    }

    // Cuisine likes
    fun updateLikes(meal : Cuisine, list: MutableList<Cuisine>, rating: Int): MutableList<Cuisine> {
        list?.find { it.name == meal.name }?.likes?.plus(rating)
        return list
    }
}

/**
 *  Class Datamanger:
 *  Handles storage of long term and one_shot data.
 *  Can read and write data
 */
class DataManager () {
        var oneShot =  DataFrame.readJson("src/main/kotlin/furhatos/app/agent/flow/memory/one_shot.json")
        var longTerm =  DataFrame.readJson("src/main/kotlin/furhatos/app/agent/flow/memory/long_term.json")
        var dfUsers = oneShot.leftJoin(longTerm){ "user_id" match "user_id"}

    /**
     * Creates new user with newID.
     * NOTE: not added to memory yet. Use writeUser to add.
     * input: all User variables except ID
     * return: returns new User
     */
    fun newUser(name: String = "",
                diet: MutableList<String> = mutableListOf(),
                allergies: MutableList<String> =  mutableListOf(),
                meals: MutableList<Meal> =  mutableListOf(),
                favourite_ingredients: MutableList<Ingredient> = mutableListOf(),
                cuisines: MutableList<Cuisine> = mutableListOf(),
                preferences: MutableList<String> = mutableListOf(),
                time: LocalDate = LocalDate.now(),
                left_overs: MutableList<Ingredient> = mutableListOf()
    ): User {
        val id = dfUsers.maxBy("user_id")["user_id"].toString().toInt() + 1
        return User(id, name, diet, allergies, meals, favourite_ingredients, cuisines,  time, preferences, left_overs)
    }

    /**
     * Get existing user by name returns null if not found
     *  input: User string
     *  return: User or null
     */
    fun getUserByName(username: String): User? {

        val dfUser = dfUsers.firstOrNull { it["name"] == username }
        if (dfUser != null){

            val m = dfUser["meals"] as DataFrame<*>
            val meals: MutableList<Meal> = mutableListOf()
            m.forEach {
                meals.add(Meal(it["id"].toString().toInt(), it["name"].toString(), it["likes"].toString().toInt(), LocalDate.parse(it["last_selected"].toString()), it["course"].toString()))
            }

            val i = dfUser["ingredients"] as DataFrame<*>
            val ingredients: MutableList<Ingredient> = mutableListOf()
            i.forEach {
                ingredients.add(Ingredient(it["name"].toString(), it["likes"].toString().toInt()))
            }

            val c = dfUser["ingredients"] as DataFrame<*>
            val cuisines: MutableList<Cuisine> = mutableListOf()
            c.forEach {
                cuisines.add(Cuisine(it["name"].toString(), it["likes"].toString().toInt()))
            }

            return User(
                dfUser["user_id"].toString().toInt(),
                dfUser["name"].toString(),
                stringToList(dfUser["diet"].toString()),
                stringToList(dfUser["allergies"].toString()),
                meals,
                ingredients,
                cuisines,
                LocalDate.now(),
                mutableListOf(),
                mutableListOf<Ingredient>()
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
        val oneShotValues = listOf(user.user_id, user.name, user.diet, user.allergies)
        val oneShotUser = dataFrameOf(oneShotNames, oneShotValues)
        val oneShotDropped = oneShot.drop{ it["user_id"] == user.user_id}
        val newOneShot = oneShotDropped.concat(oneShotUser).sortBy("user_id")
        this.oneShot = newOneShot

        print(newOneShot.head())
        newOneShot.writeJson("src/main/kotlin/furhatos/app/agent/flow/memory/one_shot.json", prettyPrint = true)

        print(longTerm.head())
//        val meals = dataFrameOf(listOf("id", "name", "likes", "last_selected", "course"), )
        val longTermNames = listOf("user_id","meals","ingredients","cuisines")
        val longTermValues = listOf(user.user_id, user.meals, user.ingredients, user.cuisines)
        val longTermUser = dataFrameOf(longTermNames, longTermValues)
        val longTermDropped = longTerm.drop{ it["user_id"] == user.user_id}
        val newLongTerm = longTermDropped.concat(longTermUser).sortBy("user_id")
        this.longTerm = newLongTerm

        print(newLongTerm.head())
        newLongTerm.writeJson("src/main/kotlin/furhatos/app/agent/flow/memory/long_term.json", prettyPrint = true)
        dfUsers = oneShot.leftJoin(longTerm){ "user_id" match "user_id"}
    }
}

/**
 * Small main for testinng datamanager class
 * and examples
 */
fun main(args: Array<String>) {
    val dm = DataManager()
    print(dm.dfUsers.head())
    val user = dm.getUserByName("joost")
    if (user != null) {
        dm.writeUser(user)
    }
    print(user?.ingredients)


//    var longTerm =  DataFrame.readJson("src/main/kotlin/furhatos/app/agent/flow/memory/long_term2.json")
//    print(longTerm.forEach { print(it["prior_meals"]) })
//    print(longTerm.head())
//    val dm = DataManager()
//
//    // example of user check
//    val inputName = "james"
//    // Example check if user joost already exists
//    val user = dm.getUserByName(inputName)
//    if (user != null) {
//        // if yes edit some things during sessions and continue
//        user.least_favourite_ingredients.add("milk")
//        user.favourite_meals.remove("sandwich ham cheese")
//        // at the end of the session write back to long term and one-shor
//        dm.writeUser(user)
//    }
//    // New user
//    else {
//        // Retrieve information
//        // first one-shot
//        val name = inputName
//        val diet = mutableListOf("")
//        val allergies = mutableListOf("peanuts")
//        // long term
//        val previous_recommendations = mutableListOf<String>()
//        val favourite_meals = mutableListOf<String>()
//        val favourite_ingredients = mutableListOf<String>()
//        val least_favourite_ingredients = mutableListOf<String>()
//        // one-shot
//        val preferences = mutableListOf<String>()
//        val prior_meal =  ""
//        val time = LocalDateTime.now()
//        val left_overs = mutableListOf<String>()
//        val newUser = dm.newUser(name, diet, allergies, previous_recommendations, favourite_meals, favourite_ingredients,
//            least_favourite_ingredients, preferences, prior_meal, time , left_overs )
//
//        // Write new user to file but forget short term memory
//        dm.writeUser(newUser)
//    }

}
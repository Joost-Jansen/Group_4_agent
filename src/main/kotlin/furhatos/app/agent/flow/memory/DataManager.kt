//package furhatos.app.agent.flow.memory
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import furhatos.app.agent.flow.memory.data.*
import furhatos.app.agent.flow.recipes.queryRecipe
import java.io.File
import java.io.FileWriter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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
    var time: Int,
    var preferred_ingredients: MutableList<String>,
    var left_overs: MutableList<Ingredient>,
    var preferred_meal_type: String)  : Comparable<User> {
    override fun compareTo(other: User) = compareValuesBy(this, other) { it.user_id }

}

class UserUpdates {
    fun findLikedMeal(list: MutableList<Meal>): Meal? {
        val max = list.maxOrNull()
        return if (max != null && max.likes > 0) {
            val diff: Long = ChronoUnit.DAYS.between(LocalDate.parse(max.last_selected), LocalDate.now())
            if (diff < 3 && list.remove(max)) findLikedMeal(list) else max
        } else max
    }

    fun findLastMeal(list: MutableList<Meal>): Meal? {
        return list.minByOrNull { ChronoUnit.DAYS.between(LocalDate.parse(it.last_selected), LocalDate.now()) }
    }

    // Update last selected as meal
    fun updateMealDate(meal : Meal, list: MutableList<Meal>, date: LocalDate): MutableList<Meal> {
        list.find { it.id == meal.id }?.last_selected = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        return list
    }

    // Update meal likes
    fun updateLikes(meal : Meal, list: MutableList<Meal>, rating: Int): MutableList<Meal> {
        list.find { it.id == meal.id }?.likes?.plus(rating)
        return list
    }

    // Ingredient likes
    fun updateLikes(meal : Ingredient, list: MutableList<Ingredient>, rating: Int): MutableList<Ingredient> {
        list.find { it.name == meal.name }?.likes?.plus(rating)
        return list
    }

    // Cuisine likes
    fun updateLikes(meal : Cuisine, list: MutableList<Cuisine>, rating: Int): MutableList<Cuisine> {
        list.find { it.name == meal.name }?.likes?.plus(rating)
        return list
    }

    fun addMeal(mealID : Int, list: MutableList<Meal>) : MutableList<Meal> {
        for(m : Meal in list) {
            if(m.id == mealID) {
                m.likes =+ 1
                m.last_selected = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                return list
            }
        }
        val m = queryRecipe(mealID)
        list.add(m)
        return list
    }



//    fun addLeftOvers(user : User, list: MutableList<Ingredients>, ) : MutableList<Ingredients> {
//
//    }
}

/**
 *  Class Datamanger:
 *  Handles storage of long term and one_shot data.
 *  Can read and write data
 */
class DataManager () {
    var dfUsers: MutableList<User> = mutableListOf()
    init {
        val inputStreamOneShot = File("src/main/kotlin/furhatos/app/agent/flow/memory/one_shot.json").inputStream().bufferedReader().use { it.readText() }
        val oneShotType = object : TypeToken<List<OneShotData>>() {}.type
        val listOneShot = Gson().fromJson<List<OneShotData>>(inputStreamOneShot, oneShotType)

        val inputStreamLongTerm = File("src/main/kotlin/furhatos/app/agent/flow/memory/long_term.json").inputStream().bufferedReader().use { it.readText() }
        val longTermType = object : TypeToken<List<LongTermData>>() {}.type
        val listLongTerm = Gson().fromJson<List<LongTermData>>(inputStreamLongTerm, longTermType)
        for (oData in listOneShot) {
            for (ldata in listLongTerm) {
                if (oData.user_id == ldata.user_id) {
                    var u = User(user_id = oData.user_id, name = oData.name, diet = oData.diet,
                        allergies = oData.allergies, cuisines = ldata.cuisines, ingredients = ldata.ingredients,
                        left_overs = mutableListOf(), meals = ldata.meals, preferred_meal_type = "", preferred_ingredients = mutableListOf(), time = Int.MAX_VALUE
                    )
                    dfUsers.add(u)
                }
            }
        }
    }
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
                preferred_ingredients: MutableList<String> = mutableListOf(),
                time: Int = Int.MAX_VALUE,
                left_overs: MutableList<Ingredient> = mutableListOf(),
                preferred_meal_type: String = ""



    ): User {
        if (dfUsers.size > 0){
            val id = Collections.max(dfUsers).user_id + 1
            val u = User(id, name, diet, allergies, meals, favourite_ingredients, cuisines,  time, preferred_ingredients, left_overs, preferred_meal_type)
            dfUsers.add(u)
            return u
        } else{
            val u = User(0, name, diet, allergies, meals, favourite_ingredients, cuisines,  time, preferred_ingredients, left_overs, preferred_meal_type)
            dfUsers.add(u)
            return u
        }


    }

    /**
     * Get existing user by name returns null if not found
     *  input: User string
     *  return: User or null
     */
    fun getUserByName(username: String): User? {
        if ( this.dfUsers.size > 0) {
            val dfUser = dfUsers.firstOrNull { it.name == username }
                return dfUser
            } else {
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
    fun writeUser(){
        val oneShotList = mutableListOf<OneShotData>()
        val longTermList = mutableListOf<LongTermData>()
        for(user in dfUsers) {
            val o = OneShotData( user_id = user.user_id, allergies = user.allergies, diet = user.diet, name = user.name)
            oneShotList.add(o)
            val l = LongTermData(user_id = user.user_id, meals = user.meals, ingredients = user.ingredients, cuisines = user.cuisines)
            longTermList.add(l)
        }
        val gson = GsonBuilder().setPrettyPrinting().create()
        val fo = FileWriter("src/main/kotlin/furhatos/app/agent/flow/memory/one_shot.json")
        gson.toJson(oneShotList, fo)
        fo.flush()
        val fl = FileWriter("src/main/kotlin/furhatos/app/agent/flow/memory/long_term.json")
        gson.toJson(longTermList, fl)
        fl.flush()
    }
}

/**
 * Small main for testinng datamanager class
 * and examples
 */
fun main(args: Array<String>) {
    val dm = DataManager()
    dm.writeUser()
//    val user = dm.getUserByName("James")
//    if (user != null) {
//        dm.writeUser(user)
//    }


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

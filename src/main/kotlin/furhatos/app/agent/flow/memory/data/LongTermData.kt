package furhatos.app.agent.flow.memory.data


data class LongTermData(
    val user_id: Int,
    var meals: MutableList<Meal>,
    var ingredients: MutableList<Ingredient>,
    var cuisines: MutableList<Cuisine>
)
data class Meal(
    val id: Int, // id in spoonacular can request by getID
    val name: String, // name of meal
    var ingredients: MutableList<String>,
    var cuisines: MutableList<String>,
    val course: String, // type of meal eg. desert
    var likes: Int, // amount of likes or dislikes (when negative)
    var last_selected: String, // last time this meal was selected. Needs to be parsed with LocalDate (cannot do it beforehand. Makes difficulties with readinf and writing
    var link: String,
    var prepTime: Int
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

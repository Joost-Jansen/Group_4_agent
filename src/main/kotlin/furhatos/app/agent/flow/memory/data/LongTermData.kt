package furhatos.app.agent.flow.memory.data

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class LongTermData {
    @SerializedName("user_id") @Expose var user_id: Int? = null
    @SerializedName("meals") @Expose var meals: MutableList<MealData>? = null
    @SerializedName("ingredients") @Expose var ingredients: MutableList<IngredientsData>? = null
    @SerializedName("cuisines") @Expose var cuisines: MutableList<CuisinesData>? = null
}

class MealData {
    @SerializedName("course") @Expose var course: String? = null
    @SerializedName("id") @Expose var id: Int? = null
    @SerializedName("ingredients") @Expose var ingredients: List<String>? = null
    @SerializedName("last_selected") @Expose var last_selected: String? = null
    @SerializedName("likes") @Expose var likes: Int? = null
    @SerializedName("name") @Expose var name: String? = null
}
class IngredientsData {
    @SerializedName("likes") @Expose var likes: Int? = null
    @SerializedName("name") @Expose var name: String? = null
}
class CuisinesData {
    @SerializedName("likes") @Expose var likes: Int? = null
    @SerializedName("name") @Expose var name: String? = null
}

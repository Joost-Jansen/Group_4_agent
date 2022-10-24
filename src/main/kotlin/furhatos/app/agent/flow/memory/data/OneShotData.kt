package furhatos.app.agent.flow.memory.data

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class OneShotData {
    @SerializedName ("user_id") @Expose var user_id: Int? = null
    @SerializedName ("name") @Expose var name: String? = null
    @SerializedName ("diet") @Expose var diet: MutableList<String>? = null
    @SerializedName ("allergies") @Expose var allergies: MutableList<String>? = null
}

package furhatos.app.agent.flow.memory.data


data class OneShotData(
    val user_id: Int,
    val name: String,
    var diet: MutableList<String>,
    var allergies: MutableList<String>
)

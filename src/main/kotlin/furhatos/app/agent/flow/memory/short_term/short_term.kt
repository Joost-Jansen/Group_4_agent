package furhatos.app.agent.flow.memory.short_term

import java.util.Date

data class short_term (val user_id: Int, val preferences: List<String>, val prior_meal: String, val time: Date, val left_overs: List<String>){

}
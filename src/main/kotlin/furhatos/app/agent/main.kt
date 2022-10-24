package furhatos.app.agent
import DataManager
import User
import UserUpdates
import furhatos.app.agent.flow.Init
import furhatos.app.agent.flow.memory.data.Cuisine
import furhatos.app.agent.flow.memory.data.Ingredient
import furhatos.app.agent.flow.memory.data.Meal
import furhatos.flow.kotlin.Flow
import furhatos.skills.Skill
import java.time.LocalDate

//global variable
val dataManager = DataManager()
var current_user: User = User(-1, "", mutableListOf<String>(), mutableListOf<String>(),
    mutableListOf<Meal>(), mutableListOf<Ingredient>(), mutableListOf<Cuisine>(), Int.MAX_VALUE, "", mutableListOf<String>(), mutableListOf<Ingredient>())
val userUpdates = UserUpdates()

class AgentSkill : Skill() {
    override fun start() {
        Flow().run(Init)
    }
}

fun main(args: Array<String>) {
    Skill.main(args)

}

package furhatos.app.agent
import DataManager
import User
import furhatos.app.agent.flow.Init
import furhatos.flow.kotlin.Flow
import furhatos.skills.Skill
import java.time.LocalDateTime

//global variable
val dataManager = DataManager()
var current_user: User = User(-1, "", mutableListOf<String>(), mutableListOf<String>(),
    mutableListOf<String>(), mutableListOf<String>(), mutableListOf<String>(),mutableListOf<String>(),
    mutableListOf<String>(), "", LocalDateTime.now(), mutableListOf<String>())

class AgentSkill : Skill() {
    override fun start() {
        Flow().run(Init)
    }
}

fun main(args: Array<String>) {
    Skill.main(args)

}

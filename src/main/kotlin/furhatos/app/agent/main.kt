package furhatos.app.agent

import furhatos.app.agent.flow.Init
import furhatos.flow.kotlin.Flow
import furhatos.skills.Skill
class AgentSkill : Skill() {
    override fun start() {
        Flow().run(Init)
    }
}

fun main(args: Array<String>) {
    Skill.main(args)

}

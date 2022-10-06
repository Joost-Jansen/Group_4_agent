package furhatos.app.agent

import furhatos.app.agent.flow.*
import furhatos.skills.Skill
import furhatos.flow.kotlin.*

class AgentSkill : Skill() {
    override fun start() {
        Flow().run(Init)
    }
}

fun main(args: Array<String>) {
    Skill.main(args)
}

package furhatos.app.agent.flow

import furhatos.app.agent.flow.main.Idle
import furhatos.app.agent.setting.distanceToEngage
import furhatos.app.agent.setting.maxNumberOfUsers
import furhatos.flow.kotlin.*
import furhatos.flow.kotlin.voice.Voice


val Init : State = state() {
    init {

        /** Set our default interaction parameters */
        users.setSimpleEngagementPolicy(distanceToEngage, maxNumberOfUsers)
        furhat.voice = Voice("Matthew")
        /** start the interaction */
        goto(Idle)

    }
}

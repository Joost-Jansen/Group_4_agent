package furhatos.app.agent.flow

import furhatos.app.agent.flow.main.Idle
import furhatos.app.agent.flow.main.PersonalInformation
import furhatos.app.agent.nlu.GoToPIM
import furhatos.flow.kotlin.*

val Parent: State = state {

    onUserLeave(instant = true) {
        when {
            users.count == 0 -> goto(Idle)
            it == users.current -> furhat.attend(users.other)
        }
    }

    onUserEnter(instant = true) {
        furhat.glance(it)
    }

    // Used for trying out (TO BE REMOVED)
    onResponse<GoToPIM> {
        goto(PersonalInformation)
    }
}
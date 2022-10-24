package furhatos.app.agent.flow

import furhatos.app.agent.flow.main.Greeting
import furhatos.app.agent.flow.main.Idle
import furhatos.app.agent.nlu.WrongPerson
import furhatos.flow.kotlin.*

val Parent: State = state {

    onUserLeave(instant = true) {
        when {
            users.count == 0 -> goto(Idle)
            it == users.current -> furhat.attend(users.other)
        }
    }
    onResponse<WrongPerson> {
        furhat.say("sorry, I must have misunderstood.")
        goto(Greeting)
    }
    onUserEnter(instant = true) {
        furhat.glance(it)
    }
}

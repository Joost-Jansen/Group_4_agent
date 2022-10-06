package furhatos.app.agent.flow.main

import furhatos.app.agent.flow.Parent
import furhatos.flow.kotlin.*
import furhatos.nlu.common.No
import furhatos.nlu.common.Yes

val Greeting : State = state(Parent) {
    onEntry {
        furhat.ask("Should I go to spoonacular?")
    }

    onResponse<Yes> {
        goto(start)
    }

    onResponse<No> {
        furhat.say("Ok.")
    }
}

package furhatos.app.agent.flow.main

import furhatos.app.agent.flow.Parent
import furhatos.app.agent.flow.recipes.Recommendation
import furhatos.flow.kotlin.*
import furhatos.nlu.common.No
import furhatos.nlu.common.Yes

val Greeting : State = state(Parent) {
    onEntry {
        furhat.ask("Should I go to Spoonacular?")
    }

    onResponse<Yes> {
        goto(Recommendation(emptyList()))
    }

    onResponse<No> {
        furhat.say("Ok.")
        goto(Idle)
    }
}

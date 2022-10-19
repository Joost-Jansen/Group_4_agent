package furhatos.app.agent.flow.main

import furhatos.app.agent.flow.Parent
import furhatos.app.agent.flow.recipes.FoodJoke
import furhatos.flow.kotlin.State
import furhatos.flow.kotlin.furhat
import furhatos.flow.kotlin.onResponse
import furhatos.flow.kotlin.state
import furhatos.nlu.common.No
import furhatos.nlu.common.Yes

val Evaluation : State = state(Parent) {
    onEntry {
        furhat.ask("Welcome to the evaluation module. This is the last module. Would you like to stop?")
    }

    onResponse<No> {
        furhat.say("Alright, then I will tell you a joke.")
        goto(FoodJoke)
    }

    onResponse<Yes> {
        furhat.say("Goodbye.")
        goto(Idle)
    }
}
package furhatos.app.agent.flow.main

import furhatos.app.agent.flow.Parent
import furhatos.app.agent.flow.recipes.FoodJoke
import furhatos.app.agent.flow.recipes.Recommendation
import furhatos.flow.kotlin.State
import furhatos.flow.kotlin.furhat
import furhatos.flow.kotlin.onResponse
import furhatos.flow.kotlin.state
import furhatos.nlu.common.No
import furhatos.nlu.common.Yes

val DayPreference : State = state(Parent) {
    onEntry {
        furhat.ask("Welcome to the day preference module. Would you like to go to the recommendation module?")
    }

    onResponse<No> {
        furhat.say("Alright, I will tell you a joke.")
        goto(FoodJoke)
    }

    onResponse<Yes> {
        furhat.say("We will now go to the recommendation module.")
        goto(Recommendation)
    }
}
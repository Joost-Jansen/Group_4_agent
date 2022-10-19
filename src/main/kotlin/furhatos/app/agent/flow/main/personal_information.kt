package furhatos.app.agent.flow.main

import furhatos.app.agent.flow.Parent
import furhatos.flow.kotlin.State
import furhatos.flow.kotlin.furhat
import furhatos.flow.kotlin.onResponse
import furhatos.flow.kotlin.state
import furhatos.nlu.common.No
import furhatos.nlu.common.Yes

val PersonalInformation : State = state(Parent) {
    onEntry {
        furhat.ask("Welcome to the personal information module. Would you like to go to the day preference module?")
    }

    onResponse<No> {
        furhat.say("Alright, I will go to Idle.")
        goto(Idle)
    }

    onResponse<Yes> {
        furhat.say("We will now go to the day preference module.")
        goto(DayPreference)
    }
}
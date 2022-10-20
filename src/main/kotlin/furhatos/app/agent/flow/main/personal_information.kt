package furhatos.app.agent.flow.main

import furhatos.app.agent.flow.Parent
import furhatos.app.agent.nlu.PersonalInformationIntent
import furhatos.flow.kotlin.State
import furhatos.flow.kotlin.furhat
import furhatos.flow.kotlin.onResponse
import furhatos.flow.kotlin.state
import furhatos.nlu.common.No
import furhatos.nlu.common.Yes

val PersonalInformation : State = state(Parent) {
    onEntry {
        furhat.ask("I would like to get to know you better. Could you tell me something about yourself?")
    }

    onResponse<No> {
        furhat.say("Alright, I'll be available if you need me.")
        goto(Idle)
    }

    onResponse<Yes> {
        furhat.say("We will now go to the day preference module.")
        goto(DayPreference)
    }

    onResponse<PersonalInformationIntent> {
        furhat.say("Ok, you are ${it.intent}")
        goto(Idle)
    }
}
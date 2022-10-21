package furhatos.app.agent.flow.main

import furhatos.app.agent.current_user
import furhatos.app.agent.dataManager
import furhatos.app.agent.flow.Parent
import furhatos.flow.kotlin.State
import furhatos.flow.kotlin.furhat
import furhatos.flow.kotlin.onResponse
import furhatos.flow.kotlin.state
import furhatos.nlu.common.No
import furhatos.nlu.common.TellName

val Greeting : State = state(Parent) {
    onEntry {
        furhat.ask("Hi there, who am I talking to?")
    }

    onResponse<TellName> {
        val user = dataManager.getUserByName(it.intent.name.toString())
        // If name is not in memory
        if (user !== null){
            current_user = user
            // Skip personal identification
            furhat.say("Good to see you back, ${current_user.name}")
            goto(PersonalInformation)
        }
        else{
            furhat.say("Nice to meet you ${it.intent.name}")
            // goto personal identifcation
            current_user = dataManager.newUser( name= it.intent.name.toString())
            goto(PersonIdentification)
        }
    }

    onResponse<No> {
        furhat.say("Ok.")
        goto(Idle)
    }
}
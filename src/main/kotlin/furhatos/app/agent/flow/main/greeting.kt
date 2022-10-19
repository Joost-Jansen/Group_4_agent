package furhatos.app.agent.flow.main

import furhatos.app.agent.current_user
import furhatos.app.agent.dataManager
import furhatos.app.agent.flow.Parent
import furhatos.app.agent.flow.recipes.Recommendation
import furhatos.flow.kotlin.State
import furhatos.flow.kotlin.furhat
import furhatos.flow.kotlin.onResponse
import furhatos.flow.kotlin.state
import furhatos.nlu.common.No
import furhatos.nlu.common.TellName
import furhatos.nlu.common.Yes

val Greeting : State = state(Parent) {
    onEntry {
        furhat.ask("Hi there, who am I talking to?")
    }

    onResponse<TellName> {
        val user = dataManager.getUserByName(it.intent.name.toString())
        // If name is not in memory
        if (user != null){
            current_user = user
            // Skip personal identification
            furhat.say("Good to see you back, ${current_user.name}")
            goto(goToSpoonacular)
        }else{
            furhat.say("Nice to meet you ${it.intent.name}")
            // goto personal identifcation
            current_user = dataManager.newUser( name= it.intent.name.toString())
            goto(personal_identification)
        }
    }

    onResponse<No> {
        furhat.say("Ok.")
        goto(Idle)
    }
}
val goToSpoonacular : State = state(Parent) {
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
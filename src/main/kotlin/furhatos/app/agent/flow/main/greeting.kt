package furhatos.app.agent.flow.main

import furhatos.app.agent.current_user
import furhatos.app.agent.dataManager
import furhatos.app.agent.flow.Parent
import furhatos.flow.kotlin.State
import furhatos.flow.kotlin.furhat
import furhatos.flow.kotlin.onResponse
import furhatos.app.agent.nlu.UserIdentification
import furhatos.flow.kotlin.*
import furhatos.nlu.common.No

fun setUser(furhat: Furhat, name: String) {
    val user = dataManager.getUserByName(name)
    // If name is not in memory
    if (user !== null){
        current_user = user
        // Skip personal identification
        furhat.say("Good to see you back, ${current_user.name}")
    }
    else{
        furhat.say("Nice to meet you $name")
        // goto personal identifcation
        current_user = dataManager.newUser( name= name)
    }
}

val Greeting : State = state(Parent) {
    init {
        furhat.say("Hi There")
    }

    onEntry {
        val name = furhat.askFor<UserIdentification>("Who am I talking to?")
        if (name != null) {
            setUser(furhat, name.name.toString())
            goto(PersonIdentification)
        }
    }
    onReentry {
        val name = furhat.askFor<UserIdentification>("Could you repeat that?")
        if (name != null) {
            setUser(furhat, name.name.toString())
            goto(PersonIdentification)
        } else {
            reentry()
        }

    }
//

    onResponse<UserIdentification> {

    }

    onResponse<No> {
        furhat.say("Ok.")
        goto(Idle)
    }
}




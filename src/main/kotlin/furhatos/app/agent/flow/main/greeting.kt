package furhatos.app.agent.flow.main

import User
import com.amazonaws.event.DeliveryMode.Check
import furhatos.app.agent.current_user
import furhatos.app.agent.dataManager
import furhatos.app.agent.flow.Parent
import furhatos.flow.kotlin.State
import furhatos.flow.kotlin.furhat
import furhatos.flow.kotlin.onResponse
import furhatos.app.agent.nlu.UserIdentification
import furhatos.flow.kotlin.*
import furhatos.nlu.common.No
import furhatos.app.agent.flow.getCurrentEmotion

fun setUser(furhat: Furhat, name: String): User? {
    val user = dataManager.getUserByName(name)
    // If name is not in memory
    if (user !== null){
        current_user = user
        // Skip personal identification
        furhat.say("Good to see you back, ${current_user.name}")
        return user
    }
    else{
        furhat.say("Nice to meet you $name")
        // goto personal identifcation
        current_user = dataManager.newUser( name= name)
        return null
    }
}

val Greeting : State = state(Parent) {
    init {
    }
    onEntry {
        furhat.say("Hi There")
        val cur_emo = getCurrentEmotion()
        if(cur_emo.has("emotion")) {
            when (cur_emo.get("emotion")) {
                "Happy" -> furhat.say("It is good to see you happy. My recipes will make you even happier!")
                "Surpise" -> furhat.say("Are you surprised to see my skills?")
                "Sad" -> furhat.say("It seems you are sad. I hope I can cheer you up with a recipe")
                "Anger" -> furhat.say("I'm sure a recipe will calm you down")
            }
        }
        val name = furhat.askFor<UserIdentification>("Who am I talking to?")
        if (name != null) {
            val user = setUser(furhat, name.name.toString())
            if (user != null) {
                goto(CheckPersonalInformation)
            } else {
                goto(PersonIdentification)
            }
        } else {
            reentry()
        }
    }

    onReentry {
        val name = furhat.askFor<UserIdentification>("Could you repeat that?")
        if (name != null) {
            val user = setUser(furhat, name.name.toString())
            if (user != null) {
                goto(CheckPersonalInformation)
            } else {
                goto(PersonIdentification)
            }
        } else {
            reentry()
        }
    }

    onResponse<No> {
        furhat.say("Ok.")
        goto(Idle)
    }
}




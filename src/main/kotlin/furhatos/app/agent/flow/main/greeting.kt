package furhatos.app.agent.flow.main

import furhatos.app.agent.current_user
import furhatos.app.agent.dataManager
import furhatos.app.agent.flow.Parent
import furhatos.flow.kotlin.State
import furhatos.flow.kotlin.furhat
import furhatos.flow.kotlin.onResponse
import furhatos.app.agent.nlu.User_identification
import furhatos.flow.kotlin.*
import furhatos.nlu.common.AskName
import furhatos.nlu.common.No
import furhatos.nlu.common.TellName

val Person_recognition : State = state(Parent) {

    onEntry {
        furhat.ask("who am I talking to?")
    }


    onResponse {
        val list_of_names = arrayOf<String>()
        var name = stripNameFromSentence(it.text)
        if(name.length > 0) {
            var is_name = furhat.askYN("Is ${name} your name?")
            if (is_name == true) {
                furhat.say("Welcome ${name}!")
                if(name in list_of_names) {
                  goto(Greeting)
                } else {
                    furhat.say("It seems you are new, is that correct?")
                }
                goto(Greeting)
            } else {
                furhat.ask("Then what is your name?")
            }
        } else {
            furhat.say("I did not catch your name")
            furhat.ask("Who am I talking to?")
        }
    }


}

fun stripNameFromSentence(sentence: String): String {
    if(sentence.startsWith("I am", ignoreCase = true)) {
        return sentence.removePrefix("I am")
    } else if(sentence.startsWith("my name is", ignoreCase = true)) {
        return sentence.removePrefix("my name is")
    } else if(sentence.startsWith("they call me", ignoreCase = true)) {
        return sentence.removePrefix("they call me")
    } else {
        return sentence
    }
}

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

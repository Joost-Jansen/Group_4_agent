package furhatos.app.agent.flow.recipes

import furhatos.app.agent.flow.Parent
import furhatos.app.agent.flow.main.Idle
import furhatos.flow.kotlin.State
import furhatos.flow.kotlin.furhat
import furhatos.flow.kotlin.onResponse
import furhatos.flow.kotlin.state
import furhatos.nlu.common.No
import furhatos.nlu.common.Yes

val FoodJoke : State = state(Parent) {
    onEntry {
        furhat.ask("Would you like to hear a joke?")
    }

    // If the user likes any of them
    onResponse<Yes>{
        furhat.say("Amazing here it comes.")
        goto(GetJoke)
    }

    // If not, ask if the user wants new ones
    onResponse<No>{
        furhat.say("Okay, no problem!")
        goto(Idle)
    }
}

val EvaluateJoke : State = state(Parent) {
    onEntry {
        furhat.ask("Did you like it?")
    }

    // If the user likes any of them
    onResponse<Yes>{
        furhat.say("Amazing!")
        goto(RepeatJoke)
    }

    // If not, ask if the user wants new ones
    onResponse<No>{
        furhat.say("Okay, I will try to make a better joke next time!")
        goto(RepeatJoke)
    }
}

val RepeatJoke : State = state(Parent) {
    onEntry {
        furhat.ask("Would you like to hear another joke?")
    }

    // If the user likes any of them
    onResponse<Yes>{
        furhat.say("Nice! I hope you like this one")
        goto(GetJoke)
    }

    // If not, ask if the user wants new ones
    onResponse<No>{
        furhat.say("Okay, no problem!")
        goto(Idle)
    }
}

val GetJoke : State = state(Parent) {
    onEntry {
        // Get responses
        val result = call(query( "food", "jokes/random")) as List<String>

        if (result.isEmpty()) {
            furhat.say("I could not connect to my brain, Im sorry!")
            goto(Idle)
        } else {
            furhat.say(result[0])
            goto(EvaluateJoke)
        }
    }
}
package furhatos.app.agent.flow.recipes

import furhatos.app.agent.flow.Parent
import furhatos.flow.kotlin.State
import furhatos.flow.kotlin.furhat
import furhatos.flow.kotlin.onResponse
import furhatos.flow.kotlin.state
import furhatos.nlu.common.No
import furhatos.nlu.common.Yes

val AcceptFromList: State = state(Parent) {
    onEntry {
        furhat.ask("Do you like any of them?")
    }

    // If the user likes any of them
    onResponse<Yes>{
        furhat.ask("Which one do you like?")
    }

    // If not, ask if the user wants new ones
    onResponse<No>{
        furhat.say("I will search for new ones.")
        terminate("")
    }

    // If not
    onResponse{
        // TODO recognize which one the user likes
        terminate(it.text)
    }
}
package furhatos.app.agent.flow.main

import furhatos.app.agent.current_user
import furhatos.app.agent.flow.Parent
import furhatos.flow.kotlin.State
import furhatos.flow.kotlin.furhat
import furhatos.flow.kotlin.onResponse
import furhatos.flow.kotlin.state
import furhatos.nlu.common.No
import furhatos.nlu.common.Yes

val personal_identification : State = state(Parent) {
    onEntry {
        furhat.say("""I'm Spoonie. I am a food recommender system who would like to help you achieve your goals. 
            Whether it would be improving your diet, learning new cooking skills or exploring new recipes. I'm there for you. 
            However, before we can start, we need to get to know each other better.""")
        furhat.ask("So ${current_user.name}, is it alright if I ask some questions? ")
    }

    onResponse<No> {
            furhat.say("That's alright. We'll do that another time.")
        }


    onResponse<Yes> {
        furhat.say("Great. Let's start")
        // goto
        goto(Idle)
    }
}
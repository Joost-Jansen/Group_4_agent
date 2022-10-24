package furhatos.app.agent.flow.main

import furhatos.app.agent.current_user
import furhatos.app.agent.dataManager
import furhatos.app.agent.flow.Parent
import furhatos.flow.kotlin.State
import furhatos.flow.kotlin.furhat
import furhatos.flow.kotlin.onResponse
import furhatos.flow.kotlin.state
import furhatos.nlu.common.No
import furhatos.nlu.common.Yes

val PersonIdentification : State = state(Parent) {
    init {
        furhat.say("""I'm Spoonie. I am a food recommender system who would like to help you achieve your goals. 
            Whether it would be improving your diet, learning new cooking skills or exploring new recipes. I'm there for you. 
            However, before we can start, we need to get to know each other better.""")
    }
    onEntry {
        furhat.ask("So ${current_user.name}, is it alright if I ask some questions?")
    }

    onResponse<No> {
            furhat.say("That's alright. We'll do that another time.")
            dataManager.writeUser(current_user)
            goto(Idle)
    }


    onResponse<Yes> {
        furhat.say("Great. Let's start")
        // Should ask personal information but not yet implemented therefore this:
        dataManager.writeUser(current_user)
        furhat.say("Now that I've identified you as ${current_user.name}. We will move on to the personal information module.")
        goto(PersonalInformation)
    }
}

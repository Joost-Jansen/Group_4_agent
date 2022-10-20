package furhatos.app.agent.flow.main

import furhatos.app.agent.flow.Parent
import furhatos.app.agent.flow.recipes.FoodJoke
import furhatos.app.agent.flow.recipes.Recommendation
import furhatos.flow.kotlin.State
import furhatos.flow.kotlin.furhat
import furhatos.flow.kotlin.onResponse
import furhatos.flow.kotlin.state
import furhatos.gestures.Gestures
import furhatos.nlu.Intent
import furhatos.nlu.common.No
import furhatos.nlu.common.Yes
import furhatos.util.Language

val DayPreference : State = state(Parent) {
    onEntry {
        random (
            { furhat.say("What did you eat today?") },
            { furhat.say("What did you eat for lunch?")}

        )
    }



    onResponse<Previous_meal> {
        furhat.say("Alright, I will tell you a joke.")
        goto(FoodJoke)
    }

    onResponse<Yes> {
        furhat.say("We will now go to the recommendation module.")
        goto(Recommendation)
    }
}

class Previous_meal : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("I had ", "nice to meet you")
    }
}
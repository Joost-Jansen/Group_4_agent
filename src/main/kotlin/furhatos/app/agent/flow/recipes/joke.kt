package furhatos.app.agent.flow.recipes

import furhatos.app.agent.current_user
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
        val text = "Now that we are done with that. "
        random(
            {furhat.ask(text + "Would you like to hear a joke?")},
            {furhat.ask(text + "Can I tell you a joke?")},
            {furhat.ask(text + "Are you interested in a joke?")}
        )
    }

    // If the user likes any of them
    onResponse<Yes>{
        random(
            {furhat.say("Amazing here it comes!")},
            {furhat.say("Cool, I hope you like it!")},
            {furhat.say("Alright!")}
        )
        goto(GetJoke)
    }

    // If not, ask if the user wants new ones
    onResponse<No>{
        random(
            {furhat.say("Okay, no problem!")},
            {furhat.say("That's fine!")}
        )
        current_user.last_step = "greeting"
        goto(Idle)
    }
}

val EvaluateJoke : State = state(Parent) {
    onEntry {
        random(
            {furhat.ask("Did you like it?")},
            {furhat.ask("And? Did you enjoy it?")},
            {furhat.ask("Was it any good?")}
        )
    }

    // If the user likes any of them
    onResponse<Yes>{
        random(
            {furhat.say("Amazing!")},
            {furhat.say("Perfect!")},
            {furhat.say("Nice!")}
        )
        goto(RepeatJoke)
    }

    // If not, ask if the user wants new ones
    onResponse<No>{
        random(
            {furhat.say("Okay, I will try to make a better joke next time!")},
            {furhat.say("Ah, that is unfortunate.")},
            {furhat.say("It is sad to hear my humor is not on your level!")}
        )
        goto(RepeatJoke)
    }
}

val RepeatJoke : State = state(Parent) {
    onEntry {
        random(
            {furhat.ask("Would you like to hear another joke?")},
            {furhat.ask("Interested in another one?")},
            {furhat.ask("Want to hear a new one?")}
        )
    }

    // If the user likes any of them
    onResponse<Yes>{
        random(
            {furhat.say("Amazing here it comes!")},
            {furhat.say("Cool, I hope you like it!")},
            {furhat.say("Nice! I hope you like this one!")}
        )
        goto(GetJoke)
    }

    // If not, ask if the user wants new ones
    onResponse<No>{
        random(
            {furhat.say("Okay, no problem!")},
            {furhat.say("That's fine!")}
        )
        current_user.last_step = "greeting"
        goto(Idle)
    }
}

val GetJoke : State = state(Parent) {
    onEntry {
        // Get responses
        val result = call(query( "food", "jokes/random")) as List<String>

        if (result.isEmpty()) {
            furhat.say("I could not connect to my brain, Im sorry! Try again later!")
            current_user.last_step = "greeting"
            goto(Idle)
        } else {
            furhat.say(result[0])
            goto(EvaluateJoke)
        }
    }
}
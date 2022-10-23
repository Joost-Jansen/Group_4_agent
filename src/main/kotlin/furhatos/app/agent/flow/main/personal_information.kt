package furhatos.app.agent.flow.main

import furhatos.app.agent.current_user
import furhatos.app.agent.flow.Parent
import furhatos.app.agent.nlu.*
import furhatos.flow.kotlin.State
import furhatos.flow.kotlin.furhat
import furhatos.flow.kotlin.onResponse
import furhatos.flow.kotlin.state
import furhatos.nlu.ListEntity
import furhatos.nlu.common.No
import furhatos.nlu.common.Yes

var diets_known = false
var allergies_known = false

val ChangePersonalInformation : State = state(Parent) {
    onEntry() {
        furhat.ask("What did I understand incorrectly?")
    }

    onResponse<No> {
        furhat.say("Alright, then I suppose that I remember everything correctly.")
        goto(EndPersonalInformation)
    }

    onResponse<RemovePersonalInformation> {
        furhat.say("Okay, so you are ${it.intent}")
        removeDiets(it.intent.diets)
        removeAllergies(it.intent.allergies)
        goto(CheckPersonalInformation)
    }

    onResponse<TellPersonalInformation> {
        // Repeat diets and allergies
        furhat.say("Ok, you are ${it.intent}")

        // Save in memory
        updateDiets(it.intent.diets)
        updateAllergies(it.intent.allergies)

        goto(CheckPersonalInformation)
    }
}

val PersonalInformation : State = state(ChangePersonalInformation) {
    onEntry {
        furhat.ask("Do you have any allergies or do you follow any specific diets?")
    }

    onResponse<No> {
        furhat.say("Great! Let's continue.")
        diets_known = true
        allergies_known = true
        goto(EndPersonalInformation)
    }

    onResponse<Yes> {
        furhat.askFor<TellPersonalInformation>("Could you tell me what they are?")
        reentry()
    }
}

val CheckPersonalInformation = state(Parent) {
    onEntry {
        when {
            !diets_known -> goto(RequestDiets)
            !allergies_known -> goto(RequestAllergies)
            else -> goto(ConfirmPersonalInformation)
        }
    }
}

val RequestDiets : State = state(ChangePersonalInformation) {
    onEntry() {
        furhat.ask("Do you follow any diets?")
    }

    onResponse<No> {
        furhat.say("Great!")
        diets_known = true
        goto(CheckPersonalInformation)
    }

    onResponse<Yes> {
        furhat.say("Which diets do you follow?")
        reentry()
    }

    onResponse<TellDiets> {
        furhat.say("Okay, you follow these diets: ${it.intent.diets}")
        updateDiets(it.intent.diets)
        goto(CheckPersonalInformation)
    }
}

val RequestAllergies : State = state(ChangePersonalInformation) {
    onEntry() {
        furhat.ask("Do you have any allergies?")
    }

    onResponse<No> {
        furhat.say("Great!")
        diets_known = true
        goto(ConfirmPersonalInformation)
    }

    onResponse<Yes> {
        furhat.say("What are you allergic to?")
        reentry()
    }

    onResponse<TellAllergies> {
        furhat.say("Okay, you follow these diets: ${it.intent.allergies}")
        updateAllergies(it.intent.allergies)
        goto(CheckPersonalInformation)
    }
}

val ConfirmPersonalInformation : State = state(ChangePersonalInformation) {
    onEntry() {
        var msg = "I can remember that "

        if (getAllergiesString() == null)
            msg += "you are not allergic to anything "
        else
            msg += getAllergiesString()

        if (getDietsString() == null)
            msg += "and you don't follow any diets"
        else
            msg += "and " + getDietsString()

        furhat.say(msg)
        furhat.ask("Is that correct?")
    }

    onResponse<Yes> {
        furhat.say("Great! Let's move on")
        goto(EndPersonalInformation)
    }

    onResponse<No> {
        goto(ChangePersonalInformation)
    }
}

val EndPersonalInformation : State = state(Parent) {
    onEntry() {
        goto(DayPreference)
    }
}

fun updateDiets(input: ListEntity<Diet>?) {
    input?.list?.forEach {
        current_user.diet.add(it.toString())
    }
    diets_known = true
}

fun updateAllergies(input: ListEntity<Allergy>?) {
    input?.list?.forEach {
        current_user.allergies.add(it.toString())
    }
    allergies_known = true
}

fun removeDiets(input: ListEntity<Diet>?) {
    input?.list?.forEach {
        current_user.diet.remove(it.toString())
    }
    diets_known = true
}

fun removeAllergies(input: ListEntity<Allergy>?) {
    input?.list?.forEach {
        current_user.allergies.remove(it.toString())
    }
    allergies_known = true
}

fun getDietsString(): String? {
    var str: String? = null
    if (current_user.diet.size > 0)
        str = "you follow the diets called ${current_user.diet.toString()} "

    return str
}

fun getAllergiesString(): String? {
    var str: String? = null
    if (current_user.allergies.size > 0)
        str = "you are allergic to ${current_user.allergies.toString()} "

    return str
}

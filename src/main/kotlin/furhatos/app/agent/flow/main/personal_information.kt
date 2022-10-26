package furhatos.app.agent.flow.main

import furhatos.app.agent.current_user
import furhatos.app.agent.flow.Parent
import furhatos.app.agent.nlu.*
import furhatos.flow.kotlin.*
import furhatos.nlu.ListEntity
import furhatos.nlu.common.No
import furhatos.nlu.common.Yes

var diets_known = false
var allergies_known = false

val HandlePersonalInformation : State = state(Parent) {
    onEntry() {
        current_user.last_step = "handle_information"
        random(
            {furhat.ask("What did I understand incorrectly?")},
            {furhat.ask("What did I forget?")},
            {furhat.ask("What did I get wrong?")}
        )
    }

    onResponse<No> {
        random(
            {furhat.say("Alright, then I suppose that I remember everything correctly.")},
            {furhat.say("Okay, I guess I remember everything then.")}
        )
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
        furhat.say("Alright, you are ${it.intent}")

        // Save in memory
        updateDiets(it.intent.diets)
        updateAllergies(it.intent.allergies)

        goto(CheckPersonalInformation)
    }

    onResponse<TellDiets> {
        furhat.say("I see, you follow the ${it.intent.diets} diet")

        updateDiets(it.intent.diets)
        goto(CheckPersonalInformation)
    }

    onResponse<TellAllergies> {
        furhat.say("Okay, you are allergic to ${it.intent.allergies}")
        updateAllergies(it.intent.allergies)
        goto(CheckPersonalInformation)
    }
}

val PersonalInformation : State = state(HandlePersonalInformation) {
    onEntry {
        current_user.last_step = "information"
        random(
            {goto(RequestDiets)},
            {goto(RequestAllergies)}
        )
    }
}

val CheckPersonalInformation = state(Parent) {
    onEntry {
        current_user.last_step = "check_information"
        when {
            !diets_known -> goto(RequestDiets)
            !allergies_known -> goto(RequestAllergies)
            else -> goto(ConfirmPersonalInformation)
        }
    }
}

val RequestDiets : State = state(HandlePersonalInformation) {
    onEntry() {
        current_user.last_step = "request_diet"
        random(
            {furhat.ask("Do you follow any diets?")},
            {furhat.ask("Are you on any diets?")}
        )
    }

    onResponse<No> {
        random(
            {furhat.say("Nice, that makes my life easy.")},
            {furhat.say("Good to know, thanks.")}
        )
        diets_known = true
        goto(CheckPersonalInformation)
    }

    onResponse<Yes> {
        val response = furhat.askFor<TellDiets>("Which diets do you follow?")
        if (response != null) {
            var msg = "Okay, so you follow the ${response.diets} diet"
            if (response.size() > 1) msg += "s"

            furhat.say(msg)
            updateDiets(response.diets)
        }
        goto(CheckPersonalInformation)
    }

    onPartialResponse<Yes> {
        raise(it, it.secondaryIntent)
    }
}

val RequestAllergies : State = state(HandlePersonalInformation) {
    onEntry() {
        current_user.last_step = "request_allergies"
        random(
            {furhat.ask("Do you have any allergies?")},
            {furhat.ask("Are you intolerant to anything?")}
        )
    }

    onResponse<No> {
        random(
            {furhat.say("Great!")},
            {furhat.say("Good for you!")}
        )
        allergies_known = true
        goto(ConfirmPersonalInformation)
    }

    onResponse<Yes> {
        val response = furhat.askFor<TellAllergies>("What are you allergic to?")
        if (response != null) {
            furhat.say("Alright, so you are allergic to ${response.allergies}")
            updateAllergies(response.allergies)
        }
        goto(CheckPersonalInformation)
    }

    onPartialResponse<Yes> {
        raise(it, it.secondaryIntent)
    }
}

val ConfirmPersonalInformation : State = state(HandlePersonalInformation) {
    onEntry() {
        current_user.last_step = "confirm_information"
        var msg = "I can remember that "

        msg += if (getAllergiesString() == null)
            "you are not allergic to anything "
        else
            getAllergiesString()

        msg += if (getDietsString() == null)
            "and you don't follow any diets"
        else
            "and " + getDietsString()

        furhat.say(msg)
        random(
            {furhat.ask("Is that correct?")},
            {furhat.ask("Am I right?")},
            {furhat.ask("Did I remember that correctly?")}
        )
    }

    onReentry {
        random(
            {furhat.ask("Is that correct?")},
            {furhat.ask("Am I right?")},
            {furhat.ask("Did I remember that correctly?")}
        )
    }

    onResponse<Yes> {
        furhat.say("Awesome! Let's move on")
        goto(EndPersonalInformation)
    }

    onResponse<No> {
        goto(HandlePersonalInformation)
    }
}

val EndPersonalInformation : State = state(Parent) {
    onEntry() {
        goto(Evaluation)
    }
}

fun updateDiets(input: ListEntity<Diet>?) {
    input?.list?.forEach {
        current_user.diet.add(it.alterDiet.toString())
    }
    diets_known = true
}

fun updateAllergies(input: ListEntity<Allergy>?) {
    input?.list?.forEach {
        current_user.allergies.add(it.alterAll.toString())
    }
    allergies_known = true
}

fun removeDiets(input: ListEntity<Diet>?) {
    input?.list?.forEach {
        current_user.diet.remove(it.alterDiet.toString())
    }
    diets_known = true
}

fun removeAllergies(input: ListEntity<Allergy>?) {
    input?.list?.forEach {
        current_user.allergies.remove(it.alterAll.toString())
    }
    allergies_known = true
}

fun getDietsString(): String? {
    var str: String? = null
    if (current_user.diet.size > 0)
        str = "you follow the ${listToString(current_user.diet)} diet"
        if (current_user.diet.size > 1) str += "s"

    return str
}

fun getAllergiesString(): String? {
    var str: String? = null
    if (current_user.allergies.size > 0)
        str = "you are allergic to ${listToString(current_user.allergies)} "

    return str
}

fun listToString(input: MutableList<String>): String {
    return when (input.size) {
        1 -> input[0]
        2 -> input[0] + " and " + input[1]
        else -> input[0] + " " + listToString(input.drop(1) as MutableList<String>)
    }
}

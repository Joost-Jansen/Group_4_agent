package furhatos.app.agent.flow.main

import furhatos.app.agent.current_user
import furhatos.app.agent.dataManager

import furhatos.app.agent.flow.recipes.GiveRecommendation
import furhatos.app.agent.flow.recipes.Recommendation
import furhatos.flow.kotlin.*

val Idle: State = state {

    init {
        when {
            users.count > 0 -> {
                furhat.attend(users.random)
                    goto(Greeting)
            }
            users.count == 0 && furhat.isVirtual() -> furhat.say("I can't see anyone. Add a virtual user please. ")
            users.count == 0 && !furhat.isVirtual() -> furhat.say("I can't see anyone. Step closer please. ")
        }
    }

    onEntry {
        dataManager.writeUser()
        furhat.attendNobody()
    }

    onUserEnter {
        furhat.attend(it)
        when (current_user.last_step) {
            "greeting" -> goto(Greeting)
            "handle_information" -> goto(HandlePersonalInformation)
            "information" -> goto(PersonalInformation)
            "check_information" -> goto(CheckPersonalInformation)
            "request_diet" -> goto(RequestDiets)
            "request_allergies" -> goto(RequestAllergies)
            "confirm_information" -> goto(ConfirmPersonalInformation)
            "identification" -> goto(PersonIdentification)
            "day_preference" -> goto(DayPreference)
            "ask_appitite" -> goto(AskAppitite)
            "ask_time" -> goto(AskTime)
            "ask_short" -> goto(AskShort)
            "ask_long" -> goto(AskLong)
            "ask_leftover" -> goto(AskLeftOver)
            "evaluation" -> goto(Evaluation)
            "positive_meal" -> goto(PositiveMealEvaluation)
            "negative_meal" -> goto(NegativeMealEvaluation)
            "recommendation" -> goto(Recommendation)
            "give_recommendation" -> goto(GiveRecommendation)
        }
    }
}

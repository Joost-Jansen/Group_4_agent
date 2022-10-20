package furhatos.app.agent.flow.main

import addMeal
import furhatos.app.agent.current_user
import furhatos.app.agent.flow.Parent
import furhatos.app.agent.flow.recipes.Recommendation
import furhatos.app.agent.flow.recipes.query
import furhatos.app.agent.nlu.Ingredients
import furhatos.flow.kotlin.State
import furhatos.flow.kotlin.furhat
import furhatos.flow.kotlin.onResponse
import furhatos.flow.kotlin.state
import furhatos.nlu.Intent
import furhatos.nlu.common.Yes
import furhatos.util.Language

class Previous_meal(
    val meal : Ingredients? = null
) : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf(
            "today i had @meal",
            "this morning i had @meal",
            "for lunch i had @ meal"
        )
    }
}

val DayPreference : State = state(Parent) {
    onEntry {
        print(current_user.meals)
        furhat.ask("What did you eat today?")
    }

    onResponse<Previous_meal> {
        val meal = call(query(it.intent.meal.toString(),"complexSearch", "recipes")) as ArrayList<String>
        val meal_ID = meal.first().toInt()

        addMeal(meal_ID, current_user.meals)
        print(current_user.meals)
        furhat.say("Thanks for sharing")
        goto(Recommendation)
    }

    onResponse<Yes> {
        furhat.say("We will now go to the recommendation module.")
        goto(Recommendation)
    }
}


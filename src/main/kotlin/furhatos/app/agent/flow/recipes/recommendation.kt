package furhatos.app.agent.flow.recipes

import furhatos.app.agent.current_user
import furhatos.app.agent.flow.Parent
import furhatos.app.agent.flow.main.DayPreference
import furhatos.app.agent.flow.main.Idle
import furhatos.app.agent.flow.memory.data.Cuisine
import furhatos.app.agent.flow.memory.data.Ingredient
import furhatos.app.agent.flow.memory.data.Meal
import furhatos.app.agent.userUpdates
import furhatos.flow.kotlin.State
import furhatos.flow.kotlin.furhat
import furhatos.flow.kotlin.onResponse
import furhatos.flow.kotlin.state
import furhatos.nlu.common.No
import furhatos.nlu.common.Yes
import java.time.LocalDate

val BASE_URL = "https://api.spoonacular.com" // Spoonacular API url
val API_KEY = "e9eeb0d76f024efcaf7cd32ae444c899" // Key to free account
val TIMEOUT = 5000 // 5 seconds
var recommendations: MutableList<Meal> = mutableListOf()

// Start state containing everything except the query to the API
val Recommendation : State = state(Parent) {
    onEntry {
        current_user.last_step = "recommendation"
        try {
            recommendations = call(query("recipes", "search")) as MutableList<Meal>
            if (recommendations.isEmpty()) {
                furhat.say("Sorry, I could not find a recipe. Let's go over your preferences again. " +
                        "It might help if you are a little bit less specific.")
                goto(DayPreference)
            } else {
                random(
                    {furhat.say("I have some ideas on which recipes you might like.")},
                    {furhat.say("I found some recipes for you.")},
                    {furhat.say("I have some recipes that match your preferences.")}
                )
                random(
                    {furhat.ask("Would you like to know?")},
                    {furhat.ask("Should I tell?")},
                    {furhat.ask("Are you curious?")}
                )
            }
        } catch(e: Exception) {
            furhat.say("I'm sorry. Something went wrong with finding a recipe.")
            val bool = furhat.askYN("Do you want to try again?")
            if(bool!!) {
               reentry()
            } else {
                current_user.last_step = "greeting"
                goto(Idle)
            }
        }
    }

    onResponse<Yes> {
        goto(GiveRecommendation)
    }

    onResponse<No> {
        furhat.say("Alright, I'll be available if you need me.")
        current_user.last_step = "greeting"
        goto(Idle)
    }
}

val GiveRecommendation = state(Parent) {
    onEntry {
        current_user.last_step = "give_recommendation"
        if (recommendations.isNotEmpty()) {
            val recipe = recommendations.first()
            print(recipe.javaClass.name)
            recommendations = if (recommendations.size >= 2) {
                recommendations.drop(1) as MutableList<Meal>
            } else {
                mutableListOf()
            }

            // Propose recipe
            furhat.say("I think you might like " + recipe.name)

            // Provide additional information
            val moreInfo = furhat.askYN("Would you like some more information?")
            if (moreInfo!! && moreInfo) {
                furhat.say(recipe.summary)
                furhat.say("It takes " + recipe.prepTime + " minutes to cook.")
            }

            goto(EvaluateRecommendation(recipe))
        } else {
            furhat.say("Unfortunately, I'm out of recommended recipes.")
            current_user.last_step = "greeting"
            goto(Idle)
        }
    }
}

fun EvaluateRecommendation(recipe: Meal) : State = state(Parent) {
    onEntry {
        random(
            {furhat.ask("Do you like it?")},
            {furhat.ask("Are you excited about this recipe?")},
            {furhat.ask("Will you enjoy this meal?")}
        )
    }

    onResponse<Yes> {
        furhat.say("Awesome!")
        recipe.last_selected = LocalDate.now().toString()
        recipe.likes += 2
        addRecipeToUser(recipe)
        goto(EndRecommendation)
    }

    onResponse<No> {
        val another = furhat.askYN("Too bad, would you like another recipe?")
        addRecipeToUser(recipe)
        userUpdates.updateMeal(-2, recipe, current_user)
        if (another!! && another) {
            if (recommendations.isEmpty()) {
                val genericRecommendation = furhat.askYN("Your demands are too specific, would you like a more generic ${current_user.preferred_meal_type}")
                if(genericRecommendation!! && genericRecommendation) {
                    furhat.say("Okay")
                    print(recommendations)
                    recommendations = call(query("recipes", "complexSearch")) as MutableList<Meal>
                    print(recommendations)
                    goto(GiveRecommendation)
                } else {
                    furhat.say("Okay, I'll be here if you need me.")
                    goto(Idle)
                }
            } else {
                goto(GiveRecommendation)
            }
        } else {
            furhat.say("Okay, I'll be here if you need me.")
            current_user.last_step = "greeting"
            goto(Idle)
        }
    }
}

val EndRecommendation : State = state(Parent) {
    onEntry {
        random(
            {furhat.say("Enjoy your meal and see you next time!")},
            {furhat.say("Bon appetite! See you soon.")}
        )
        current_user.last_step = "greeting"
        goto(Idle)
    }
}

fun addRecipeToUser(recipe: Meal){
    current_user.meals = userUpdates.addMeal(recipe, current_user.meals)
    for (i in recipe.ingredients){
        current_user.ingredients = userUpdates.addIngredient(Ingredient(i, 0), current_user.ingredients)
    }
    for (j in recipe.cuisines){
        current_user.cuisines = userUpdates.addCuisine(Cuisine(j, 0), current_user.cuisines)
    }
}

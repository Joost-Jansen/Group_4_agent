package furhatos.app.agent.flow.main

import Ingredient
import Meal
import furhatos.app.agent.current_user
import furhatos.app.agent.flow.Parent
import furhatos.app.agent.flow.recipes.queryHuggingFace
import furhatos.app.agent.nlu.negativeWildCardIntent
import furhatos.app.agent.nlu.postiveWildCardIntent
import furhatos.app.agent.nlu.wildCardIntent
import furhatos.app.agent.userUpdates
import furhatos.flow.kotlin.State
import furhatos.flow.kotlin.furhat
import furhatos.flow.kotlin.onResponse
import furhatos.flow.kotlin.state
import furhatos.gestures.Gestures
import furhatos.nlu.common.DontKnow
import furhatos.nlu.common.No
import furhatos.nlu.common.Yes

var lastMeal: Meal = Meal(-1, "", mutableListOf<String>() ,"", -1, "")

val Evaluation : State = state(Parent) {
    onReentry {
        val meal = userUpdates.findLastMeal(current_user.meals)
        if (meal !== null) {
            lastMeal = meal
            furhat.ask(
                random(
                    "Could you maybe specify?"
                )
            )
        } else {
            goto(DayPreference)
        }
    }

    onEntry {
        val meal = userUpdates.findLastMeal(current_user.meals)
        if (meal !== null) {
            lastMeal = meal
            furhat.ask(
                random(
                    "The last time I saw you, I recommended you to eat ${lastMeal.name} for ${lastMeal.course}. Did you like that meal?",
                    "Did you like that ${lastMeal.name} for ${lastMeal.course}? I recommended it you last time."
                )
            )
        } else {
            goto(DayPreference)
        }
    }


    onResponse<DontKnow> {
        furhat.say("That's alright. I have that sometimes too.")
        furhat.say("We'll talk about something else then.")
        goto(DayPreference)
    }

    onResponse<No> (priority = true) {
        furhat.gesture(Gestures.ExpressSad)
        furhat.say(
            random(
                "That's unfortunate.",
                "That's too bad."
            )
        )

        goto(negativeMealEvaluation)
    }

    onResponse<Yes> {
        furhat.gesture(Gestures.Smile)
        furhat.say(
            random(
                "It's always nice to hear that people love my recommendations.",
                "That's great to hear!"
            )
        )
        goto(positiveMealEvaluation)
    }

    onResponse<postiveWildCardIntent>{
        if (it.intent.textInput !== null) {
            val sentimentQuery = queryHuggingFace(it.intent.textInput.toString())
            var max: Float = "0.0".toFloat()
            var max_label = ""
            for (i in 0 until 3){
                val current = sentimentQuery.getJSONObject(i)
                val current_score = current.get("score").toString().toFloat()
                if (current_score >= max){
                    max = current_score
                    max_label = current.get("label").toString()
                }
            }
            var updateScore = 0
            when(max_label){
                "LABEL_0" ->{ 0
//                    furhat.say("You were negative but you liked the meal.So Score is positive: $updateScore")
                    furhat.say("Wait. you're saying that you liked the meal, but you sound kind of negative.")
//                    furhat.ask("Could you maybe specify?")
                    reentry()
                }
                "LABEL_1" ->{
                    updateScore = 5
                    userUpdates.updateLikes(lastMeal, current_user.meals, updateScore )
                    for (i in lastMeal.ingredients){
                        userUpdates.updateLikes(Ingredient(i, 0), current_user.ingredients, updateScore)
                    }
                    furhat.say("Alright. Great to hear that you liked the recipe. I'll keep that in mind.")
//                    furhat.say("I would go to Daypref now but stay for testing")
                    goto(DayPreference)
                }
                "LABEL_2" ->{
                    updateScore = (10*max).toInt()
                    userUpdates.updateLikes(lastMeal, current_user.meals, updateScore )
                    for (i in lastMeal.ingredients){
                        userUpdates.updateLikes(Ingredient(i, 0), current_user.ingredients, updateScore)
                    }
                    furhat.say("Alright. Great to hear that you liked the recipe. I'll keep that in mind.")
//                    furhat.say("I would go to Daypref now but stay for testing")
                    goto(DayPreference)
                }
            }
            reentry()
        }else{
            furhat.say(random("I didn't quite get that.",
                                        "I'm sorry.",
                                    ) + random(" Could you repeat that?",
                                                " What were you saying?"))
            reentry()
        }
    }

    onResponse<negativeWildCardIntent>{
        if (it.intent.textInput !== null) {
            val sentimentQuery = queryHuggingFace(it.intent.textInput.toString())
            var max: Float = "0.0".toFloat()
            var max_label = ""
            for (i in 0 until 3){
                val current = sentimentQuery.getJSONObject(i)
                val current_score = current.get("score").toString().toFloat()
                if (current_score >= max){
                    max = current_score
                    max_label = current.get("label").toString()
                }
            }
            var updateScore = 0
            when(max_label){
                "LABEL_0" ->{
                    updateScore = (-10*max).toInt()
                    userUpdates.updateLikes(lastMeal, current_user.meals, updateScore )
                    for (i in lastMeal.ingredients){
                        userUpdates.updateLikes(Ingredient(i, 0), current_user.ingredients, updateScore)
                    }
                    furhat.say("Too bad that you didn't like the ${lastMeal.name}. I'll try to remember that for your next meal.")
//                    furhat.say("I would go to Daypref now but stay for testing")
                    goto(DayPreference)
                }
                "LABEL_1" ->{
                    updateScore = -3
                    furhat.say("Too bad that you didn't like the ${lastMeal.name}. I'll try to remember that for your next meal.")
//                    furhat.say("I would go to Daypref now but stay for testing")
                    userUpdates.updateLikes(lastMeal, current_user.meals, updateScore )
                    for (i in lastMeal.ingredients){
                        userUpdates.updateLikes(Ingredient(i, 0), current_user.ingredients, updateScore)
                    }
                    goto(DayPreference)
                }
                "LABEL_2" ->{
                    furhat.say("Wait. you're saying that you didn't like the meal, but you sound kind of positive.")
                    reentry()

                }
            }
            reentry()
        }else{
            furhat.say(random("I didn't quite get that.",
                "I'm sorry.",
            ) + random("Could you repeat that?",
                "What were you saying?"))
            reentry()
        }
    }

    onResponse<wildCardIntent> (priority = false){
        if (it.intent.textInput !== null) {
            val sentimentQuery = queryHuggingFace(it.intent.textInput.toString())
            var max: Float = "0.0".toFloat()
            var max_label = ""
            for (i in 0 until 3){
                val current = sentimentQuery.getJSONObject(i)
                val current_score = current.get("score").toString().toFloat()
                if (current_score >= max){
                    max = current_score
                    max_label = current.get("label").toString()
                }
            }
            var updateScore = 0
            when(max_label){
                "LABEL_0" ->{
                    updateScore = (-10*max).toInt()
                    furhat.say("Too bad that you didn't like the ${lastMeal.name}. I'll try to remember that for your next meal.")
//                    furhat.say("I would go to Daypref now but stay for testing")
                    userUpdates.updateLikes(lastMeal, current_user.meals, updateScore )
                    for (i in lastMeal.ingredients){
                        userUpdates.updateLikes(Ingredient(i, 0), current_user.ingredients, updateScore)
                    }
                    goto(DayPreference)
                }
                "LABEL_1" ->{
                    furhat.say("You sound kind of neutral.")
                    reentry()
                }
                "LABEL_2" ->{
                    updateScore = (10*max).toInt()
                    furhat.say("Alright. Great to hear that you liked the recipe. I'll keep that in mind.")
                    furhat.say("I would go to Daypref now but stay for testing")
                    userUpdates.updateLikes(lastMeal, current_user.meals, updateScore )
                    for (i in lastMeal.ingredients){
                        userUpdates.updateLikes(Ingredient(i, 0), current_user.ingredients, updateScore)
                    }
                    goto(DayPreference)
                }
            }
            reentry()
        }else{
            furhat.say(random("I didn't quite get that.",
                "I'm sorry.",
            ) + random("Could you repeat that?",
                "What were you saying?"))
            reentry()
        }
    }
}



//
//    onResponse<negativeFlavourMeal> {
//        print(it.intent.flavours)
//        val flavours = it.intent.flavours
//        if (flavours !== null)  {
//            furhat.say(
//                random(
//                    "Alright, I get that you find the ${lastMeal.name} ${flavours}. I'll keep that in mind for the next time",
//                    "Too bad that ${lastMeal.name} was ${flavours}. I'll try to remember that for your next meal."
//                )
//            )
//        } else {
//            furhat.say("Too bad that you didn't like the ${lastMeal.name}. I'll try to remember that for your next meal.")
//        }
//        furhat.gesture(Gestures.Blink)
//
//        // update likes of meal
//        lastMeal.likes -= 2
//        userUpdates.updateLikes(lastMeal, current_user.meals, lastMeal.likes)
//        goto(DayPreference)
//    }
//
//
//    onResponse<positiveFlavourMeal> {
//        if(it.intent.flavours !== null){
//            furhat.say("Great to hear that ${lastMeal.name} were ${it.intent.flavours}. I'll keep that in mind for the next time")
//
//        }
////        else if(it.intent.ingredients != null){
////            furhat.say("Great to hear that ${lastMeal.name} had such nice ingredients. I'll keep that in mind for the next time")
////        }
//        else{
//            furhat.say("It's always nice to hear that people love my recommendations. I'll try to remember that for your next meal.")
//        }
//
//        lastMeal.likes += 2
//        userUpdates.updateLikes(lastMeal, current_user.meals, lastMeal.likes)
//        furhat.gesture(Gestures.Blink)
//        goto(DayPreference)
//    }
//
//    onNoResponse { reentry() }
//}
//
val positiveMealEvaluation : State = state(Evaluation){
    onEntry {
        furhat.ask(
            random(
                "What did you like about the meal?",
                "What did you like about the ${lastMeal.name}?"
            )
        )
    }

    onResponse<wildCardIntent>{
        if (it.intent.textInput !== null) {
            val sentimentQuery = queryHuggingFace(it.intent.textInput.toString())
            var max: Float = "0.0".toFloat()
            var max_label = ""
            for (i in 0 until 3){
                val current = sentimentQuery.getJSONObject(i)
                val current_score = current.get("score").toString().toFloat()
                if (current_score >= max){
                    max = current_score
                    max_label = current.get("label").toString()
                }
            }
            var updateScore = 0
            when(max_label){
                "LABEL_0" ->{
                    updateScore = (5*max).toInt()
                    furhat.say("You were negative but you liked the meal.So Score is positive: $updateScore")
                }
                "LABEL_1" ->{
                    updateScore = 5
                    furhat.say("You were neutral. Score: $updateScore")
                }
                "LABEL_2" ->{
                    updateScore = (10*max).toInt()
                    furhat.say("You were positive. Score: $updateScore")

                }
            }
            userUpdates.updateLikes(lastMeal, current_user.meals, updateScore )
            for (i in lastMeal.ingredients){
                userUpdates.updateLikes(Ingredient(i, 0), current_user.ingredients, updateScore)
            }
            goto(Evaluation)
        }else{
            furhat.say("I thought you used the wildcard. But nothing was there")
            goto(Evaluation)
        }
    }

    onResponse<DontKnow> {
        furhat.say("That's alright. I have that sometimes too.")
        furhat.say("We'll talk about something else then.")
        userUpdates.updateLikes(lastMeal, current_user.meals, -1)
//        goto(DayPreference)
        goto(Evaluation)
    }

}

val negativeMealEvaluation : State = state(Evaluation){
    onEntry {
        random(
            furhat.ask("What didn't you like about the meal?"),
            furhat.ask("What didn't you like about the ${lastMeal.name}?")
        )

    }

    onResponse<wildCardIntent>{
        if (it.intent.textInput !== null) {
            val sentimentQuery = queryHuggingFace(it.intent.textInput.toString())
            var max: Float = "0.0".toFloat()
            var max_label = ""
            for (i in 0 until 3){
                val current = sentimentQuery.getJSONObject(i)
                val current_score = current.get("score").toString().toFloat()
                if (current_score >= max){
                    max = current_score
                    max_label = current.get("label").toString()
                }
            }
            var updateScore = 0
            when(max_label){
                "LABEL_0" ->{
                    updateScore = (-10*max).toInt()
                    furhat.say("You were negative. Score: $updateScore")
                }
                "LABEL_1" ->{
                    updateScore = -5
                    furhat.say("You were neutral. Score: $updateScore")
                }
                "LABEL_2" ->{
                    updateScore = (-5*max).toInt()
                    furhat.say("You were positive but you didn't like the meal. Score: $updateScore")

                }
            }
            userUpdates.updateLikes(lastMeal, current_user.meals, updateScore )
            for (i in lastMeal.ingredients){
                userUpdates.updateLikes(Ingredient(i, 0), current_user.ingredients, updateScore)
            }
            goto(Evaluation)
        }else{
            furhat.say("I thought you used the wildcard. But nothing was there")
            goto(Evaluation)
        }
    }

    onResponse<DontKnow> {
        furhat.say("That's alright. I have that sometimes too.")
        furhat.say("We'll talk about something else then.")
        userUpdates.updateLikes(lastMeal, current_user.meals, -1)
        for (i in lastMeal.ingredients){
            userUpdates.updateLikes(Ingredient(i, 0), current_user.ingredients, -1)
        }
        goto(Evaluation)
    }

}


package furhatos.app.agent.flow.main

import furhatos.app.agent.current_user
import furhatos.app.agent.flow.memory.data.Meal
import furhatos.app.agent.flow.memory.data.Ingredient
import furhatos.app.agent.flow.Parent
import furhatos.app.agent.flow.getCurrentEmotion
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
            println("checking emotion")
            checkEmotion()
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
                    random ("Did you like that ${lastMeal.name} for ${lastMeal.course}?", "Did you enjoy that ${lastMeal.name} for ${lastMeal.course}?") +
                    random (" I recommended it to you last time.", " I suggested it to you last time")
                )
            )
            println("checking emotion")
            checkEmotion()
        } else {
            goto(DayPreference)
        }
    }


    onResponse<DontKnow> {
        furhat.say("That's alright. I have that sometimes too.")
        furhat.say("Let's talk about what you would like to eat today.")
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
        checkEmotion()
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
        checkEmotion()
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
                    furhat.say(random("Wait.", "I didn't quite get that.", "Perhaps I misunderstood.") +
                            " You're saying that you liked the meal, but you sound kind of negative.")
//                    furhat.ask("Could you maybe specify?")
                    reentry()
                }
                "LABEL_1" ->{
                    updateScore = 5
                    updateMeal(updateScore)
                    furhat.say(random("Allright.","Nice!", "") +
                                   random(" Great to hear that you liked the recipe.", " It's wonderful to hear you enjoy the recipe.") +
                                   random(" I'll keep that in mind.", " I'll remember that for the next time.")
                    )

                    goto(DayPreference)
                }
                "LABEL_2" ->{
                    updateScore = (10*max).toInt()
                    updateMeal(updateScore)
                    furhat.say(random("Allright.","Nice!", "") +
                            random(" Great to hear that you liked the recipe.", " It's wonderful to hear you enjoy the recipe.") +
                            random(" I'll keep that in mind.", " I'll remember that for the next time.")
                    )
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
        checkEmotion()
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
                    furhat.say(random("Too bad that you didn't like the ${lastMeal.name}.", "It's so unfortunate you didn't like the ${lastMeal.name}.") +
                            random(" I'll try to remember that for your next meal.", " I'll keep that in mind for your next recommendation")
                    )
                    updateMeal(updateScore)
                    goto(DayPreference)
                }
                "LABEL_1" ->{
                    updateScore = -3
                    furhat.say(random("Too bad that you didn't like the ${lastMeal.name}.", "It's so unfortunate you didn't like the ${lastMeal.name}.") +
                            random(" I'll try to remember that for your next meal.", " I'll keep that in mind for your next recommendation")
                    )
                    updateMeal(updateScore)
                    goto(DayPreference)
                }
                "LABEL_2" ->{
                    furhat.say(random("Wait.", "I didn't quite get that.", "Perhaps I misunderstood.") +
                            " You're saying that you didn't like the meal, but you sound kind of positive.")
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
        checkEmotion()
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
                    furhat.say(random("Too bad that you didn't like the ${lastMeal.name}.", "It's so unfortunate you didn't like the ${lastMeal.name}.") +
                            random(" I'll try to remember that for your next meal.", " I'll keep that in mind for your next recommendation")
                    )
                    updateMeal(updateScore)
                    goto(DayPreference)
                }
                "LABEL_1" ->{
                    furhat.say(random("I didn't quite get your opinion ",
                        "I didn't entirely understand your point of view"
                    ) )
                    reentry()
                }
                "LABEL_2" ->{
                    updateScore = (10*max).toInt()
                    furhat.say(random("Allright.","Nice!", "") +
                            random(" Great to hear that you liked the recipe.", " It's wonderful to hear you enjoy the recipe.") +
                            random(" I'll keep that in mind.", " I'll remember that for the next time.")
                    )
                    updateMeal(updateScore)
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
val positiveMealEvaluation : State = state(Evaluation){
    onEntry {
        furhat.ask(
            random(
                "What did you like about the meal?",
                "What did you like about the ${lastMeal.name}?"
            )
        )
        checkEmotion()
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
                    furhat.say(random("Wait.", "I didn't quite get that.", "Perhaps I misunderstood.") +
                            " You're saying that you didn't like the meal, but you sound kind of positive.")
                    reentry()
                }
                "LABEL_1" -> {
                    updateScore = 5
                    furhat.say(
                        random("Allright.", "Nice!", "") +
                                random(
                                    " Great to hear that you liked the recipe.",
                                    " It's wonderful to hear you enjoy the recipe."
                                ) +
                                random(" I'll keep that in mind.", "I'll remember that for the next time.")
                    )
                    updateMeal(updateScore)
                }
                "LABEL_2" ->{
                    updateScore = (10*max).toInt()
                        furhat.say(random("Allright.","Nice!", "") +
                                random(" Great to hear that you liked the recipe.", " It's wonderful to hear you enjoy the recipe.") +
                                random(" I'll keep that in mind.", " I'll remember that for the next time.")
                        )
                    updateMeal(updateScore)
                }
            }
        }else{
            furhat.say(random("I didn't quite get that.",
                "I'm sorry.",
            ) + random("Could you repeat that?",
                "What were you saying?"))
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
        checkEmotion()
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
                    furhat.say(random("Too bad that you didn't like the ${lastMeal.name}.", "It's so unfortunate you didn't like the ${lastMeal.name}.") +
                            random(" I'll try to remember that for your next meal.", " I'll keep that in mind for your next recommendation")
                    )
                    updateMeal(updateScore)
                    goto(DayPreference)
                }
                "LABEL_1" ->{
                    updateScore = -5
                    furhat.say(random("Too bad that you didn't like the ${lastMeal.name}.", "It's so unfortunate you didn't like the ${lastMeal.name}.") +
                            random(" I'll try to remember that for your next meal.", " I'll keep that in mind for your next recommendation")
                    )
                    updateMeal(updateScore)
                    goto(DayPreference)
                }
                "LABEL_2" ->{
                    updateScore = (-5*max).toInt()
                    furhat.say(random("Wait.", "I didn't quite get that.", "Perhaps I misunderstood.") +
                            " You're saying that you didn't like the meal, but you sound kind of positive.")
                    reentry()
                }
            }
        }else{
            furhat.say(random("I didn't quite get that.",
                "I'm sorry.",
            ) + random("Could you repeat that?",
                "What were you saying?"))
            goto(Evaluation)
        }
    }

    onResponse<DontKnow> {
        furhat.say("That's alright. I have that sometimes too.")
        furhat.say("We'll talk about something else then.")
        updateMeal(-1)
        goto(Evaluation)
    }

}

fun checkEmotion(){
    val emotion = getCurrentEmotion().get("emotion").toString()
    var updateScore: Int = 0
    when (emotion){"Neutral"-> {
            updateScore = 0
        }
        "Happy", "Surprise" -> {
            updateScore = 1
        }
        "Sad", "Anger", "Disgust", "Fear",
        "Contempt",  -> {
            updateScore = -1
        }
        else -> {
        updateScore = 0
    }
    }
    println("updated score based on emotion: " .plus(emotion) + ", score: ".plus(updateScore))
    updateMeal(updateScore)
}

fun updateMeal(updateScore: Int){
    userUpdates.updateLikes(lastMeal, current_user.meals, updateScore )
    for (i in lastMeal.ingredients){
        userUpdates.updateLikes(Ingredient(i, 0), current_user.ingredients, updateScore)
    }
}

package furhatos.app.agent.flow.main

import furhatos.app.agent.current_user
import furhatos.app.agent.flow.Parent
import furhatos.app.agent.flow.getCurrentEmotion
import furhatos.app.agent.flow.memory.data.Meal
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
import java.time.LocalDate


var lastMeal: Meal = Meal(-1, "", mutableListOf<String>() , mutableListOf<String>(), "",-1, LocalDate.MIN.toString()o, 0, "")

val Evaluation : State = state(Parent) {
    onReentry {
        val meal = userUpdates.findLastMeal(current_user.meals)
        if (meal !== null && meal.last_selected !== LocalDate.MIN.toString()) {
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
        current_user.last_step = "evaluation"
        val meal = userUpdates.findLastMeal(current_user.meals)
        if (meal !== null && meal.last_selected !== LocalDate.MIN.toString()) {
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
            furhat.say("I have not yet recommended you a meal. Let me find one for you!")
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

        goto(NegativeMealEvaluation)
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
        goto(PositiveMealEvaluation)
    }

    onResponse<postiveWildCardIntent>{
        checkEmotion()
        if (it.intent.textInput !== null) {
            val p = getSentiment(it.intent.textInput.toString())
            val max = p.first
            when(p.second){
                "LABEL_0" ->{

                    furhat.say(random("Wait.", "I didn't quite get that.", "Perhaps I misunderstood.") +
                            " You're saying that you liked the meal, but you sound kind of negative.")
                    reentry()
                }
                "LABEL_1" ->{
                    val updateScore = 5
                    userUpdates.updateMeal(updateScore, lastMeal, current_user)
                    furhat.say(random("Alright.","Nice!", "") +
                                   random(" Great to hear that you liked the recipe.", " It's wonderful to hear you enjoy the recipe.") +
                                   random(" I'll keep that in mind.", " I'll remember that for the next time.")
                    )

                    goto(DayPreference)
                }
                "LABEL_2" ->{
                    val updateScore = (10*max).toInt()
                    userUpdates.updateMeal(updateScore, lastMeal, current_user)
                    furhat.say(random("Alright.","Nice!", "") +
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
            val p = getSentiment(it.intent.textInput.toString())
            val max = p.first
            when(p.second){
                "LABEL_0" ->{
                    val updateScore = (-10*max).toInt()
                    furhat.say(random("Too bad that you didn't like the ${lastMeal.name}.", "It's so unfortunate you didn't like the ${lastMeal.name}.") +
                            random(" I'll try to remember that for your next meal.", " I'll keep that in mind for your next recommendation")
                    )
                    userUpdates.updateMeal(updateScore, lastMeal, current_user)
                    goto(DayPreference)
                }
                "LABEL_1" ->{
                    val updateScore = -3
                    furhat.say(random("Too bad that you didn't like the ${lastMeal.name}.", "It's so unfortunate you didn't like the ${lastMeal.name}.") +
                            random(" I'll try to remember that for your next meal.", " I'll keep that in mind for your next recommendation")
                    )
                    userUpdates.updateMeal(updateScore, lastMeal, current_user)
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
            val p = getSentiment(it.intent.textInput.toString())
            val max = p.first
            when(p.second){
                "LABEL_0" ->{
                    val updateScore = (-10*max).toInt()
                    furhat.say(random("Too bad that you didn't like the ${lastMeal.name}.", "It's so unfortunate you didn't like the ${lastMeal.name}.") +
                            random(" I'll try to remember that for your next meal.", " I'll keep that in mind for your next recommendation")
                    )
                    userUpdates.updateMeal(updateScore, lastMeal, current_user)
                    goto(DayPreference)
                }
                "LABEL_1" ->{
                    furhat.say(random("I didn't quite get your opinion ",
                        "I didn't entirely understand your point of view"
                    ) )
                    reentry()
                }
                "LABEL_2" ->{
                    val updateScore = (10*max).toInt()
                    furhat.say(random("Alright.","Nice!", "") +
                            random(" Great to hear that you liked the recipe.", " It's wonderful to hear you enjoy the recipe.") +
                            random(" I'll keep that in mind.", " I'll remember that for the next time.")
                    )
                    userUpdates.updateMeal(updateScore, lastMeal, current_user)
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
val PositiveMealEvaluation : State = state(Evaluation){
    onEntry {
        current_user.last_step = "positive_meal"
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
            val p = getSentiment(it.intent.textInput.toString())
            val max = p.first
            when(p.second){
                "LABEL_0" ->{
                    furhat.say(random("Wait.", "I didn't quite get that.", "Perhaps I misunderstood.") +
                            " You're saying that you didn't like the meal, but you sound kind of positive.")
                    reentry()
                }
                "LABEL_1" -> {
                    val updateScore = 5
                    furhat.say(
                        random("Alright.", "Nice!", "") +
                                random(
                                    " Great to hear that you liked the recipe.",
                                    " It's wonderful to hear you enjoy the recipe."
                                ) +
                                random(" I'll keep that in mind.", "I'll remember that for the next time.")
                    )
                    userUpdates.updateMeal(updateScore, lastMeal, current_user)
                }
                "LABEL_2" ->{
                    val updateScore = (10*max).toInt()
                        furhat.say(random("Alright.","Nice!", "") +
                                random(" Great to hear that you liked the recipe.", " It's wonderful to hear you enjoy the recipe.") +
                                random(" I'll keep that in mind.", " I'll remember that for the next time.")
                        )
                    userUpdates.updateMeal(updateScore, lastMeal, current_user)
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

val NegativeMealEvaluation : State = state(Evaluation){
    onEntry {
        current_user.last_step = "negative_meal"
        random(
            furhat.ask("What didn't you like about the meal?"),
            furhat.ask("What didn't you like about the ${lastMeal.name}?")
        )
        checkEmotion()
    }

    onResponse<wildCardIntent>{
        if (it.intent.textInput !== null) {
            val p = getSentiment(it.intent.textInput.toString())
            val max = p.first
            when(p.second){
                "LABEL_0" ->{
                    val updateScore = (-10*max).toInt()
                    furhat.say(random("Too bad that you didn't like the ${lastMeal.name}.", "It's so unfortunate you didn't like the ${lastMeal.name}.") +
                            random(" I'll try to remember that for your next meal.", " I'll keep that in mind for your next recommendation")
                    )
                    userUpdates.updateMeal(updateScore, lastMeal, current_user)
                    goto(DayPreference)
                }
                "LABEL_1" ->{
                    val updateScore = -5
                    furhat.say(random("Too bad that you didn't like the ${lastMeal.name}.", "It's so unfortunate you didn't like the ${lastMeal.name}.") +
                            random(" I'll try to remember that for your next meal.", " I'll keep that in mind for your next recommendation")
                    )
                    userUpdates.updateMeal(updateScore, lastMeal, current_user)
                    goto(DayPreference)
                }
                "LABEL_2" ->{
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
        userUpdates.updateMeal(-1, lastMeal, current_user)
        goto(Evaluation)
    }

}

fun getSentiment(input: String): Pair<Float,String> {
    val sentimentQuery = queryHuggingFace(input)
    var max: Float = "0.0".toFloat()
    var maxLabel = ""
    for (i in 0 until 3){
        val current = sentimentQuery.getJSONObject(i)
        val currentScore = current.get("score").toString().toFloat()
        if (currentScore >= max){
            max = currentScore
            maxLabel = current.get("label").toString()
        }
    }
    return Pair(max, maxLabel)
}

fun checkEmotion(){
    val emotion = getCurrentEmotion().get("emotion").toString()
    val updateScore = when (emotion){"Neutral"-> {
        0
    }
        "Happy", "Surprise" -> {
            1
        }
        "Sad", "Anger", "Disgust", "Fear",
        "Contempt",  -> {
            -1
        }
        else -> {
            0
        }
    }
    println("updated score based on emotion: " .plus(emotion) + ", score: ".plus(updateScore))
    userUpdates.updateMeal(updateScore, lastMeal, current_user)
}



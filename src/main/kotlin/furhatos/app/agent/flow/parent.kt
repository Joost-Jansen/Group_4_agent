package furhatos.app.agent.flow

import furhatos.app.agent.current_user
import furhatos.app.agent.dataManager
import furhatos.app.agent.flow.main.Greeting
import furhatos.app.agent.flow.main.Idle
import furhatos.app.agent.nlu.WrongPerson
import furhatos.flow.kotlin.*
import furhatos.gestures.Gestures
import furhatos.records.Location
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import kotlin.concurrent.thread

val Parent: State = state {
    var current_emotion: JSONObject
    var first_x: Int = -1
    var first_y: Int = -1
    init {
        thread {
            var first_call = getCurrentEmotion()
            if(first_call.has("emotion")) {
                while(true) {
                    Thread.sleep(500)
                    current_emotion = getCurrentEmotion()
                    if(current_emotion.has("emotion") && current_emotion.has("x") && current_emotion.has("y")) {
                        if(first_x < 0) {
                            try {
                                first_x = (current_emotion.get("x") as String).toInt()
                                print("x = $first_x")
                            } catch (e: Exception) {
                                print(e)
                            }
                        }
                        if(first_y < 0) {
                            try {
                                first_y = (current_emotion.get("y") as String).toInt()
                                print("y = $first_y")
                            } catch (e: Exception) {
                                print(e)
                            }
                        }
                        doGesture(first_x, first_y, current_emotion, furhat)
                    } else {
                        break
                    }
                }
            }
        }
    }

    onUserLeave(instant = true) {
        dataManager.writeUser(current_user)
        when {
            users.count == 0 -> goto(Idle)
            it == users.current -> furhat.attend(users.other)
        }
    }
    onResponse<WrongPerson> {
        furhat.say("sorry, I must have misunderstood.")
        goto(Greeting)
    }
    onUserEnter(instant = true) {
        furhat.glance(it)
    }
}
fun getCurrentEmotion(): JSONObject {
    val url = URL("http://localhost:5000")
    try {
        val connection = url.openConnection()
        var emotion: JSONObject? = null
        BufferedReader(InputStreamReader(connection.getInputStream())).use { inp ->
            var line: String?
            while (inp.readLine().also { line = it } != null) {
                val obj = JSONObject(line.toString())
                emotion = obj
            }
        }
        return emotion!!
    } catch (e: Exception){
        println("NO CONNECTION TO CAMERA")
        println("Did you start up the flask camera service?")
        return JSONObject()
    }

}
fun doGesture(first_x: Int, first_y: Int, emotion: JSONObject, furhat: Furhat) {
    when (emotion.get("emotion")) {
        "Happy" -> furhat.gesture(Gestures.BigSmile(strength = 2.0))
    }
    val divide = 1000
//    println("-----")
//    println((first_x - (emotion.get("x") as String).toInt()).toDouble() / divide)
//    println((first_y - (emotion.get("y") as String).toInt()).toDouble() / divide)
//    println("-----")
    val location = Location(((first_x - (emotion.get("x") as String).toInt()).toDouble() / divide), ((first_y - (emotion.get("y") as String).toInt()).toDouble() / divide), 1.0)
//    println(location)
    furhat.attend(location)
}

package com.alien.testlogsgenerator

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class LogStarter(
    private val logOptions: LogOptions = LogOptions(),
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
)
{
    fun start() {
        coroutineScope.launch {
            var counter = 1
            if (logOptions.shouldRepeat) {
                while (true) {
                    delay(logOptions.repeatTimeout)
                    getLog(logOptions, counter)
                    counter++
                }
            } else {
                getLog(logOptions, counter)
            }
        }
    }

    private fun getLog (logOptions: LogOptions, counter: Int) {
        val logLevel: Int by lazy {
            if (logOptions.logLevel.levelInt == -1) {
                (2..7).random()
            } else {
                logOptions.logLevel.levelInt
            }
        }
        Log.println(
            logLevel,
            logOptions.tag,
            getMessage(
                logOptions.messageType,
                logOptions.randomMessageLength,
                logOptions.customMessage
            ) + if (logOptions.shouldAddMessageCounter) " $counter" else ""
        )
    }

    private fun getMessageByType(messageType: MessageType): (Int, String) -> String {
        val arrayOfFunctions: Array<(messageLength: Int, customMessage: String) -> String> = arrayOf(
            {
                    _,_ -> JSON
            },
            {
                    _,customMessage -> customMessage
            },
            {
                    messageLength,_ ->  getRandomString(messageLength)

            },
            {
                    _,_ ->
                val exception = Exception()
                Log.getStackTraceString(exception)
            }
        )

        return if (messageType.value == 0) {
            arrayOfFunctions.random()
        } else {
            arrayOfFunctions[messageType.value-1]
        }
    }

    private fun getMessage(messageType: MessageType, messageLength: Int, customMessage: String):String{
        return getMessageByType(messageType)(messageLength,customMessage)
    }
}

fun getRandomString(length: Int, spaceProbability: Double = 0.2) : String {
    val charset = "ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz0123456789"
    return (1..length)
        .map { if (Random.nextDouble() < spaceProbability) ' ' else charset.random() }
        .joinToString("")
}

const val JSON = "{\"random\":\"31\",\"randomfloat\":\"100.997\",\"bool\":\"true\",\"date\":\"1991-02-16\",\"regEx\":\"hellooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooworld\",\"enum\":\"generator\",\"firstname\":\"Lacie\",\"lastname\":\"Codding\",\"city\":\"Hiroshima\",\"country\":\"NetherlandsAntilles\",\"countryCode\":\"MN\",\"emailusescurrentdata\":\"Lacie.Codding@gmail.com\",\"emailfromexpression\":\"Lacie.Codding@yopmail.com\",\"array\":[\"Raf\",\"Ursulina\",\"Darci\",\"Vere\",\"Sharai\"],\"arrayofobjects\":[{\"index\":\"0\",\"indexstartat5\":\"5\"},{\"index\":\"1\",\"indexstartat5\":\"6\"},{\"index\":\"2\",\"indexstartat5\":\"7\"}],\"Randa\":{\"age\":\"81\"}}"
package com.alien.testlogsgenerator

import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

data class LogOptions(
    val logLevel: LogLevel = LogLevel.RANDOM,
    val shouldRepeat: Boolean = true,
    val repeatTimeout: Duration = 100.toDuration(DurationUnit.MILLISECONDS),
    val messageType: MessageType = MessageType.RANDOM,
    val customMessage: String = "Unspecified message string",
    val shouldAddMessageCounter: Boolean = true,
    val randomMessageLength: Int = 10,
    val tag: String = "UnspecifiedTag"
)

enum class MessageType(val value: Int) {
    JSON(1),
    STRING(2),
    RANDOM_STRING(3),
    STACK_TRACE(4),
    RANDOM(0)
}

enum class LogLevel(val levelInt: Int) {
    ASSERT(7),
    DEBUG(3),
    ERROR(6),
    INFO(4),
    VERBOSE(2),
    WARN(5),
    RANDOM(-1)
}
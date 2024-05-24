package com.alien.testlogsgenerator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alien.testlogsgenerator.ui.theme.TestLogsGeneratorTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TestLogsGeneratorTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        verticalArrangement = Arrangement.Top
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        SpecialActionButton(SpecialActionType.ANR)
                        SpecialActionButton(SpecialActionType.CRASH)
                        SpecialActionButton(SpecialActionType.NPE)
                        SpecialActionButton(SpecialActionType.NON_FATAL)
                    }
                }
            }
        }


        LogStarter(
            LogOptions(
            logLevel = LogLevel.RANDOM,
            messageType = MessageType.RANDOM_STRING,
            repeatTimeout = 20.toDuration(DurationUnit.MILLISECONDS),
            shouldRepeat = true,
            randomMessageLength = 300
        )
        ).start()
        LogStarter(
            LogOptions(
            logLevel = LogLevel.RANDOM,
            messageType = MessageType.JSON,
            repeatTimeout = 200.toDuration(DurationUnit.MILLISECONDS),
            shouldRepeat = true,
        )
        ).start()
        LogStarter(
            LogOptions(
            logLevel = LogLevel.RANDOM,
            messageType = MessageType.STACK_TRACE,
            repeatTimeout = 10000.toDuration(DurationUnit.MILLISECONDS),
            shouldRepeat = true,
        )
        ).start()
        LogStarter(
            LogOptions(
            logLevel = LogLevel.ERROR,
            messageType = MessageType.STRING,
            repeatTimeout = 2000.toDuration(DurationUnit.MILLISECONDS),
            shouldRepeat = true,
            tag = "ExampleCustomTag1",
            customMessage = "Custom message for ExampleCustomTag1"
        )
        ).start()
        LogStarter(
            LogOptions(
            logLevel = LogLevel.VERBOSE,
            messageType = MessageType.STRING,
            repeatTimeout = 2000.toDuration(DurationUnit.MILLISECONDS),
            shouldRepeat = true,
            tag = "ExampleCustomTag2",
            customMessage = "ExampleCustomTag2 custom message"
        )
        ).start()
    }
}


@Composable
fun SpecialActionButton (specialActionType: SpecialActionType) {
    Button(
        onClick = {
            SpecialActionsExecutor().performAction(specialActionType)
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Text(
            text = specialActionType.actionName,
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
}

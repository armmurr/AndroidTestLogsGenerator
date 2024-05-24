package com.alien.testlogsgenerator

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel

class LogGeneratorViewModel : ViewModel() {
    private val _startedJobs = mutableStateListOf<Pair<Job?, String>>()
    val startedJobs: List<Pair<Job?, String>> = _startedJobs

    fun startLogGenerator(logOptions: LogOptions) {
        viewModelScope.launch {
            val job = LogStarter(logOptions, this).start()
            if (!logOptions.shouldRepeat) {
                delay(300.toDuration(DurationUnit.MILLISECONDS))
                job.cancel()
            } else {
                _startedJobs.add(
                    Pair(
                        job,
                        "${logOptions.logLevel.name} " +
                                "${logOptions.messageType} " +
                                "${logOptions.repeatTimeout.inWholeMilliseconds}ms:  ${logOptions.tag}"
                    )
                )
            }
        }
    }

    fun stopAllJobs () {
        startedJobs.forEach{
            val jobsToStop = startedJobs.toList()
            jobsToStop.forEach {
                stopJob(it.first)
            }
        }
    }

    fun stopJob(job: Job?) {
        job?.cancel()
        val index = _startedJobs.indexOfFirst { it.first == job }
        if (index >= 0) {
            _startedJobs[index] = _startedJobs[index].copy(first = null)
            _startedJobs.remove(_startedJobs[index])
        }
    }
}


@SuppressLint("MutableCollectionMutableState")
@Composable
fun LogGeneratorInterface() {
    val viewModel: LogGeneratorViewModel = viewModel()
    var logOptions by remember { mutableStateOf(LogOptions()) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        var expanded by remember { mutableStateOf(false) }
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .clickable {
                        expanded = !expanded
                        Log.i(
                            "LogGenerator", "Spoiler for LogGenerator has been clicked" +
                                    "\nActual state is ${if (expanded) "opened" else "closed"}"
                        )
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Генератор логов",
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = "Expand"
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically(),
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp)
            ) {
                Column {
                    DropdownMenuForEnum(
                        label = "Log Level",
                        selectedOption = logOptions.logLevel,
                        options = LogLevel.entries.toTypedArray(),
                        onSelectionChange = { logOptions = logOptions.copy(logLevel = it) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    DropdownMenuForEnum(
                        label = "Message Type",
                        selectedOption = logOptions.messageType,
                        options = MessageType.entries.toTypedArray(),
                        onSelectionChange = {
                            logOptions = logOptions.copy(messageType = it)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if ((logOptions.messageType == MessageType.RANDOM) ||
                        (logOptions.messageType == MessageType.RANDOM_STRING)
                    ) {
                        OutlinedTextField(
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.None),
                            value = logOptions.randomMessageLength.toString(),
                            onValueChange = {
                                logOptions = logOptions.copy(
                                    randomMessageLength = it.toIntOrNull()
                                        ?: logOptions.randomMessageLength
                                )
                            },
                            label = { Text("Random Message Length") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if ((logOptions.messageType == MessageType.STRING)) {
                        OutlinedTextField(
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.None),
                            value = logOptions.customMessage,
                            onValueChange = { logOptions = logOptions.copy(customMessage = it) },
                            label = { Text("Custom Message") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    OutlinedTextField(
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.None),
                        value = logOptions.tag,
                        onValueChange = { logOptions = logOptions.copy(tag = it) },
                        label = { Text("Tag") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = logOptions.shouldRepeat,
                            onCheckedChange = { logOptions = logOptions.copy(shouldRepeat = it) }
                        )
                        Text("Repeat")
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    if (logOptions.shouldRepeat) {

                        OutlinedTextField(
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.None),
                            value = logOptions.repeatTimeout.inWholeMilliseconds.toString(),
                            onValueChange = {
                                logOptions = logOptions.copy(
                                    repeatTimeout = it.toLongOrNull()
                                        ?.toDuration(DurationUnit.MILLISECONDS)
                                        ?: logOptions.repeatTimeout
                                )
                            },
                            label = { Text("Repeat Timeout (ms)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Start Button
                    Button(
                        modifier = Modifier
                            .fillMaxWidth(),
                        onClick = {
                            viewModel.startLogGenerator(logOptions)
                        }
                    ) {
                        Text("Start")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        SpecialActionsSpoiler()
        Spacer(modifier = Modifier.height(16.dp))

        StartPresets(getLogOptionsPresets(),viewModel)
        Spacer(modifier = Modifier.height(16.dp))

        RunningJobsList(viewModel)
    }
}

@Composable
fun RunningJobsList(viewModel: LogGeneratorViewModel) {
    val startedJobs = viewModel.startedJobs
    val onStopJob = viewModel::stopJob

    AnimatedVisibility(
        visible = startedJobs.isNotEmpty(),
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = "Запущенные задачи: ",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                startedJobs.forEach {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = it.second,
                            modifier = Modifier.weight(7f)
                        )
                        Button(
                            onClick = { onStopJob(it.first) },
                            modifier = Modifier.weight(2f)
                        ) {
                            Text("Stop")
                        }
                    }
                }
                StopAllJobs(viewModel)
            }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Enum<T>> DropdownMenuForEnum(
    label: String,
    selectedOption: T,
    options: Array<T>,
    onSelectionChange: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            value = selectedOption.name,
            onValueChange = { },
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            colors = ExposedDropdownMenuDefaults.textFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    onClick = {
                        onSelectionChange(option)
                        expanded = false
                    },
                    text = { Text(option.name) }
                )
            }
        }
    }
}

@Composable
fun SpecialActionButton(specialActionType: SpecialActionType) {
    Button(
        onClick = {
            Log.i("SpecialActionsExecutor", "Button $specialActionType has been pushed")
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

@Composable
fun SpecialActionsSpoiler() {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .clickable {
                    expanded = !expanded
                    Log.i(
                        "SpecialActionsExecutor", "Spoiler for SpecialActions has been clicked" +
                                "\nActual state is ${if (expanded) "opened" else "closed"}"
                    )
                }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Особые события",
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = "Expand"
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp)
        ) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))
                SpecialActionButton(SpecialActionType.ANR)
                SpecialActionButton(SpecialActionType.CRASH)
                SpecialActionButton(SpecialActionType.NPE)
                SpecialActionButton(SpecialActionType.NON_FATAL)
            }
        }
    }
}


@Composable
fun StartPresets(logOptionsList: List<LogOptions>, viewModel: LogGeneratorViewModel) {
    Button(
        onClick = {
            Log.i("SpecialActionsExecutor", "Start presets button has been pushed")
            logOptionsList.forEach {
                viewModel.startLogGenerator(it)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Text(
            text = "Запустить тестовые пресеты",
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun StopAllJobs(viewModel: LogGeneratorViewModel) {
    Button(
        onClick = {
            Log.i("SpecialActionsExecutor", "Stop all jobs button has been pushed")

                viewModel.stopAllJobs()

        },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Text(
            text = "Остановить все задачи",
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
}

fun getLogOptionsPresets():List<LogOptions> {

    return listOf<LogOptions>(
        LogOptions(
            logLevel = LogLevel.RANDOM,
            messageType = MessageType.RANDOM,
            repeatTimeout = 200.toDuration(DurationUnit.MILLISECONDS),
            shouldRepeat = true,
            randomMessageLength = 300
        ),
        LogOptions(
            logLevel = LogLevel.WARN,
            messageType = MessageType.JSON,
            repeatTimeout = 200.toDuration(DurationUnit.MILLISECONDS),
            shouldRepeat = true,
        ),
        LogOptions(
            logLevel = LogLevel.RANDOM,
            messageType = MessageType.STACK_TRACE,
            repeatTimeout = 10000.toDuration(DurationUnit.MILLISECONDS),
            shouldRepeat = true,
        ),
        LogOptions(
            logLevel = LogLevel.INFO,
            messageType = MessageType.STRING,
            repeatTimeout = 2000.toDuration(DurationUnit.MILLISECONDS),
            shouldRepeat = true,
            tag = "Tag1",
            customMessage = "Custom message for ExampleCustomTag1"
        ),
        LogOptions(
            logLevel = LogLevel.VERBOSE,
            messageType = MessageType.STRING,
            repeatTimeout = 2000.toDuration(DurationUnit.MILLISECONDS),
            shouldRepeat = true,
            tag = "ExampleCustomTag2",
            customMessage = "Example Tag2 message"
        ),
        LogOptions(
            logLevel = LogLevel.ERROR,
            messageType = MessageType.STRING,
            repeatTimeout = 200.toDuration(DurationUnit.MILLISECONDS),
            shouldRepeat = true,
            tag = "ExampleTag3",
            customMessage = "Tag3 custom message"
        )
    )
}
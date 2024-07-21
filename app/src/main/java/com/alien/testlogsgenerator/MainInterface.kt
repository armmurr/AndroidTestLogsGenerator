package com.alien.testlogsgenerator

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel

const val ACTIONS_TAG = "UserActions"
const val STATE_TAG = "AppStates"
class LogGeneratorViewModel : ViewModel() {
    private val _startedJobs = mutableStateListOf<Pair<Job?, String>>()
    val startedJobs: List<Pair<Job?, String>> = _startedJobs

    fun startLogGenerator(logOptions: LogOptions) {
        viewModelScope.launch {
            val job = LogStarter(logOptions, this).start()
            val jobName =  "\"${logOptions.logLevel.name} " +
                    "${logOptions.messageType} " +
                    if (logOptions.shouldRepeat) {"${logOptions.repeatTimeout.inWholeMilliseconds}ms "} else {""} +
                            "${logOptions.tag}\""
            if (!logOptions.shouldRepeat) {
                delay(300.toDuration(DurationUnit.MILLISECONDS))
                job.cancel()
                Log.i(STATE_TAG,"[startLogGenerator] One time Job $jobName has been performed")
            } else {
                _startedJobs.add(
                    Pair(
                        job,
                        jobName
                    )
                )
                Log.i(STATE_TAG,"[startLogGenerator] Repeated Job $jobName has been started")
            }
        }
    }

    fun stopAllJobs () {
        val jobsToStop = startedJobs.toList()
        jobsToStop.forEach {
            stopJob(it.first)
        }
        Log.i(STATE_TAG,"[stopAllJobs] All jobs have been stopped")
    }

    fun stopJob(job: Job?) {
        job?.cancel()
        val index = _startedJobs.indexOfFirst { it.first == job }
        if (index >= 0) {
            val jobName = _startedJobs[index]
            //_startedJobs[index] = _startedJobs[index].copy(first = null)
            _startedJobs.remove(_startedJobs[index])
            Log.i(STATE_TAG,"[stopJob] Job ${jobName.second} has been stopped")
        }
    }
}

@Composable
fun MainInterface(list: SnapshotStateList<String>) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        LogsGenerator()
        Spacer(modifier = Modifier.height(16.dp))

        SpecialActionsSpoiler()
        Spacer(modifier = Modifier.height(16.dp))

        ComposableList(list)
    }
}

@Composable
fun LogsGenerator() {
    val viewModel: LogGeneratorViewModel = viewModel()
    DropDown("Генератор логов в цикле") {
        CustomLogMenu(viewModel)
        Spacer(modifier = Modifier.height(16.dp))

        StartPresets(getLogOptionsPresets(), viewModel)
        Spacer(modifier = Modifier.height(16.dp))

        RunningJobsList(viewModel)
    }
}

@Composable
fun CustomLogMenu(viewModel: LogGeneratorViewModel) {
    var logOptions by remember { mutableStateOf(LogOptions()) }
    DropDown("Создать кастомные логи") {
        DropdownMenuForEnum(
            label = "Log Level",
            selectedOption = logOptions.logLevel,
            options = LogLevel.entries.toTypedArray(),
            onSelectionChange = {
                logOptions = logOptions.copy(logLevel = it)
                Log.i(ACTIONS_TAG, "Log Level is set to $it")
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        DropdownMenuForEnum(
            label = "Message Type",
            selectedOption = logOptions.messageType,
            options = MessageType.entries.toTypedArray(),
            onSelectionChange = {
                logOptions = logOptions.copy(messageType = it)
                Log.i(ACTIONS_TAG, "Message Type is set to $it")
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
                    val value = it.toIntOrNull()
                        ?: logOptions.randomMessageLength
                    logOptions = logOptions.copy(
                        randomMessageLength = value
                    )
                    Log.i(ACTIONS_TAG, "randomMessageLength is set to $value")
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
                onValueChange = {
                    logOptions = logOptions.copy(customMessage = it)
                    Log.i(ACTIONS_TAG, "Custom Message is set to $it")
                },
                label = { Text("Custom Message") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        OutlinedTextField(
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.None),
            value = logOptions.tag,
            onValueChange = {
                logOptions = logOptions.copy(tag = it)
                Log.i(ACTIONS_TAG, "Tag is set to $it")
            },
            label = { Text("Tag") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = logOptions.shouldRepeat,
                onCheckedChange = {
                    logOptions = logOptions.copy(shouldRepeat = it)
                    Log.i(
                        ACTIONS_TAG,
                        "Repeat checkbox has been ${if (it) "checked" else "unchecked"}"
                    )
                }
            )
            Text("Repeat")
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (logOptions.shouldRepeat) {

            OutlinedTextField(
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.None),
                value = logOptions.repeatTimeout.inWholeMilliseconds.toString(),
                onValueChange = {
                    val value = it.toLongOrNull()
                        ?.toDuration(DurationUnit.MILLISECONDS)
                        ?: logOptions.repeatTimeout
                    logOptions = logOptions.copy(
                        repeatTimeout = value
                    )
                    Log.i(
                        ACTIONS_TAG,
                        "Repeat Timeout (ms) is set to ${value.inWholeMilliseconds}ms"
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
                Log.i(ACTIONS_TAG, "Start custom job button has been pushed")
            }
        ) {
            Text("Start")
        }
        Spacer(modifier = Modifier.height(16.dp))
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
                            onClick = {
                                Log.i(ACTIONS_TAG, "Stop job ${it.second} button has been pushed")
                                onStopJob(it.first)
                            },
                            modifier = Modifier.weight(3f)
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
            Log.i(ACTIONS_TAG, "Button $specialActionType has been pushed")
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
    DropDown("Особые события") {
        Spacer(modifier = Modifier.height(16.dp))
        SpecialActionButton(SpecialActionType.ANR)
        SpecialActionButton(SpecialActionType.CRASH)
        SpecialActionButton(SpecialActionType.NPE)
        SpecialActionButton(SpecialActionType.NON_FATAL)
    }
}


@Composable
fun StartPresets(logOptionsList: List<LogOptions>, viewModel: LogGeneratorViewModel) {
    Button(
        onClick = {
            Log.i(ACTIONS_TAG, "Start presets button has been pushed")
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
            Log.i(ACTIONS_TAG, "Stop all jobs button has been pushed")
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

@Composable
fun DropDown(text:String, content: @Composable () -> Unit) {
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
                        ACTIONS_TAG, "Spoiler for $text has been clicked"
                    )
                    Log.i(
                        STATE_TAG,
                        "Spoiler for $text is ${if (expanded) "opened" else "closed"}"
                    )
                }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
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
                content()
            }
        }

    }
}
@Composable
fun ComposableList(list: SnapshotStateList<String>) {
    val listState = rememberLazyListState()
    LaunchedEffect(list.size) {
        listState.animateScrollToItem(index = list.size - 1)
    }

    DropDown("Activity States logs") {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    items(list.size) { index ->
                        Text(text = "${index+1})  ${list[index]}", modifier = Modifier.padding(4.dp))
                    }
                }
            }
        }
    }



}

fun getLogOptionsPresets():List<LogOptions> {
    return listOf(
        LogOptions(
            logLevel = LogLevel.RANDOM,
            messageType = MessageType.RANDOM,
            repeatTimeout = 100.toDuration(DurationUnit.MILLISECONDS),
            shouldRepeat = true,
            randomMessageLength = 1000
        ),
        LogOptions(
            logLevel = LogLevel.WARN,
            messageType = MessageType.JSON,
            repeatTimeout = 100.toDuration(DurationUnit.MILLISECONDS),
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
            logLevel = LogLevel.DEBUG,
            messageType = MessageType.STRING,
            repeatTimeout = 500.toDuration(DurationUnit.MILLISECONDS),
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
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
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import android.app.Activity
import androidx.compose.ui.res.stringResource

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
            _startedJobs.remove(_startedJobs[index])
            Log.i(STATE_TAG,"[stopJob] Job ${jobName.second} has been stopped")
        }
    }
}

@Composable
fun MainInterface(list: SnapshotStateList<String>, mainActivity: MainActivity) {
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

        PastActivityStatesList(list, mainActivity)
        Spacer(modifier = Modifier.height(16.dp))

        RequestBluetoothPermission()
        Spacer(modifier = Modifier.height(16.dp))

        val context = LocalContext.current
        if (context is Activity) {
            Button(
                onClick = { context.finish() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(stringResource(R.string.finish_activity))
            }
        }
    }
}

@Composable
fun LogsGenerator() {
    val viewModel: LogGeneratorViewModel = viewModel()
    DropDown(stringResource(R.string.log_generator_loop)) {
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
    DropDown(stringResource(R.string.create_custom_logs)) {
        DropdownMenuForEnum(
            label = stringResource(R.string.log_level),
            selectedOption = logOptions.logLevel,
            options = LogLevel.entries.toTypedArray(),
            onSelectionChange = {
                logOptions = logOptions.copy(logLevel = it)
                Log.i(ACTIONS_TAG, "Log Level is set to $it")
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        DropdownMenuForEnum(
            label = stringResource(R.string.message_type),
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
                label = { Text(stringResource(R.string.random_message_length)) },
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
                label = { Text(stringResource(R.string.custom_message)) },
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
            label = { Text(stringResource(R.string.tag)) },
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
            Text(stringResource(R.string.repeat))
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
                label = { Text(stringResource(R.string.repeat_timeout_ms)) },
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
            Text(stringResource(R.string.start))
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
                        text = stringResource(R.string.running_tasks),
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
                            Text(stringResource(R.string.stop))
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
    DropDown(stringResource(R.string.special_events)) {
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
            text = stringResource(R.string.start_test_presets),
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
            text = stringResource(R.string.stop_all_jobs),
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun DropDown(text:String, isExpanded: Boolean = false, content: @Composable () -> Unit) {
    var expanded by remember { mutableStateOf(isExpanded) }
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
                contentDescription = stringResource(R.string.expand_arrow_content_description)
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
fun PastActivityStatesList(list: SnapshotStateList<String>, mainActivity: MainActivity) {
    val listState = rememberLazyListState()
    LaunchedEffect(list.size) {
        listState.animateScrollToItem(index = list.size) //тут нет -1 так как ниже мы всегда добавляем кнопку в конец
    }

    DropDown(stringResource(R.string.activity_states_logs), true) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                ) {
                    items(list.size) { index ->
                        Text(text = "${index+1})  ${list[index]}", modifier = Modifier.padding(4.dp))
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { mainActivity.clearStatesLogs() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(stringResource(R.string.clear_log))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun RequestBluetoothPermission() {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { _: Boolean ->
        }
    )

    Text(stringResource(R.string.request_permission_description))
    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                launcher.launch(Manifest.permission.CAMERA)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Text(stringResource(R.string.request_camera_permission))
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
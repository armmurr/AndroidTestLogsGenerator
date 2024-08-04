package com.alien.testlogsgenerator

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import com.alien.testlogsgenerator.ui.theme.TestLogsGeneratorTheme
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val mainActivityActionsTag = "MainActivityActions"
    private val listOfStates = mutableStateListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        loadStates()
        if (savedInstanceState != null) {
            val saveInstStateLog = "onCreate(savedInstanceState: Bundle?), bundle: ${savedInstanceState.hashCode()}"
            listOfStates.add(saveInstStateLog)
            Log.i(mainActivityActionsTag,saveInstStateLog)
        } else {
            val logStr = "${getCurrentTime()}: onCreate()"
            listOfStates.add(logStr)
            Log.i(mainActivityActionsTag,logStr)
        }
        saveStates()
        super.onCreate(savedInstanceState)
        setContent {
            TestLogsGeneratorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        verticalArrangement = Arrangement.Top
                    ) {
                        MainInterface(listOfStates,this@MainActivity)
                    }
                }
            }
        }
    }

    override fun onStart() {
        val logStr = "${getCurrentTime()}: onStart()"
        listOfStates.add(logStr)
        Log.i(mainActivityActionsTag,logStr)
        saveStates()
        super.onStart()
    }

    override fun onResume() {
        val logStr = "${getCurrentTime()}: onResume()"
        listOfStates.add(logStr)
        Log.i(mainActivityActionsTag,logStr)
        saveStates()
        super.onResume()
    }

    override fun onPause() {
        val logStr = "${getCurrentTime()}: onPause()"
        listOfStates.add(logStr)
        Log.i(mainActivityActionsTag,logStr)
        saveStates()
        super.onPause()
    }

    override fun onStop() {
        val logStr = "${getCurrentTime()}: onStop()"
        listOfStates.add(logStr)
        Log.i(mainActivityActionsTag,logStr)
        saveStates()
        super.onStop()
    }

    override fun onRestart() {
        val logStr = "${getCurrentTime()}: onRestart()"
        listOfStates.add(logStr)
        Log.i(mainActivityActionsTag,logStr)
        saveStates()
        super.onRestart()
    }

    override fun onDestroy() {
        val logStr = "${getCurrentTime()}: onDestroy()"
        listOfStates.add(logStr)
        Log.i(mainActivityActionsTag,logStr)
        saveStates()
        super.onDestroy()
    }

    private fun saveStates() {
        val jsonArray = JSONArray()
        for (state in listOfStates) {
            jsonArray.put(state)
        }

        val jsonObject = JSONObject()
        jsonObject.put("log_states", jsonArray)

        val prefs = getPreferences(Context.MODE_PRIVATE)
        prefs.edit().putString("log_states_json", jsonObject.toString()).apply()
    }

    private fun loadStates() {
        val prefs = getPreferences(Context.MODE_PRIVATE)
        val jsonString = prefs.getString("log_states_json", null)

        if (!jsonString.isNullOrEmpty()) {
            val jsonObject = JSONObject(jsonString)
            val jsonArray = jsonObject.getJSONArray("log_states")

            for (i in 0 until jsonArray.length()) {
                listOfStates.add(jsonArray.getString(i))
            }
        }
    }

     fun clearStatesLogs() {
        listOfStates.clear()
        val prefs = getPreferences(Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.remove("log_states_json")
        editor.apply()
        saveStates()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val logStr = "${getCurrentTime()}: onSaveInstanceState(outState: Bundle), bundle: ${outState.hashCode()}"
        listOfStates.add(logStr)
        Log.i(mainActivityActionsTag,logStr)
        saveStates()
        super.onSaveInstanceState(outState)
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        val logStr = "${getCurrentTime()}: onSaveInstanceState(outState: Bundle," +
                " outPersistentState: PersistableBundle), " +
                "bundle: ${outState.hashCode()}, " +
                "persistentBundle: ${outPersistentState.hashCode()}"
        listOfStates.add(logStr)
        Log.i(mainActivityActionsTag,logStr)
        saveStates()
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        val logStr = "${getCurrentTime()}: onRestoreInstanceState(savedInstanceState: Bundle), bundle: ${savedInstanceState.hashCode()}"
        listOfStates.add(logStr)
        Log.i(mainActivityActionsTag,logStr)
        saveStates()
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onLowMemory() {
        val logStr = "${getCurrentTime()}: onLowMemory()"
        listOfStates.add(logStr)
        Log.i(mainActivityActionsTag,logStr)
        saveStates()
        super.onLowMemory()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        val logStr = "${getCurrentTime()}: onConfigurationChanged(newConfig: Configuration)"
        listOfStates.add(logStr)
        Log.i(mainActivityActionsTag,logStr)
        saveStates()
        super.onConfigurationChanged(newConfig)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onTopResumedActivityChanged(isTopResumedActivity: Boolean) {
        val logStr = "${getCurrentTime()}: onTopResumedActivityChanged(), isTopResumedActivity = $isTopResumedActivity"
        listOfStates.add(logStr)
        Log.i(mainActivityActionsTag,logStr)
        saveStates()
        super.onTopResumedActivityChanged(isTopResumedActivity)
    }

    override fun finish() {
        val logStr = "${getCurrentTime()}: finish()"
        listOfStates.add(logStr)
        Log.i(mainActivityActionsTag,logStr)
        saveStates()
        super.finish()
    }

    private fun getCurrentTime(): String {
        val currentDate = Date()
        val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return formatter.format(currentDate)
    }
}

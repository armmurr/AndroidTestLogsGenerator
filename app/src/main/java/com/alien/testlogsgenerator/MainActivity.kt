package com.alien.testlogsgenerator

import android.content.res.Configuration
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import com.alien.testlogsgenerator.ui.theme.TestLogsGeneratorTheme

class MainActivity : ComponentActivity() {
    private val mainActivityActionsTag = "MainActivityActions"
    private val listOfStates = mutableStateListOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            val savedList = savedInstanceState.getStringArrayList("log_list")
            if (savedList != null) {
                listOfStates.addAll(savedList)
            }
            val saveInstStateLog = "savedInstanceState is not mull"
            listOfStates.add(saveInstStateLog)
            Log.i(mainActivityActionsTag,saveInstStateLog)
        }
        val logStr = "onCreate(savedInstanceState: Bundle?)"
        listOfStates.add(logStr)
        Log.i(mainActivityActionsTag,logStr)

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
                        MainInterface(listOfStates)
                    }
                }
            }
        }
    }

    override fun onStart() {
        val logStr = "onStart()"
        listOfStates.add(logStr)
        Log.i(mainActivityActionsTag,logStr)
        super.onStart()
    }

    override fun onResume() {
        val logStr = "onResume()"
        listOfStates.add(logStr)
        Log.i(mainActivityActionsTag,logStr)
        super.onResume()
    }

    override fun onPause() {
        val logStr = "onPause()"
        listOfStates.add(logStr)
        Log.i(mainActivityActionsTag,logStr)
        super.onPause()
    }

    override fun onStop() {
        val logStr = "onStop()"
        listOfStates.add(logStr)
        Log.i(mainActivityActionsTag,logStr)
        super.onStop()
    }

    override fun onRestart() {
        val logStr = "onRestart()"
        listOfStates.add(logStr)
        Log.i(mainActivityActionsTag,logStr)
        super.onRestart()
    }

    override fun onDestroy() {
        val logStr = "onDestroy()"
        listOfStates.add(logStr)
        Log.i(mainActivityActionsTag,logStr)
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val logStr = "onSaveInstanceState(outState: Bundle)"
        listOfStates.add(logStr)
        Log.i(mainActivityActionsTag,logStr)
        super.onSaveInstanceState(outState)
        outState.putStringArrayList("log_list", ArrayList(listOfStates))
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        val logStr = "onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle)"
        listOfStates.add(logStr)
        Log.i(mainActivityActionsTag,logStr)
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        val logStr = "onRestoreInstanceState(savedInstanceState: Bundle)"
        listOfStates.add(logStr)
        Log.i(mainActivityActionsTag,logStr)
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onLowMemory() {
        val logStr = "onLowMemory()"
        listOfStates.add(logStr)
        Log.i(mainActivityActionsTag,logStr)
        super.onLowMemory()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        // не будет вызываться так как в манифесте не прописан android:configChanges
        val logStr = "onConfigurationChanged(newConfig: Configuration)"
        listOfStates.add(logStr)
        Log.i(mainActivityActionsTag,logStr)
        super.onConfigurationChanged(newConfig)
    }
}

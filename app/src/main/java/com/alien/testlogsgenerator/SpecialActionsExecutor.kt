package com.alien.testlogsgenerator

import android.os.SystemClock.sleep
import android.util.Log
import java.lang.NullPointerException

class SpecialActionsExecutor {

     fun performAction(actionType: SpecialActionType) {
        when (actionType) {
            SpecialActionType.ANR -> {sleep(5000)}
            SpecialActionType.CRASH -> throw RuntimeException("This is a simulated crash")
            SpecialActionType.NPE -> throw NullPointerException("This is a simulated NPE")
            SpecialActionType.NON_FATAL -> try {
                throw RuntimeException("This is a simulated non-fatal exception")
            } catch (e: Exception) {
                Log.e("SpecialActionsExecutor", "", e)
            }
        }

    }

}

//TODO:
// надо разделить NPE и ANR это типы ошибок,
// а Crash и Non-fatal это просто наличие или отсутствие обработки
enum class SpecialActionType(val actionName:String) {
    ANR ("ANR (нажмите дважды)"),
    NPE ("NullPointerException c Fatal Exception"),
    CRASH ("Crash (Fatal Exception)"),
    NON_FATAL("Non-fatal exception")
}

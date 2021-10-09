package com.garbagecollection.utils

import android.content.Context
import android.content.SharedPreferences
import com.garbagecollection.viewUI.weekday.WeekDayData

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object PrefUtils {

    fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences("LINKODES_PREF", Context.MODE_PRIVATE)
    }

    fun storeUserInfo(context: Context, Driver: WeekDayData) {
        val editor = getSharedPreferences(context).edit()
        editor.putString("USER_INFO", Gson().toJson(Driver))
        editor.apply()
    }

    fun retrieveUserInfo(context: Context): WeekDayData? {
        val type = object : TypeToken<WeekDayData>() {}.type
        return Gson().fromJson(getSharedPreferences(context).getString("USER_INFO", null), type)
    }

    fun storeAuthKey(context: Context, apiKey: String) {
        val editor = getSharedPreferences(context).edit()
        editor.putString("AUTH_KEY", apiKey)
        editor.apply()
    }

    fun getAuthKey(context: Context): String? {
        return getSharedPreferences(context).getString("AUTH_KEY", null)
    }

    fun setSaveValue(context: Context, key: String, value: String) {
        val editor = getSharedPreferences(context).edit()
        editor.putString(key, value)
        editor.apply()
        editor.commit()
    }

    fun getSaveValue(context: Context, key: String): String {
        if (getSharedPreferences(context).getString(key, "")!!.isEmpty()) {
            return ""
        } else {
            return getSharedPreferences(context).getString(key, "")!!
        }
    }

    fun getSaveIntValue(context: Context, key: String): Int {
        return getSharedPreferences(context).getInt(key, 0)
    }

    fun setUserLoggedInStatus(context: Context, isLogin: Boolean) {
        val editor = getSharedPreferences(context).edit()
        editor.putBoolean("IS_USER_LOGGED_IN", isLogin)
        editor.apply()
    }

    fun getUserLoggedInStatus(context: Context): Boolean? {
        return getSharedPreferences(context).getBoolean("IS_USER_LOGGED_IN", false)
    }

    fun clearPrefs(context: Context) {
        val sharedPrefs = context.getSharedPreferences("LINKODES_PREF", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        editor.clear()
        editor.apply()
    }

    fun clear(context: Context, name: String) {
        val sharedPrefs = context.getSharedPreferences("LINKODES_PREF", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        editor.remove(name)
        editor.apply()
    }

    fun storeAppleEmailInfo(context: Context, user: String?) {
        val editor = getSharedPreferences(context).edit()
        editor.putString("AuthResult", Gson().toJson(user))
        editor.apply()
    }

}
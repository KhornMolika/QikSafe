package com.example.testfeatureofqiksafe.util

import android.content.Context

object SharedPrefHelper {
    private const val PREF_NAME = "QikSafePrefs"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_REMEMBER_ME = "remember_me"

    fun saveUserId(context: Context, userId: String) {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPref.edit().putString(KEY_USER_ID, userId).apply()
    }

    fun getUserId(context: Context): String? {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPref.getString(KEY_USER_ID, null)
    }

    fun setRememberMe(context: Context, remember: Boolean) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_REMEMBER_ME, remember).apply()
    }

    fun isRememberMeEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_REMEMBER_ME, false)
    }

    fun clearAll(context: Context) {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPref.edit().clear().apply()
    }
}
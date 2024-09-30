package net.palmut.fmscardbalance.data

import java.util.prefs.Preferences

actual class SharedPreferences actual constructor() {
    val preferences =  Preferences.userRoot().node("prefs.ini")

    actual fun putString(key: String, value: String) {
        preferences.put(key, value)
        preferences.flush()
    }

    actual fun getString(key: String): String? {
        return preferences.get(key, "[]")
    }

    actual companion object {
        actual var INSTANCE: SharedPreferences
            get() = SharedPreferences()
            set(value) {}
    }
}
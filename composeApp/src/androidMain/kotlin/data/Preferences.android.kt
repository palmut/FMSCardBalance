package data

import android.content.Context
import android.content.SharedPreferences

actual class SharedPreferences actual constructor() {

    constructor(context: Context): this() {
        this.prefs = context.getSharedPreferences("PREfS", Context.MODE_PRIVATE)
    }

    private var prefs: SharedPreferences? = null

    actual fun putString(key: String, value: String) {
        prefs?.edit()?.putString(key, value)?.apply()
    }

    actual fun getString(key: String): String? {
        return prefs?.getString(key, null)
    }

    actual companion object {
        actual var INSTANCE: data.SharedPreferences = SharedPreferences()

        fun create(context: Context) {
            INSTANCE = SharedPreferences(context)
        }
    }
}
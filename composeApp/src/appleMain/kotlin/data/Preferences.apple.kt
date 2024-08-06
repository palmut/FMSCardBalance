package data

import platform.Foundation.NSUserDefaults

actual class Preferences {
    actual fun putString(key: String, value: String) {
        NSUserDefaults.standardUserDefaults.setObject(value, key)
    }

    actual fun getString(key: String): String? {
        return NSTUserDefaults.standardUserDefaults.stringForKey(key)
    }

    actual companion object {
        actual var INSTANCE: Preferences = Preferences()
    }
}
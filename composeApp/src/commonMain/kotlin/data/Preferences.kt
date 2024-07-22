package data

expect class Preferences() {

    fun putString(key: String, value: String)
    fun getString(key: String): String?

    companion object {
        var INSTANCE: Preferences
    }
}
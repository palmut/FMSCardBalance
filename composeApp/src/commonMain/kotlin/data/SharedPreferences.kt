package data

expect class SharedPreferences() {

    fun putString(key: String, value: String)
    fun getString(key: String): String?

    companion object {
        var INSTANCE: SharedPreferences
    }
}
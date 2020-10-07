package mp.amir.ir.wewi.respository.sharepref

import android.content.Context
import android.content.SharedPreferences

object PrefManager {
    private const val MY_PREFS_NAME = "prefs"
    private const val IS_USER_LOGGED_IN = "is-login"

    private lateinit var prefs: SharedPreferences


    val isUserLoggedIn get() = prefs.getBoolean(IS_USER_LOGGED_IN, false)




    fun setContext(context: Context) {
        if (!this::prefs.isInitialized) {
            prefs = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    fun flush() {
        prefs.edit().clear().apply()
    }

    fun userLoggedIn() {
        prefs.edit().putBoolean(IS_USER_LOGGED_IN, true).apply()
    }

    fun userLoggedOut() {
        prefs.edit().putBoolean(IS_USER_LOGGED_IN, false).apply()
    }

}
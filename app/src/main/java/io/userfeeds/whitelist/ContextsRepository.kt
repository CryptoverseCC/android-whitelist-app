package io.userfeeds.whitelist

import android.content.Context
import android.preference.PreferenceManager
import kotlin.LazyThreadSafetyMode.NONE

class ContextsRepository(context: Context) {

    private val prefs by lazy(NONE) { PreferenceManager.getDefaultSharedPreferences(context) }

    val contexts get() = prefs.getStringSet(CONTEXTS_KEY, emptySet()).sorted()

    fun add(context: String) {
        val oldContexts = prefs.getStringSet(CONTEXTS_KEY, emptySet())
        val newContexts = oldContexts + context
        prefs.edit().putStringSet(CONTEXTS_KEY, newContexts).apply()
    }

    companion object {

        private const val CONTEXTS_KEY = "contexts"
    }
}

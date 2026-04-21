package com.example.distridulce

import android.app.Application
import com.example.distridulce.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Custom Application class.
 *
 * Responsibilities:
 * - Provides the applicationContext needed by [SessionManager] for DataStore.
 * - Kicks off the async token-loading from DataStore so [SessionManager.isLoggedIn]
 *   transitions from `null` (initializing) to `true` or `false` before any screen
 *   tries to read the session state.
 *
 * Must be registered in AndroidManifest.xml via android:name=".DistriDulceApplication".
 */
class DistriDulceApplication : Application() {

    /**
     * Application-wide IO scope.  SupervisorJob ensures that a failure in one
     * child coroutine (e.g. a DataStore write) does not cancel the others.
     */
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // 1. Give SessionManager the applicationContext it needs for DataStore.
        SessionManager.init(this)
        // 2. Read the persisted token asynchronously on a background thread.
        //    Until this completes, SessionManager.isLoggedIn == null and
        //    DistriDulceApp shows a loading screen.
        appScope.launch { SessionManager.loadToken() }
    }
}

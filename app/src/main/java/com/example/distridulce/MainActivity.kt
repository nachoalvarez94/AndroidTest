package com.example.distridulce

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.distridulce.navigation.NavGraph
import com.example.distridulce.session.SessionManager
import com.example.distridulce.ui.auth.LoginScreen
import com.example.distridulce.ui.components.Sidebar
import com.example.distridulce.ui.theme.BrandBlue
import com.example.distridulce.ui.theme.DistriDulceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DistriDulceTheme {
                DistriDulceApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DistriDulceApp() {
    // Three-valued: null=initializing, true=logged in, false=logged out.
    val isLoggedIn by SessionManager.isLoggedIn.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color    = MaterialTheme.colorScheme.background
    ) {
        when (isLoggedIn) {

            // ── Initializing ─────────────────────────────────────────────────
            // DataStore.loadToken() is still running.  Show a blank loading
            // screen — typically resolves in <50 ms.
            null -> AppLoadingScreen()

            // ── Unauthenticated ───────────────────────────────────────────────
            // No token found (first launch, logout, or 401 received).
            false -> LoginScreen()

            // ── Authenticated ─────────────────────────────────────────────────
            // Token loaded — show the full app.
            // NavController is created INSIDE this branch so that a new, clean
            // controller is used each time the user logs in.  Any state from a
            // previous session is discarded automatically.
            true -> {
                val navController = rememberNavController()
                PermanentNavigationDrawer(
                    drawerContent = { Sidebar(navController = navController) }
                ) {
                    NavGraph(navController = navController)
                }
            }
        }
    }
}

// ── Loading screen ────────────────────────────────────────────────────────────

/**
 * Shown while [SessionManager.loadToken] is reading the persisted token from
 * DataStore.  Typically visible for less than one frame on a warm start.
 */
@Composable
private fun AppLoadingScreen() {
    Box(
        modifier         = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = BrandBlue)
    }
}

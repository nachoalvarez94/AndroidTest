package com.example.distridulce.navigation

import androidx.navigation.NavController

/**
 * Navigates to a top-level / sidebar destination.
 *
 * Every navigation to a root section — whether triggered by a sidebar tap or
 * by a Dashboard quick-action — must use these options so that the Navigation
 * component treats all sections as peer tabs, never as nested sub-destinations
 * of each other.
 *
 * Why each option matters:
 *  - `popUpTo(startId) { saveState = true }`: pops everything above the start
 *    destination, saving the popped section's back stack so it can be restored
 *    when the user comes back to that section.
 *  - `launchSingleTop = true`: prevents duplicate entries when the user taps
 *    the same section they are already on.
 *  - `restoreState = true`: when returning to a section that was previously
 *    saved, restores its back stack (e.g. remembers scroll position or
 *    opened sub-screens within that section).
 *
 * Omitting these options — as a plain `navigate(route)` call does — pushes the
 * destination onto the *current* section's back stack.  If that state is then
 * saved by a subsequent `popUpTo { saveState = true }`, it gets associated with
 * the wrong section and is incorrectly restored when returning to it.
 */
fun NavController.navigateTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState    = true
    }
}

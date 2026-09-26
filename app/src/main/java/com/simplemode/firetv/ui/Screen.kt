package com.simplemode.firetv.ui

/** Every place the viewer can be. Deliberately small: everything is one
 *  tap from [Home], and Home is always reachable the same way. */
sealed interface Screen {
    data object Home : Screen
    data object LiveGuide : Screen
    data object HerShows : Screen
    data object Films : Screen
    data object BoxSets : Screen
    data object FamilyVideos : Screen
    data object CallForHelp : Screen
    data object TellMe : Screen
    data object Family : Screen
    data object CaregiverPin : Screen
    data object CaregiverCatalog : Screen
    data class NowPlaying(val label: String) : Screen
    data class NotAvailable(val message: String) : Screen
}

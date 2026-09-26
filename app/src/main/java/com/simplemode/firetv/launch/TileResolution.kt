package com.simplemode.firetv.launch

/** What happened when a tile was pressed. */
sealed interface LaunchResult {
    data object Launched : LaunchResult
    data class NotAvailable(val message: String) : LaunchResult
}

/**
 * Whatever can answer "is this package installed" -- abstracted so the
 * resolution logic below is testable without a real PackageManager.
 */
fun interface PackageResolver {
    fun isInstalled(packageName: String): Boolean
}

/**
 * Decides what pressing a tile should do. Kept separate from any Activity or
 * Intent so it is plain, fast, unit-testable logic: the only thing that
 * actually varies from run to run is whether a given package is installed.
 */
object TileResolution {
    fun resolve(tile: Tile, resolver: PackageResolver): LaunchResult =
        when (val target = tile.target) {
            is LaunchTarget.ExternalApp -> if (resolver.isInstalled(target.packageName)) {
                LaunchResult.Launched
            } else {
                LaunchResult.NotAvailable("${tile.title} is not set up on this TV yet.")
            }
            is LaunchTarget.InApp -> LaunchResult.Launched
            is LaunchTarget.SystemSetting -> LaunchResult.Launched
        }
}

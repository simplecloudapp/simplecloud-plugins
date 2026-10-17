package app.simplecloud.plugin.api.shared.permission

/**
 * @author Niklas Nieberler
 */

fun interface PermissionChecker<P> {

    /**
     * Checks the permission by a player
     * @param player to check the permission
     * @param name of the permission
     */
    fun checkPermission(player: P, name: String): Boolean

}
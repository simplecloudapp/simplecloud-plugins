package app.simplecloud.plugin.api.shared.placeholder.async

/**
 * @author Niklas Nieberler
 */

fun interface AsyncPlaceholderHandler<T> {

    /**
     * Gets the return value for a placeholder
     * @param value of the placeholder key method
     */
    suspend fun handle(value: T): Any?

}
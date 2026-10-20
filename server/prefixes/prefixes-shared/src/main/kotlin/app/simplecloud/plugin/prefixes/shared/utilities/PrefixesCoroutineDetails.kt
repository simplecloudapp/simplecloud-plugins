package app.simplecloud.plugin.prefixes.shared.utilities

import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

data class PrefixesCoroutineDetails(
    val player: String?,
    val reason: String,
) : AbstractCoroutineContextElement(PrefixesCoroutineDetails) {

    companion object Key : CoroutineContext.Key<PrefixesCoroutineDetails>
}

package it.unibo.jakta.agents.bdi.intentions.impl

import it.unibo.jakta.agents.bdi.events.Event
import it.unibo.jakta.agents.bdi.intentions.Intention
import it.unibo.jakta.agents.bdi.intentions.IntentionID
import it.unibo.jakta.agents.bdi.intentions.IntentionPool
import it.unibo.tuprolog.unify.Unificator.Companion.mguWith

internal data class IntentionPoolImpl(
    val from: Map<IntentionID, Intention> = emptyMap(),
) : IntentionPool, LinkedHashMap<IntentionID, Intention>(from) {

    override fun updateIntention(intention: Intention): IntentionPool =
        IntentionPoolImpl(this + Pair(intention.id, intention))

    override fun nextIntention(): Intention = this.entries.first { it.value.waitingFor == null }.value

    override fun pop(): IntentionPool = IntentionPoolImpl(this - nextIntention().id)

    override fun deleteIntention(intentionID: IntentionID): IntentionPool =
        IntentionPoolImpl(this - intentionID)

    override fun anyRunning() = this.any { it.value.waitingFor == null }

    override fun resumeAll(waitingFor: Event): IntentionPool {
        var res: IntentionPool = this
        this.values.forEach {
            if (it.waitingFor != null && it.waitingFor!!.trigger::class == waitingFor.trigger::class) {
                val substitution = it.waitingFor!!.trigger.value.mguWith(waitingFor.trigger.value)
                if (substitution.isSuccess) {
                    res = res.updateIntention(it.copy(waitingFor = null).applySubstitution(substitution))
                }
            }
        }
        return res
    }

    override fun toString(): String = from.values.joinToString(separator = "\n")
}

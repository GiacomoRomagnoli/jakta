package it.unibo.jakta.agents.bdi.intentions

import it.unibo.jakta.agents.bdi.events.Event
import it.unibo.jakta.agents.bdi.intentions.impl.IntentionPoolImpl

interface IntentionPool : Map<IntentionID, Intention> {
    fun updateIntention(intention: Intention): IntentionPool

    fun nextIntention(): Intention

    fun pop(): IntentionPool

    fun deleteIntention(intentionID: IntentionID): IntentionPool

    fun anyRunning(): Boolean

    fun resumeAll(waitingFor: Event): IntentionPool

    companion object {
        fun empty(): IntentionPool = IntentionPoolImpl()

        fun of(intentions: Map<IntentionID, Intention>): IntentionPool = IntentionPoolImpl(intentions)
        fun of(vararg intentions: Intention): IntentionPool = of(intentions.asList())
        fun of(intentions: List<Intention>): IntentionPool = IntentionPoolImpl(intentions.associateBy { it.id })
    }
}

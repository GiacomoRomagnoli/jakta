package io.github.anitvam.agents.bdi.context

import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.events.EventQueue
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.actions.InternalAction
import io.github.anitvam.agents.bdi.actions.InternalActions
import io.github.anitvam.agents.bdi.impl.AgentContextImpl
import io.github.anitvam.agents.bdi.intentions.IntentionPool
import io.github.anitvam.agents.bdi.plans.PlanLibrary
import io.github.anitvam.agents.bdi.plans.Plan
import io.github.anitvam.agents.bdi.perception.Perception

/**
 * The Context is the actual state of a BDI Agent's structures.
 */
interface AgentContext {

    /** [BeliefBase] of the BDI Agent */
    val beliefBase: BeliefBase

    /** [Event]s on which the BDI Agent reacts */
    val events: EventQueue

    /** Component of the Agent that let it perceive the environment */
    val perception: Perception

    /** [Plan]s collection of the BDI Agent */
    val planLibrary: PlanLibrary

    val intentions: IntentionPool

    val internalActions: Map<String, InternalAction>

    fun copy(
        beliefBase: BeliefBase = this.beliefBase,
        events: EventQueue = this.events,
        planLibrary: PlanLibrary = this.planLibrary,
        perception: Perception = this.perception,
        intentions: IntentionPool = this.intentions,
        internalActions: Map<String, InternalAction> = this.internalActions,
    ): AgentContext = of(beliefBase, events, planLibrary, perception, intentions, internalActions)

    companion object {
        fun of(
            beliefBase: BeliefBase = BeliefBase.empty(),
            events: EventQueue = emptyList(),
            planLibrary: PlanLibrary = PlanLibrary.empty(),
            perception: Perception = Perception.empty(),
            intentions: IntentionPool = IntentionPool.empty(),
            internalActions: Map<String, InternalAction> = InternalActions.default(),
        ): AgentContext = AgentContextImpl(beliefBase, events, planLibrary, perception, intentions, internalActions)
    }
}
package io.github.anitvam.agents.bdi.executionstrategies.impl

import io.github.anitvam.agents.bdi.AgentLifecycle
import io.github.anitvam.agents.bdi.Mas
import io.github.anitvam.agents.bdi.executionstrategies.ExecutionStrategy
import io.github.anitvam.agents.fsm.Activity
import io.github.anitvam.agents.fsm.Runner

internal class OneThreadPerAgentImpl : ExecutionStrategy {
    override fun dispatch(mas: Mas) {
        mas.agents.forEach { agent ->
            val agentLC = AgentLifecycle.of(agent)
            Runner.threadOf(
                Activity.of {
                    val sideEffects = agentLC.reason(mas.environment, it)
                    applySideEffects(sideEffects, mas)
                }
            ).run()
        }
    }
}

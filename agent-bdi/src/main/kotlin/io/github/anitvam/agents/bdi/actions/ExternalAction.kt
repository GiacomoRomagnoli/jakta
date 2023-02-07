package io.github.anitvam.agents.bdi.actions

import io.github.anitvam.agents.bdi.Agent
import io.github.anitvam.agents.bdi.Message
import io.github.anitvam.agents.bdi.actions.effects.BroadcastMessage
import io.github.anitvam.agents.bdi.actions.effects.EnvironmentChange
import io.github.anitvam.agents.bdi.actions.effects.RemoveAgent
import io.github.anitvam.agents.bdi.actions.effects.SendMessage
import io.github.anitvam.agents.bdi.actions.effects.SpawnAgent
import it.unibo.tuprolog.solve.Signature

abstract class ExternalAction(override val signature: Signature) :
    AbstractAction<EnvironmentChange, ExternalResponse, ExternalRequest>(signature) {

    constructor(name: String, arity: Int) : this(Signature(name, arity))

    protected fun addAgent(agent: Agent) = effects.add(SpawnAgent(agent))
    protected fun removeAgent(agentName: String) = effects.add(RemoveAgent(agentName))
    protected fun sendMessage(agentName: String, message: Message) =
        effects.add(SendMessage(message, agentName))
    protected fun broadcastMessage(message: Message) = effects.add(BroadcastMessage(message))

    override fun toString(): String = "ExternalAction(${signature.name}, ${signature.arity})"
}

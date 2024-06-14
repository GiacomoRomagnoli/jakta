package it.unibo.jakta.agents.bdi.actions

import it.unibo.jakta.agents.bdi.actions.impl.AbstractExternalAction
import it.unibo.jakta.agents.bdi.messages.Achieve
import it.unibo.jakta.agents.bdi.messages.Message
import it.unibo.jakta.agents.bdi.messages.Tell

object ExternalActions {
    object Send : AbstractExternalAction("send", 3) {
        override fun action(request: ExternalRequest) {
            if (request.arguments[0].isAtom && request.arguments[1].isAtom && request.arguments[2].isStruct) {
                val receiver = request.arguments[0].castToAtom()
                val type = request.arguments[1].castToAtom()
                val message = request.arguments[2].castToStruct()
                when (type.value) {
                    "tell" -> sendMessage(receiver.value, Message(request.sender, Tell, message))
                    "achieve" -> sendMessage(
                        receiver.value,
                        Message(request.sender, Achieve, message),
                    )
                }
            }
        }
    }

    fun default() = mapOf(
        Send.signature.name to Send,
    )
}

package it.unibo.jakta.agents.bdi.actions

import it.unibo.jakta.agents.bdi.actions.impl.AbstractExternalAction
import it.unibo.jakta.agents.bdi.messages.Achieve
import it.unibo.jakta.agents.bdi.messages.Message
import it.unibo.jakta.agents.bdi.messages.Tell
import it.unibo.tuprolog.core.List
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.parsing.parse

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

    object AllNames : AbstractExternalAction("all_names", 1) {
        override fun action(request: ExternalRequest) {
            if (request.arguments[0].isVar) {
                val allNames = request.arguments[0].castToVar()
                val nameList = List.of((request.environment.agentIDs.keys - request.sender).map { Term.parse(it) })
                addResults(Substitution.of(allNames, nameList))
            }
        }
    }

    fun default() = mapOf(
        Send.signature.name to Send,
        AllNames.signature.name to AllNames,
    )
}

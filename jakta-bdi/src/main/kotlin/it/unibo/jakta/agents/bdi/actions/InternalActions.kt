package it.unibo.jakta.agents.bdi.actions

import it.unibo.jakta.agents.bdi.actions.impl.AbstractInternalAction
import it.unibo.tuprolog.core.Integer
import it.unibo.tuprolog.core.Numeric
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Truth
import it.unibo.tuprolog.unify.Unificator.Companion.unifyWith
import it.unibo.tuprolog.core.Atom as Atom2pkt
import it.unibo.tuprolog.core.List as List2pkt

object InternalActions {
    object Print : AbstractInternalAction("print", 2) {
        override fun action(request: InternalRequest) {
            val payload = request.arguments.joinToString(" ") {
                when {
                    it.isAtom -> it.castToAtom().value
                    else -> it.toString()
                }
            }
            println("[${request.agent.name}] $payload")
        }
    }

    object Fail : AbstractInternalAction("fail", 0) {
        override fun action(request: InternalRequest) {
            result = Substitution.failed()
        }
    }

    object Stop : AbstractInternalAction("stop", 0) {
        override fun action(request: InternalRequest) {
            stopAgent()
        }
    }

    object Pause : AbstractInternalAction("pause", 0) {
        override fun action(request: InternalRequest) {
            pauseAgent()
        }
    }

    object Sleep : AbstractInternalAction("sleep", 1) {
        override fun action(request: InternalRequest) {
            if (request.arguments[0].isInteger) {
                sleepAgent(request.arguments[0].castToInteger().value.toLong())
            }
        }
    }

    class Random : AbstractInternalAction("random", 1) {
        var generator: kotlin.random.Random = kotlin.random.Random
        override fun action(request: InternalRequest) {
            if (request.arguments[0].isVar) {
                val variable = request.arguments[0].castToVar()
                addResults(Substitution.of(variable, Numeric.of(generator.nextDouble())))
            }
        }
    }

    class RandomSeed(randomAction: Random) : AbstractInternalAction("randomSeed", 1) {
        private var random = randomAction
        override fun action(request: InternalRequest) {
            if (request.arguments[0].isInteger) {
                val seed = request.arguments[0].castToInteger().value.toLong()
                random.generator = kotlin.random.Random(seed)
            }
        }
    }

    object Atom : AbstractInternalAction("atom", 1) {
        override fun action(request: InternalRequest) {
            if (!request.arguments[0].isAtom) {
                addResults(Substitution.failed())
            }
        }
    }

    object Structure : AbstractInternalAction("structure", 1) {
        override fun action(request: InternalRequest) {
            if (!request.arguments[0].isStruct) {
                addResults(Substitution.failed())
            }
        }
    }

    object List : AbstractInternalAction("list", 1) {
        override fun action(request: InternalRequest) {
            if (!request.arguments[0].isList) {
                addResults(Substitution.failed())
            }
        }
    }

    object Ground : AbstractInternalAction("ground", 1) {
        override fun action(request: InternalRequest) {
            if (!request.arguments[0].isGround) {
                addResults(Substitution.failed())
            }
        }
    }

    object Number : AbstractInternalAction("number", 1) {
        override fun action(request: InternalRequest) {
            if (!request.arguments[0].isNumber) {
                addResults(Substitution.failed())
            }
        }
    }

    object Type : AbstractInternalAction("type", 2) {
        override fun action(request: InternalRequest) {
            val type = request.arguments[1]
            val term = request.arguments[0]
            val value = when {
                term.isNumber -> Atom2pkt.of("number")
                term.isAtom -> Atom2pkt.of("atom")
                term.isVar -> Atom2pkt.of("variable")
                term.isList -> Atom2pkt.of("list")
                term.isTuple -> Atom2pkt.of("tuple")
                term.isIndicator -> Atom2pkt.of("indicator")
                term.isClause -> Atom2pkt.of("clause")
                term.isStruct -> Atom2pkt.of("structure")
                else -> Atom2pkt.of("unknown")
            }
            when (type.unifyWith(value)) {
                null -> addResults(Substitution.failed())
                else -> if (type.isVar) {
                    addResults(Substitution.of(type.castToVar(), value))
                }
            }
        }
    }

    object Eval : AbstractInternalAction("eval", 2) {
        override fun action(request: InternalRequest) {
            if (request.arguments[1].isStruct) {
                val result = request.arguments[0]
                val expression = request.arguments[1].castToStruct()
                val solution = request.agent.context.beliefBase.solve(expression)
                val value = when (solution.isYes) {
                    true -> Truth.TRUE
                    false -> Truth.FALSE
                }
                when (result.unifyWith(value)) {
                    null -> addResults(Substitution.failed())
                    else -> if (result.isVar) {
                        addResults(Substitution.of(result.castToVar(), value))
                    }
                }
            } else {
                addResults(Substitution.failed())
            }
        }
    }

    object AddSource : AbstractInternalAction("add_source", 3) {
        private fun apply(term: Struct, source: Atom2pkt) =
            term.addFirst(Struct.of("source", source))

        override fun action(request: InternalRequest) {
            if (request.arguments[1].isAtom && request.arguments[2].isVar) {
                val source = request.arguments[1].castToAtom()
                val result = request.arguments[2].castToVar()
                if (request.arguments[0].isList) {
                    val beliefs = request.arguments[0].castToList().unfoldedList
                    val beliefsWithSource = beliefs.flatMap {
                        if (it.isStruct && !it.isEmptyList) {
                            listOf(apply(it.castToStruct(), source))
                        } else {
                            listOf()
                        }
                    }
                    addResults(Substitution.of(result, List2pkt.of(beliefsWithSource)))
                } else if (request.arguments[0].isStruct) {
                    val belief = request.arguments[0].castToStruct()
                    addResults(Substitution.of(result, apply(belief, source)))
                }
            }
        }
    }

    object MyName : AbstractInternalAction("my_name", 1) {
        override fun action(request: InternalRequest) {
            val myName = request.arguments[0]
            when (val value = myName.unifyWith(Atom2pkt.of(request.agent.name))) {
                null -> addResults(Substitution.failed())
                else -> if (myName.isVar) {
                    addResults(Substitution.of(myName.castToVar(), value))
                }
            }
        }
    }

    object SubString : AbstractInternalAction("substring", 3) {
        override fun action(request: InternalRequest) {
            val substring = request.arguments[0].toString()
            val string = request.arguments[1].toString()
            when (val range = string.toRegex().find(substring)?.range) {
                null -> addResults(Substitution.failed())
                else -> if (request.arguments.size == 3) {
                    val start = request.arguments[2]
                    when (val value = start.unifyWith(Integer.of(range.first))) {
                        null -> addResults(Substitution.failed())
                        else -> if (start.isVar) {
                            addResults(Substitution.of(start.castToVar(), value))
                        }
                    }
                }
            }
        }
    }

    fun default(): Map<String, InternalAction> {
        val random = Random()
        val randomSeed = RandomSeed(random)
        return mapOf(
            Print.signature.name to Print,
            Fail.signature.name to Fail,
            Stop.signature.name to Stop,
            Pause.signature.name to Pause,
            Sleep.signature.name to Sleep,
            random.signature.name to random,
            randomSeed.signature.name to randomSeed,
            Atom.signature.name to Atom,
            Structure.signature.name to Structure,
            Ground.signature.name to Ground,
            Number.signature.name to Number,
            Type.signature.name to Type,
            Eval.signature.name to Eval,
            AddSource.signature.name to AddSource,
            MyName.signature.name to MyName,
            SubString.signature.name to SubString,
        )
    }
}

package it.unibo.jakta.agents.bdi.actions

import it.unibo.jakta.agents.bdi.actions.impl.AbstractInternalAction
import it.unibo.tuprolog.core.Numeric
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.Truth
import it.unibo.tuprolog.core.parsing.parse

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
            if (request.arguments[1].isVar) {
                val type = request.arguments[1].castToVar()
                val term = request.arguments[0]
                when {
                    term.isNumber -> addResults(Substitution.of(type, Term.parse("number")))
                    term.isAtom -> addResults(Substitution.of(type, Term.parse("atom")))
                    term.isVar -> addResults(Substitution.of(type, Term.parse("variable")))
                    term.isList -> addResults(Substitution.of(type, Term.parse("list")))
                    term.isTuple -> addResults(Substitution.of(type, Term.parse("tuple")))
                    term.isIndicator -> addResults(Substitution.of(type, Term.parse("indicator")))
                    term.isClause -> addResults(Substitution.of(type, Term.parse("clause")))
                    term.isStruct -> addResults(Substitution.of(type, Term.parse("structure")))
                    else -> addResults(Substitution.of(type, Term.parse("unknown")))
                }
            }
        }
    }

    object Eval : AbstractInternalAction("eval", 2) {
        override fun action(request: InternalRequest) {
            if (request.arguments[0].isVar) {
                val value = request.arguments[0].castToVar()
                val expression = request.arguments[1].castToStruct()
                val solution = request.agent.context.beliefBase.solve(expression)
                when (solution.isYes) {
                    true -> addResults(Substitution.of(value, Truth.TRUE))
                    false -> addResults(Substitution.of(value, Truth.FALSE))
                }
            }
        }
    }

    object AddSource : AbstractInternalAction("add_source", 3) {
        private fun apply(term: Struct, source: it.unibo.tuprolog.core.Atom) =
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
                    addResults(Substitution.of(result, it.unibo.tuprolog.core.List.of(beliefsWithSource)))
                } else if (request.arguments[0].isStruct) {
                    val belief = request.arguments[0].castToStruct()
                    addResults(Substitution.of(result, apply(belief, source)))
                }
            }
        }
    }

    object MyName : AbstractInternalAction("my_name", 1) {
        override fun action(request: InternalRequest) {
            if (request.arguments[0].isVar) {
                val myName = request.arguments[0].castToVar()
                addResults(Substitution.of(myName, Term.parse(request.agent.name)))
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
        )
    }
}

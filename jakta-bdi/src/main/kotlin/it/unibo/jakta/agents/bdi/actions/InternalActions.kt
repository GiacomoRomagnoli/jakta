package it.unibo.jakta.agents.bdi.actions

import it.unibo.jakta.agents.bdi.actions.impl.AbstractInternalAction
import it.unibo.tuprolog.core.Integer
import it.unibo.tuprolog.core.Numeric
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.Truth
import it.unibo.tuprolog.core.parsing.parse
import it.unibo.tuprolog.unify.Unificator.Companion.mguWith
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

    class SetRandomSeed(randomAction: Random) : AbstractInternalAction("set_random_seed", 1) {
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
            addResults(type.mguWith(value))
        }
    }

    object Eval : AbstractInternalAction("eval", 2) {
        override fun action(request: InternalRequest) {
            when (request.arguments[1].isStruct) {
                false -> addResults(Substitution.failed())
                true -> {
                    val result = request.arguments[0]
                    val expression = request.arguments[1].castToStruct()
                    val solution = request.agent.context.beliefBase.solve(expression)
                    val value = when (solution.isYes) {
                        true -> Truth.TRUE
                        false -> Truth.FALSE
                    }
                    addResults(result.mguWith(value))
                }
            }
        }
    }

    object AddNestedSource : AbstractInternalAction("add_nested_source", 3) {
        private fun apply(belief: Struct, source: Atom2pkt) =
            when (belief.arity > 0 && belief[0].isStruct && belief[0].castToStruct().functor == "source") {
                true -> belief.setArgs(listOf(Struct.of("source", source, belief[0])) + belief.args - belief[0])
                false -> belief.addFirst(Struct.of("source", source))
            }

        override fun action(request: InternalRequest) {
            when (request.arguments[1].isAtom) {
                false -> addResults(Substitution.failed())
                true -> {
                    val beliefs = request.arguments[0]
                    val source = request.arguments[1].castToAtom()
                    val result = request.arguments[2]
                    val value: Term? = when {
                        beliefs.isList -> {
                            List2pkt.of(
                                beliefs.castToList().unfoldedList
                                    .filter { it.isStruct && !it.isEmptyList }
                                    .map { apply(it.castToStruct(), source) },
                            )
                        }
                        beliefs.isStruct -> apply(beliefs.castToStruct(), source)
                        else -> null
                    }
                    when (value) {
                        null -> addResults(Substitution.failed())
                        else -> addResults(result.mguWith(value))
                    }
                }
            }
        }
    }

    object MyName : AbstractInternalAction("my_name", 1) {
        override fun action(request: InternalRequest) {
            val myName = request.arguments[0]
            addResults(myName.mguWith(Atom2pkt.of(request.agent.name)))
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
                    addResults(start.mguWith(Integer.of(range.first)))
                }
            }
        }
    }

    object Term2String : AbstractInternalAction("term2string", 2) {
        override fun action(request: InternalRequest) {
            val term = request.arguments[0]
            val string = request.arguments[1]
            when {
                term.isGround -> addResults(string mguWith Atom2pkt.of(term.toString()))
                string.isAtom -> addResults(term mguWith Term.parse(string.castToAtom().value))
                else -> addResults(Substitution.failed())
            }
        }
    }

    sealed class LowerCaseToUpperCase(name: String, private val reverse: Boolean = false) :
        AbstractInternalAction(name, 2) {
        private fun transform(string: String, condition: Boolean) =
            when (condition) {
                true -> string.uppercase()
                false -> string.lowercase()
            }

        override fun action(request: InternalRequest) {
            val string = request.arguments[0]
            val result = request.arguments[1]
            when {
                string.isAtom ->
                    addResults(result mguWith Atom2pkt.of(transform(string.castToAtom().value, reverse)))
                result.isAtom ->
                    addResults(string mguWith Atom2pkt.of(transform(result.castToAtom().value, !reverse)))
            }
        }
    }

    object LowerCase : LowerCaseToUpperCase("lower_case")
    object UpperCase : LowerCaseToUpperCase("upper_case", true)

    object Replace : AbstractInternalAction("replace", 4) {
        override fun action(request: InternalRequest) {
            if (request.arguments[0].isAtom && request.arguments[1].isAtom && request.arguments[2].isAtom) {
                val s1 = request.arguments[0].castToAtom().value
                val s2 = request.arguments[1].castToAtom().value
                val s3 = request.arguments[2].castToAtom().value
                val s4 = request.arguments[3]
                val value = Atom2pkt.of(s1.replace(s2, s3))
                addResults(s4 mguWith value)
            } else {
                addResults(Substitution.failed())
            }
        }
    }

    fun default(): Map<String, InternalAction> {
        val random = Random()
        val setRandomSeed = SetRandomSeed(random)
        return mapOf(
            Print.signature.name to Print,
            Fail.signature.name to Fail,
            Stop.signature.name to Stop,
            Pause.signature.name to Pause,
            Sleep.signature.name to Sleep,
            random.signature.name to random,
            setRandomSeed.signature.name to setRandomSeed,
            Atom.signature.name to Atom,
            Structure.signature.name to Structure,
            Ground.signature.name to Ground,
            Number.signature.name to Number,
            Type.signature.name to Type,
            Eval.signature.name to Eval,
            AddNestedSource.signature.name to AddNestedSource,
            MyName.signature.name to MyName,
            SubString.signature.name to SubString,
            Term2String.signature.name to Term2String,
            UpperCase.signature.name to UpperCase,
            LowerCase.signature.name to LowerCase,
            Replace.signature.name to Replace,
        )
    }
}

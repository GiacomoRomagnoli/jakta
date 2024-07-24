package it.unibo.jakta.agents.bdi.dsl

import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.unify.Unificator.Companion.mguWith

fun main() {
    mas {
        agent("sender") {
            goals {
                achieve("ask"("question"))
            }
            plans {
                +achieve("ask"("question")) then {
                    execute("print"("asking a question..."))
                    execute("send"("receiver", "askOne", "good"(X)))
                    execute("print"("the answer is ", X))
                }
            }
        }
        agent("receiver") {
            actions {
                action("respond", 1) {
                    val arg: Term = argument(0)
                    addResults(arg mguWith Atom.of("ice-cream"))
                }
            }
            plans {
                +test("good"("source"(S), Y)) then {
                    execute("print"("responding..."))
                    execute("respond"(Y))
                }
            }
        }
    }.start(true)
}

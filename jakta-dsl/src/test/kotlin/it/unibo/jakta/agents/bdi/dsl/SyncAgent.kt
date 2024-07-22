package it.unibo.jakta.agents.bdi.dsl

fun main() {
    mas {
        agent("sender") {
            goals {
                achieve("ask"("question"))
            }
            plans {
                +achieve("ask"("question")) then {
                    execute("send"("receiver", "askOne", "good"(X)))
                    execute("print"(X))
                }
            }
        }
        agent("receiver") {
            plans {
                +test("good"("source"(S), Y)) then {
                    execute("send"(S, "tell", "good"("ice-cream")))
                }
            }
        }
    }.start()
}

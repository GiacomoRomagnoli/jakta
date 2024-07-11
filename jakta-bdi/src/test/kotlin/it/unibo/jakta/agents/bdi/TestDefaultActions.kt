package it.unibo.jakta.agents.bdi

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import it.unibo.jakta.agents.bdi.actions.InternalActions
import it.unibo.jakta.agents.bdi.actions.InternalRequest
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.List
import it.unibo.tuprolog.core.Numeric
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Var
import org.gciatto.kt.math.BigDecimal
import kotlin.random.Random

class TestDefaultActions : DescribeSpec({
    lateinit var agent: Agent
    beforeEach {
        agent = Agent.of()
    }

    describe("Random") {
        it("should generate random numbers between 0 and 1") {
            val action = InternalActions.Random()
            val x = Var.of("X")
            val request = InternalRequest.of(agent, null, x)
            val result = action.execute(request).substitution[x]
            result shouldNotBe null
            result!!.isReal shouldBe true
            result.castToReal().value shouldBeGreaterThanOrEqualTo BigDecimal.of(0)
            result.castToReal().value shouldBeLessThanOrEqualTo BigDecimal.of(1)
        }
    }

    describe("SetRandomSeed") {
        it("should cause Random to generate a precise sequence of numbers") {
            val random = InternalActions.Random()
            val action = InternalActions.SetRandomSeed(random)
            val actionRequest = InternalRequest.of(agent, null, Numeric.of(1))
            val generator = Random(1)
            action.action(actionRequest)
            for (i in 1..100) {
                val x = Var.of("X")
                val randomRequest = InternalRequest.of(agent, null, x)
                val nextNumber = random.execute(randomRequest).substitution[x]!!.castToReal().value
                nextNumber shouldBeEqualComparingTo BigDecimal.of(generator.nextDouble())
            }
        }
    }

    describe("Atom") {
        it("should not fail if argument is an Atom") {
            val request = InternalRequest.of(agent, null, Atom.of("ATOM"))
            InternalActions.Atom.execute(request).substitution shouldBe Substitution.empty()
        }
        it("should fail if argument is not an Atom") {
            val request = InternalRequest.of(agent, null, Numeric.of(1))
            InternalActions.Atom.execute(request).substitution shouldBe Substitution.failed()
        }
    }

    describe("Structure") {
        it("should not fail if argument is a Struct") {
            val request = InternalRequest.of(agent, null, Struct.of("functor", Atom.of("arg")))
            InternalActions.Structure.execute(request).substitution shouldBe Substitution.empty()
        }
        it("should fail if argument is not a Struct") {
            val request = InternalRequest.of(agent, null, Numeric.of(1))
            InternalActions.Structure.execute(request).substitution shouldBe Substitution.failed()
        }
    }

    describe("List") {
        it("should not fail if argument is a List") {
            val request = InternalRequest.of(agent, null, List.of())
            InternalActions.List.execute(request).substitution shouldBe Substitution.empty()
        }
        it("should fail if argument is not a List") {
            val request = InternalRequest.of(agent, null, Numeric.of(1))
            InternalActions.List.execute(request).substitution shouldBe Substitution.failed()
        }
    }

    describe("Ground") {
        it("should not fail if argument is ground") {
            val request = InternalRequest.of(agent, null, Atom.of("atom"))
            InternalActions.Ground.execute(request).substitution shouldBe Substitution.empty()
        }
        it("should fail if argument is not ground") {
            val request = InternalRequest.of(agent, null, Var.of("X"))
            InternalActions.Ground.execute(request).substitution shouldBe Substitution.failed()
        }
    }

    describe("Number") {
        it("should not fail if argument is a number") {
            val request = InternalRequest.of(agent, null, Numeric.of(1))
            InternalActions.Number.execute(request).substitution shouldBe Substitution.empty()
        }
        it("should fail if argument is not a number") {
            val request = InternalRequest.of(agent, null, Atom.of("atom"))
            InternalActions.Number.execute(request).substitution shouldBe Substitution.failed()
        }
    }

    describe("Type") {
        it("should unify the second argument with first argument type") {
            val type = Var.of("X")
            val request1 = InternalRequest.of(agent, null, Numeric.of(1), type)
            val request2 = InternalRequest.of(agent, null, List.of(), type)
            InternalActions.Type.execute(request1).substitution[type] shouldBe Atom.of("number")
            InternalActions.Type.execute(request2).substitution[type] shouldBe Atom.of("list")
        }
    }
})

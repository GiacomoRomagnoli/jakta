package it.unibo.jakta.agents.bdi

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import it.unibo.jakta.agents.bdi.actions.InternalActions
import it.unibo.jakta.agents.bdi.actions.InternalRequest
import it.unibo.tuprolog.core.Numeric
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
})

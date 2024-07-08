package it.unibo.jakta.agents.bdi

import it.unibo.jakta.agents.bdi.beliefs.Belief
import it.unibo.jakta.agents.bdi.beliefs.BeliefBase
import it.unibo.jakta.agents.bdi.environment.Environment
import it.unibo.jakta.agents.bdi.events.Event
import it.unibo.jakta.agents.bdi.executionstrategies.ExecutionStrategy
import it.unibo.jakta.agents.bdi.goals.Achieve
import it.unibo.jakta.agents.bdi.goals.Test
import it.unibo.jakta.agents.bdi.plans.Plan
import it.unibo.jakta.agents.bdi.plans.PlanLibrary

fun main() {
    val belief = Belief.fromSelfSource(Jakta.parseStruct("good(ice-cream)"))
    val desire = Jakta.parseStruct("eat(ice-cream)")
    val agent = Agent.of(
        beliefBase = BeliefBase.of(belief),
        planLibrary = PlanLibrary.of(
            Plan.ofAchievementGoalInvocation(
                desire,
                listOf(Test.of(belief)),
            ),
        ),
        events = listOf(
            Event.ofAchievementGoalInvocation(Achieve.of(desire)),
        ),
    )

    Mas.of(ExecutionStrategy.oneThreadPerMas(), Environment.of(), agent)
        .start(true)
}

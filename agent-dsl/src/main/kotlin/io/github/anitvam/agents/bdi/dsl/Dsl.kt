package io.github.anitvam.agents.bdi.dsl

import io.github.anitvam.agents.bdi.Mas

internal fun mas(f: MasScope.() -> Unit): Mas =
    MasScope().also(f).build()

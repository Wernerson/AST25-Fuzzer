package net.sebyte.run

import net.sebyte.ast.Select
import net.sebyte.cfg.GeneratorConfig
import net.sebyte.gen.OutputMap
import net.sebyte.gen.SelectGenerator
import net.sebyte.gen.Tables
import net.sebyte.mut.Mutator

interface Legislator : Iterator<Select> {
    val size: Int?
        get() = null

    fun notice(query: Select, verdict: Verdict) {} // do nothing by default
}

class SimpleLegislator(
    private val numberOfQueries: Int,
    cfg: GeneratorConfig,
    tables: Tables
) : Legislator {
    override val size = numberOfQueries
    private val generator = SelectGenerator(cfg, tables)
    private val queries = iterator {
        for (i in 0 until numberOfQueries) yield(generator.select())
    }

    override fun hasNext() = queries.hasNext()
    override fun next() = queries.next()
}

class MutableLegislator(
    private val initialQueries: Int,
    private val mutations: Int,
    cfg: GeneratorConfig,
    tables: Tables
) : Legislator {
    private val generator = SelectGenerator(cfg, tables)
    private val mutator = Mutator(cfg, tables)
    private val queue = ArrayDeque<Select>(initialQueries)
    private val maps: MutableMap<Select, OutputMap> = mutableMapOf()

    init {
        for (i in 0 until initialQueries) {
            val map: OutputMap = mutableMapOf()
            val query = generator.select(map)
            queue.add(query)
            maps[query] = map
        }
    }

    override fun hasNext() = queue.isNotEmpty()
    override fun next() = queue.removeFirst()

    override fun notice(query: Select, verdict: Verdict) = when (verdict) {
        Verdict.UNINTERESTING -> {} //do nothing
        Verdict.BUGGY,
        Verdict.INTERESTING -> {
            val map = maps[query]!!
            maps.remove(query)
            for (i in 0..mutations) {
                val mutation = mutator.mutate(query, map)
                queue.add(mutation)
                maps[mutation] = map
            }
        }
    }
}
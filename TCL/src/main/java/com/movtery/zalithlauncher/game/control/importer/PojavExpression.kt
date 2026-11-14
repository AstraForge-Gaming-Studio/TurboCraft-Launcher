package com.movtery.zalithlauncher.game.control.importer

/** Small, bounded expression parser: never executes code from an imported file. */
internal object PojavExpression {
    fun evaluate(expression: String, variables: Map<String, Double>, density: Double): Double {
        require(expression.length <= 1024) { "Expression is too long" }
        val expanded = Regex("\\$\\{([a-zA-Z_]+)}").replace(expression) {
            (variables[it.groupValues[1]] ?: error("Unknown variable ${it.value}")).toString()
        }
        return Parser(expanded, density).parse()
    }

    private class Parser(private val input: String, private val density: Double) {
        private var pos = 0
        private var depth = 0
        fun parse(): Double {
            val result = sum()
            space()
            require(pos == input.length && result.isFinite()) { "Invalid expression" }
            return result
        }
        private fun space() { while (pos < input.length && input[pos].isWhitespace()) pos++ }
        private fun take(c: Char): Boolean {
            space()
            if (pos < input.length && input[pos] == c) { pos++; return true }
            return false
        }
        private fun sum(): Double {
            var value = product()
            while (true) value = when {
                take('+') -> value + product()
                take('-') -> value - product()
                else -> return value
            }
        }
        private fun product(): Double {
            var value = atom()
            while (true) value = when {
                take('*') -> value * atom()
                take('/') -> value / atom()
                else -> return value
            }
        }
        private fun atom(): Double {
            require(++depth <= 64) { "Expression is too deep" }
            try {
                if (take('+')) return atom()
                if (take('-')) return -atom()
                space()
                if (take('(')) {
                    val value = sum()
                    require(take(')')) { "Missing closing parenthesis" }
                    return value
                }
                if (pos < input.length && input[pos].isLetter()) {
                    val start = pos
                    while (pos < input.length && input[pos].isLetter()) pos++
                    val name = input.substring(start, pos)
                    require(take('(')) { "Expected function argument" }
                    val value = sum()
                    require(take(')')) { "Missing closing parenthesis" }
                    return when (name) { "px" -> value * density; "dp" -> value / density; else -> error("Unknown function $name") }
                }
                val start = pos
                while (pos < input.length && (input[pos].isDigit() || input[pos] == '.')) pos++
                require(start != pos) { "Expected number" }
                return input.substring(start, pos).toDouble()
            } finally { depth-- }
        }
    }
}

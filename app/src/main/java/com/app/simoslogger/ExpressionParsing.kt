package com.app.simoslogger

import kotlin.math.*

fun eval(str: String): Float {
    return object : Any() {
        var pos = -1
        var ch = 0
        fun nextChar() {
            ch = if (++pos < str.length) str[pos].code else -1
        }

        fun eat(charToEat: Int): Boolean {
            while (ch == ' '.code) nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Float {
            nextChar()
            val x = parseExpression()
            if (pos < str.length)
                throw RuntimeException("Unexpected: " + ch.toChar())
            return x
        }

        // Grammar:
        // expression = term | expression `+` term | expression `-` term
        // term = factor | term `*` factor | term `/` factor
        // factor = `+` factor | `-` factor | `(` expression `)`
        //        | number | functionName factor | factor `^` factor
        fun parseExpression(): Float {
            var x = parseTerm()
            while (true) {
                when {
                    eat('+'.code) -> x += parseTerm() // addition
                    eat('-'.code) -> x -= parseTerm() // subtraction
                    else -> return x
                }
            }
        }

        fun parseTerm(): Float {
            var x = parseFactor()
            while (true) {
                when {
                    eat('*'.code) -> x *= parseFactor() // multiplication
                    eat('/'.code) -> x /= parseFactor() // division
                    else -> return x
                }
            }
        }

        fun parseFactor(): Float {
            if (eat('+'.code))
                return parseFactor() // unary plus
            if (eat('-'.code))
                return -parseFactor() // unary minus
            var x: Float
            val startPos = pos
            if (eat('('.code)) { // parentheses
                x = parseExpression()
                eat(')'.code)
            } else if (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) { // numbers
                while (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) nextChar()
                x = str.substring(startPos, pos).toFloat()
            } else if (ch >= 'a'.code && ch <= 'z'.code) { // functions
                while (ch >= 'a'.code && ch <= 'z'.code) nextChar()
                val func = str.substring(startPos, pos)
                x = parseFactor()
                x = when (func) {
                    "sqrt" -> sqrt(x)
                    "sin" -> sin(Math.toRadians(x.toDouble())).toFloat()
                    "cos" -> cos(Math.toRadians(x.toDouble())).toFloat()
                    "tan" -> tan(Math.toRadians(x.toDouble())).toFloat()
                    else -> throw RuntimeException("Unknown function: $func")
                }
            } else {
                throw RuntimeException("Unexpected: " + ch.toChar())
            }
            if (eat('^'.code))
                x = x.pow(parseFactor()) // exponentiation

            return x
        }
    }.parse()
}
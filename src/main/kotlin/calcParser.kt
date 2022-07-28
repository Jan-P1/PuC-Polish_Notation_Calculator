import kotlin.collections.Iterator
import kotlin.Exception


//TEST
sealed class Token {

    @Override
    override fun toString(): String {
        return this.javaClass.simpleName
    }

    // Symbols
    object EQUALS : Token()


    // Literal
    data class DOUBLE_LIT(val value: Double) : Token()


    // Operator
    // Lexer: Soll Zeichen erkennt: +, -, *, /, ^, root, =, pi, checksum
    object ABSOLUTE : Token()
    object PLUS : Token()
    object MINUS : Token()
    object MULTIPLY : Token()
    object DIVIDES : Token()
    object POWER : Token()
    object FACTORIAL : Token()
    object CHECKSUM : Token()
    object ROOT : Token()


    // Control
    object EOF : Token()
}

class PeekableIterator<T>(val iter: Iterator<T>) {
    var lh: T? = null
    fun peek(): T? {
        lh = next()
        return lh
    }

    fun next(): T? {
        lh?.let { lh = null; return it }
        return if (iter.hasNext()) {
            iter.next()
        } else {
            null
        }
    }
}

class Lexer(input: String) {

    private val iter = PeekableIterator(input.iterator())
    var lh: Token? = null

    fun next(): Token {
        lh?.let { it -> lh = null; return it }
        if (iter.peek()?.isWhitespace() == true)
            chompWhitespace()

        val t = when (val c = iter.next()) {
            null -> Token.EOF
            '+' -> Token.PLUS
            '/' -> Token.DIVIDES
            '*' -> Token.MULTIPLY
            '^' -> Token.POWER
            '-' -> if (iter.peek()?.isWhitespace() == true) {
                Token.MINUS
            } else if (iter.peek()?.isDigit() == true) {
                lexDouble(c)
            } else throw Exception("Invalid Syntax")
            '=' -> Token.EQUALS
            '!' -> Token.FACTORIAL
            '|' -> if (iter.peek() == '|') {
                iter.next()
                Token.ABSOLUTE
            } else {
                throw Exception("Unknown Operator |$c")
            }
            else -> when {
                c.isLetter() -> {
                    lexText(c)
                }
                c.isDigit() -> {
                    lexDouble(c)
                }
                else -> throw Exception("Unexpected $c")
            }
        }
        println(t)
        return t
    }


    private fun lexDouble(first: Char): Token {
        var res = first.toString()
        var pointFound = false
        var hasPrefix = false
        while (true) {

            if(iter.peek()?.isWhitespace() == true || iter.peek() == null){
                return Token.DOUBLE_LIT(res.toDouble())}

            if (iter.peek()?.isLetter() == true)
                throw Exception("Found unexpected characters in a Double. Please make sure to seperate each part with a whitespace.")

            if (iter.peek()?.equals('.') == true) {
                if (pointFound)
                    throw Exception("Too many dots for a Double.")
                else pointFound = true
            }

            if (iter.peek()?.equals('-') == true) {
                if (hasPrefix)
                    throw Exception("Only one prefix allowed")
                hasPrefix = true
            }
            res += iter.next()
        }
    }

    private fun lexText(first: Char): Token {
        var res = first.toString()
        while (true) {
            if(iter.peek()?.isWhitespace() == true || iter.peek()?.equals(null) == true){
                break
            }
            res += iter.next()
        }

        return when (res) {
            "root" -> Token.ROOT
            "checksum" -> Token.CHECKSUM
            "pi" -> Token.DOUBLE_LIT(Math.PI)
            else -> throw Exception("Unknown Token")
        }
    }

    private fun chompWhitespace() {
        while (iter.peek()?.isWhitespace() == true) {
            iter.next()
        }
    }

    fun lookahead(): Token {
        lh = next()
        return lh ?: Token.EOF
    }
}

class CalcParser(private val lexer: Lexer) {

    fun quickParse(opCount: Int = 0): Double {
        var opC = opCount
        val token = lexer.next()
        println("$token")
        if(token is Token.DOUBLE_LIT) {
            if (lexer.lookahead() is Token.EOF) {
                return token.value
            } else throw Exception("Invalid Syntax")
        } else if(lexer.lookahead() is Token.EOF)
            throw Exception("WHERE'S THE GOD DAMN NUMBERS?!")

        ++opC

        var left: Double
        var right: Double? = null


        left = if (lexer.lookahead() !is Token.DOUBLE_LIT) {
            quickParse(opC)
        } else {
            (lexer.next() as Token.DOUBLE_LIT).value
        }

        if (token is Token.ABSOLUTE)
            return parseExpression(token, left, right)
        if (token is Token.CHECKSUM)
            return parseExpression(token, left, right)
        if (token is Token.FACTORIAL)
            return parseExpression(token, left, right)


        right = if (lexer.lookahead() !is Token.DOUBLE_LIT)
            quickParse(opC)
        else {
            (lexer.next() as Token.DOUBLE_LIT).value
        }

        if(opC == 1)
            if(token !is Token.ABSOLUTE && token !is Token.FACTORIAL && token !is Token.CHECKSUM)
                if(lexer.lookahead() != Token.EOF)
                    throw Exception("Invalid syntax")

        return parseExpression(token, left, right)
    }

    private fun parseExpression(operator: Token, left: Double, right: Double?): Double {
        if(right != null){
            return when (operator) {

                Token.DIVIDES ->
                    interp(
                        Expression.Division(
                            Expression.Number(left),
                            Expression.Number(right)
                        )
                    )

                Token.EQUALS ->
                    throw Exception("Gleichungen sind noch nicht implementiert")

                Token.MINUS ->
                    interp(
                        Expression.Subtraction(
                            Expression.Number(left),
                            Expression.Number(right)
                        )
                    )

                Token.MULTIPLY ->
                    interp(
                        Expression.Multiplication(
                            Expression.Number(left),
                            Expression.Number(right)
                        )
                    )

                Token.PLUS ->
                    interp(
                        Expression.Addition(
                            Expression.Number(left),
                            Expression.Number(right)
                        )
                    )

                Token.POWER ->
                    interp(
                        Expression.Power(
                            Expression.Number(left),
                            Expression.Number(right)
                        )
                    )

                Token.ROOT ->
                    interp(
                        Expression.Root(
                            Expression.Number(left),
                            Expression.Number(right)
                        )
                    )

                else ->
                    throw Exception("Not an operator")
            }
        }

        else{
            return when(operator){
                is Token.ABSOLUTE -> interp(
                    Expression.Absolute(
                        Expression.Number(left)
                    )
                )
                is Token.CHECKSUM -> interp(
                    Expression.Checksum(
                        Expression.Number(left)
                    )
                )
                is Token.FACTORIAL -> interp(
                    Expression.Factorial(
                        Expression.Number(left)
                    )
                )
                else -> throw Exception("Not an operator")
            }
        }
    }
}
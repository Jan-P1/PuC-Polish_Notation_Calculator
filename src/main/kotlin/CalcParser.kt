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
    data class DOUBLE_LIT(val double: Double) : Token()


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
    object WHITESPACE : Token()
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
        if (iter.peek()?.isWhitespace() == true || iter.peek()?.equals(null) == true)
            chompWhitespace()

        return when (val c = iter.next()) {
            null -> Token.EOF
            '+' -> Token.PLUS
            '/' -> Token.DIVIDES
            '*' -> Token.MULTIPLY
            '^' -> Token.POWER
            '-' -> Token.MINUS
            '=' -> Token.EQUALS
            '!' -> Token.FACTORIAL
            '|' -> if (iter.peek() == '|') {
                iter.next()
                chompWhitespace()
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
    }


    private fun lexDouble(first: Char): Token {
        var res = first.toString()
        var pointFound = false
        var hasPrefix = false
        while (true) {

            if(iter.peek()?.isWhitespace() == true){
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
            if(iter.peek()?.isWhitespace() == true){
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

    fun quickParse(): Double {
        println("starting new parse cycle")
        val token = lexer.next()
        if(token is Token.DOUBLE_LIT) {
            println("Checking if token is number")
            if (lexer.lookahead() is Token.EOF) {
                println("only one number")
                return token.double
            } else throw Exception("Invalid Syntax")
        }

        var left: Double
        var right: Double? = null

        println("break 1 ")
        if (lexer.lookahead() !is Token.DOUBLE_LIT) {
            println("break 2 ")
            left = quickParse()
        } else {
            println("break 3 ")
            left = (lexer.next() as Token.DOUBLE_LIT).double
        }
        println("break 4 ")
        if (token is Token.ABSOLUTE)
            return parseExpression(token, left, right)
        if (token is Token.CHECKSUM)
            return parseExpression(token, left, right)
        if (token is Token.FACTORIAL)
            return parseExpression(token, left, right)

        println("break 5 ")
        right = if (lexer.lookahead() !is Token.DOUBLE_LIT)
            quickParse()
        else {
            (lexer.next() as Token.DOUBLE_LIT).double
        }
        println("Return")
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
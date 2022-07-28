import kotlin.math.pow

sealed class Expression {
    data class Addition(val summand1: Expression, val summand2: Expression) : Expression()
    data class Subtraction(val x: Expression, val y: Expression) : Expression()
    data class Multiplication(val factor1: Expression, val factor2: Expression): Expression()
    data class Division(val x: Expression, val y: Expression): Expression()
    data class Number(val number: Double) : Expression()
    data class Negation(val expr: Expression): Expression()
    data class Absolute(val expr: Expression): Expression()
    data class Root(val base: Expression, val root: Expression): Expression()
    data class Power(val base: Expression, val pow: Expression): Expression()
    data class Checksum(val value: Expression): Expression()
    data class Factorial(val value: Expression): Expression()
}

sealed class SimplerExpression {
    data class Addition(val summand1: SimplerExpression, val summand2: SimplerExpression) : SimplerExpression()
    data class Subtraction(val x: SimplerExpression, val y: SimplerExpression) : SimplerExpression()
    data class Multiplication(val factor1: SimplerExpression, val factor2: SimplerExpression): SimplerExpression()
    data class Division(val x: SimplerExpression, val y: SimplerExpression): SimplerExpression()
    data class Number(val number: Double) : SimplerExpression()
    data class Power(val base: SimplerExpression, val pow: SimplerExpression): SimplerExpression()
    data class Absolute(val expr: SimplerExpression): SimplerExpression()
    data class Checksum(val value: SimplerExpression): SimplerExpression()
    data class Factorial(val value: SimplerExpression): SimplerExpression()
}

fun simplify(expression: Expression): SimplerExpression {
    return when (expression) {
        is Expression.Addition -> SimplerExpression.Addition(
            simplify(expression.summand1),
            simplify(expression.summand2)
        )
        is Expression.Multiplication -> SimplerExpression.Multiplication(
            simplify(expression.factor1),
            simplify(expression.factor2)
        )
        is Expression.Division -> SimplerExpression.Division(
            simplify(expression.x),
            simplify(expression.y)
        )
        is Expression.Negation -> SimplerExpression.Subtraction(
            SimplerExpression.Number(0.0),
            simplify(expression.expr)
        )
        is Expression.Number -> SimplerExpression.Number(expression.number)
        is Expression.Subtraction -> SimplerExpression.Subtraction(
            simplify(expression.x),
            simplify(expression.y)
        )
        is Expression.Root -> SimplerExpression.Power(
            simplify(expression.base),
            simplify(Expression.Division(Expression.Number(1.0), expression.root))
        )
        is Expression.Absolute -> SimplerExpression.Absolute(
            simplify(expression)
        )
        is Expression.Checksum -> SimplerExpression.Checksum(
            simplify(expression.value)
        )
        is Expression.Power -> SimplerExpression.Power(
            simplify(expression.base),
            simplify(expression.pow)
        )
        is Expression.Factorial -> SimplerExpression.Factorial(
            simplify(expression.value)
        )
    }
}

fun interpSimple(expression: SimplerExpression): Double {
    return when (expression) {
        is SimplerExpression.Addition ->
            interpSimple(expression.summand1) + interpSimple(expression.summand2)
        is SimplerExpression.Number ->
            expression.number
        is SimplerExpression.Multiplication ->
            interpSimple(expression.factor1) * interpSimple(expression.factor2)
        is SimplerExpression.Division ->
            interpSimple(expression.x) / interpSimple(expression.y)
        is SimplerExpression.Subtraction ->
            interpSimple(expression.x) - interpSimple(expression.y)
        is SimplerExpression.Absolute -> {
            if(interpSimple(expression.expr) < 0.0) {
                return interpSimple(expression.expr) * -1.0
            } else return interpSimple(expression.expr)
        }
        is SimplerExpression.Checksum -> {
            val numString = interpSimple(expression.value).toString()
            var vorzeichen = interpSimple(expression.value) < 0
            var res = 0.0
            for (c in numString) {
                if(c == '.' || c == '-')
                    continue
                res += c.toString().toDouble()
            }
            if(vorzeichen)
                res *= -1
            return res
        }
        is SimplerExpression.Power ->
            interpSimple(expression.base).pow(interpSimple(expression.pow))
        is SimplerExpression.Factorial -> {
            var factorial = 1.0
            var i = 1
            if(interpSimple(expression.value) % 1.0 != 0.0)
                throw Exception("Faculty not implemented for decimals")

            while (i <= interpSimple(expression.value)){
                factorial *= i
                ++i
            }
            return factorial
        }
    }
}

fun interp(expr: Expression): Double {
    return interpSimple(simplify(expr))
}
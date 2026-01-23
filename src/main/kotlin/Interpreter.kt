package dev.jhyub.klox

import dev.jhyub.klox.TokenType.*

class Interpreter: Expr.Visitor<Any?> {
    private fun evaluate(expr: Expr?): Any? {
        return expr?.accept(this)
    }

    private fun isTruthy(obj: Any?): Boolean {
        if (obj == null) return false
        if (obj is Boolean) return obj
        return true
    }

    private fun isEqual(a: Any?, b: Any?): Boolean {
        // This piece of code seems very Java-specific, but whatever...
        if (a == null && b == null) return true
        if (a == null) return false
        return a == b
    }

    private fun checkNumberOperand(operator: Token, operand: Any?) {
        if (operand is Double) return
        throw RuntimeError(operator, "Operand must be a number.")
    }

    private fun checkNumberOperands(operator: Token, left: Any?, right: Any?) {
        if (left is Double && right is Double) return
        throw RuntimeError(operator, "Operands must be a number.")
    }

    override fun visitLiteral(expr: Expr.Literal): Any? {
        return expr.value
    }

    override fun visitGrouping(expr: Expr.Grouping): Any? {
        return evaluate(expr.expr)
    }

    override fun visitUnary(expr: Expr.Unary): Any? {
        val right = evaluate(expr.right)

        return when(expr.operator.type) {
            BANG -> !isTruthy(right)
            MINUS -> {
                checkNumberOperand(expr.operator, right)
                -(right as Double)
            }
            else -> null
        }
    }

    override fun visitBinary(expr: Expr.Binary): Any? {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)

        when (expr.operator.type) {
            MINUS, SLASH, STAR, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL -> checkNumberOperands(expr.operator, left, right)
            else -> {}
        }

        return when (expr.operator.type) {
            PLUS if left is Double && right is Double -> left + right
            PLUS if left is String && right is String -> left + right
            PLUS -> throw RuntimeError(expr.operator, "Operands must be two numbers or two strings.")
            MINUS -> left as Double - right as Double
            SLASH -> left as Double / right as Double
            STAR -> left as Double * right as Double
            GREATER -> left as Double > right as Double
            GREATER_EQUAL -> left as Double >= right as Double
            LESS -> (left as Double) < right as Double
            LESS_EQUAL -> left as Double <= right as Double
            BANG_EQUAL -> !isEqual(left, right)
            EQUAL_EQUAL -> isEqual(left, right)
            else -> null
        }
    }

    private fun stringify(obj: Any?): String {
        if (obj == null) return "nil"

        if (obj is Double) {
            var text = obj.toString()
            if (text.endsWith(".0")) text = text.substring(0, text.length - 2)
            return text
        }

        return obj.toString()
    }

    fun interpret(expression: Expr?) {
        try {
            val value = evaluate(expression)
            println(stringify(value))
        } catch (e: RuntimeError) {
            Lox.runtimeError(e)
        }
    }
}
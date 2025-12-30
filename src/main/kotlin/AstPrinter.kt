package dev.jhyub.klox

object AstPrinter: Visitor<String> {
    fun print(expr: Expr): String {
        return expr.accept(AstPrinter)
    }

    override fun visitBinary(expr: Expr.Binary): String {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right)
    }

    override fun visitGrouping(expr: Expr.Grouping): String {
        return parenthesize("group", expr.expr)
    }

    override fun visitLiteral(expr: Expr.Literal): String {
        return expr.value?.toString() ?: "nil"
    }

    override fun visitUnary(expr: Expr.Unary): String {
        return parenthesize(expr.operator.lexeme, expr.right)
    }

    private fun parenthesize(name: String, vararg exprs: Expr): String {
        val sb = StringBuilder()

        sb.append("(").append(name)
        for (expr in exprs) {
            sb.append(" ${expr.accept(this)}")
        }
        sb.append(")")

        return sb.toString()
    }
}
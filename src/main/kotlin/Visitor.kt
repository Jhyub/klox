package dev.jhyub.klox

interface Visitor<R> {
    fun visitBinary(expr: Expr.Binary): R
    fun visitGrouping(expr: Expr.Grouping): R
    fun visitLiteral(expr: Expr.Literal): R
    fun visitUnary(expr: Expr.Unary): R
}
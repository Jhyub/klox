package dev.jhyub.klox

interface Expr {
    interface Visitor<R> {
        fun visitBinary(expr: Binary): R
        fun visitGrouping(expr: Grouping): R
        fun visitLiteral(expr: Literal): R
        fun visitUnary(expr: Unary): R
    }

    class Binary(val left: Expr, val operator: Token, val right: Expr): Expr {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitBinary(this)
        }
    }

    class Grouping(val expr: Expr): Expr {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitGrouping(this)
        }
    }

    class Literal(val value: Any?): Expr {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitLiteral(this)
        }
    }

    class Unary(val operator: Token, val right: Expr): Expr {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitUnary(this)
        }
    }

    fun <R> accept(visitor: Visitor<R>): R
}

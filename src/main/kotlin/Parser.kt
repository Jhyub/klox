package dev.jhyub.klox

import dev.jhyub.klox.TokenType.*

class Parser(private val tokens: List<Token>) {
    private class ParseError: RuntimeException()

    private var current = 0

    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }

        return false
    }

    private fun check(type: TokenType): Boolean {
        if (isAtEnd()) return false
        return peek().type == type
    }

    private fun isAtEnd(): Boolean {
        return peek().type == EOF
    }

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun peek(): Token {
        return tokens[current]
    }

    private fun previous(): Token {
        return tokens[current-1]
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()

        throw error(peek(), message)
    }

    private fun error(token: Token, message: String): ParseError {
        Lox.error(token, message)
        return ParseError()
    }

    private fun synchronize() {
        advance()

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return

            when (peek().type) {
                CLASS, FUN, VAR, FOR, IF, WHILE, PRINT, RETURN -> return
                else -> advance()
            }
        }
    }

    private fun expression(): Expr {
        return assignment()
    }

    private fun assignment(): Expr {
        val expr = or()

        if (match(EQUAL)) {
            val equals = previous()
            val value = assignment()

            if (expr is Expr.Variable) {
                val name = expr.name
                return Expr.Assign(name, value)
            }

            error(equals, "Invalid assignment target.")
        }

        return expr
    }

    private fun or(): Expr {
        var expr = and()

        while (match(OR)) {
            val operator = previous()
            val right = and()
            expr = Expr.Logical(expr, operator, right)
        }

        return expr
    }

    private fun and(): Expr {
        var expr = equality()

        while (match(AND)) {
            val operator = previous()
            val right = equality()
            expr = Expr.Logical(expr, operator, right)
        }

        return expr
    }

    private fun equality(): Expr {
        var expr = comparison()

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            val operator = previous()
            val right = comparison()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun comparison(): Expr {
        var expr = term()

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            val operator = previous()
            val right = term()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun term(): Expr {
        var expr = factor()

        while (match(MINUS, PLUS)) {
            val operator = previous()
            val right = factor()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun factor(): Expr {
        var expr = unary()

        while (match(SLASH, STAR)) {
            val operator = previous()
            val right = unary()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun unary(): Expr {
        if (match(BANG, MINUS)) {
            val operator = previous()
            val right = unary()
            return Expr.Unary(operator, right)
        }

        return primary()
    }

    private fun primary(): Expr {
        if (match(FALSE)) return Expr.Literal(false)
        if (match(TRUE)) return Expr.Literal(true)
        if (match(NIL)) return Expr.Literal(null)

        if (match(NUMBER, STRING)) {
            return Expr.Literal(previous().literal)
        }

        if (match(IDENTIFIER)) {
            return Expr.Variable(previous())
        }

        if (match(LEFT_PAREN)) {
            val expr = expression()
            consume(RIGHT_PAREN, "Expected ')' after expression")
            return Expr.Grouping(expr)
        }

        throw error(peek(), "Expceted expression")
    }

    private fun expressionStatement(): Stmt {
        val expr = expression()
        consume(SEMICOLON, "Expected ';' after expression.")
        return Stmt.Expression(expr)
    }

    private fun forStatement(): Stmt {
        consume(LEFT_PAREN, "Expected '(' after 'for'.")

        val initializer = if (match(SEMICOLON)) null
        else if (match(VAR)) varDeclaration()
        else expressionStatement()

        var condition = if(!check(SEMICOLON)) expression() else null
        consume(SEMICOLON, "Expect ';' after loop condition.")

        val increment = if(!check(RIGHT_PAREN)) expression() else null
        consume(RIGHT_PAREN, "Expect ')' after for clauses.")

        var body = statement()

        if (increment != null) {
            body = Stmt.Block(listOf(body, Stmt.Expression(increment)))
        }

        if (condition == null) condition = Expr.Literal(true)
        body = Stmt.While(condition, body)

        if (initializer != null) {
            body = Stmt.Block(listOf(initializer, body))
        }

        return body
    }

    private fun ifStatement(): Stmt {
        consume(LEFT_PAREN, "Expected '(' after 'if'.")
        val condition = expression()
        consume(RIGHT_PAREN, "Expected ')' after if condition.")
        val thenBranch = statement()
        val elseBranch = if (match(ELSE)) statement() else null

        return Stmt.If(condition, thenBranch, elseBranch)
    }

    private fun printStatement(): Stmt {
        val value = expression()
        consume(SEMICOLON, "Expected ';' after value.")
        return Stmt.Print(value)
    }

    private fun whileStatement(): Stmt {
        consume(LEFT_PAREN, "Expected '(' after 'while'.")
        val condition = expression()
        consume(RIGHT_PAREN, "Expected ')' after condition.")
        val body = statement()

        return Stmt.While(condition, body)
    }

    private fun block(): List<Stmt?> {
        val ret = mutableListOf<Stmt?>()

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            ret.add(declaration())
        }

        consume(RIGHT_BRACE, "Expected '}' after block.")
        return ret
    }

    private fun statement(): Stmt {
        if (match(FOR)) return forStatement()
        if (match(IF)) return ifStatement()
        if (match(PRINT)) return printStatement()
        if (match(WHILE)) return whileStatement()
        if (match(LEFT_BRACE)) return Stmt.Block(block())

        return expressionStatement()
    }

    private fun varDeclaration(): Stmt {
        val name = consume(IDENTIFIER, "Expected variable name.")
        val initializer = if (match(EQUAL)) expression() else null

        consume(SEMICOLON, "Expected ';' after variable declaration.")
        return Stmt.Var(name, initializer)
    }

    private fun declaration(): Stmt? {
        try {
            if (match(VAR)) return varDeclaration()
            return statement()
        } catch (e: ParseError) {
            synchronize()
            return null
        }
    }

    fun parse(): List<Stmt?> {
        val ret = mutableListOf<Stmt?>()

        while (!isAtEnd()) {
            ret.add(declaration())
        }

        return ret
    }

}
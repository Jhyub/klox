package dev.jhyub.klox

import dev.jhyub.klox.TokenType.*

class Scanner(private val source: String) {
    private val tokens = mutableListOf<Token>()

    private var start = 0
    private var current = 0
    private var line = 1

    companion object {
        val keywords = mapOf(
            "and" to AND,
            "class" to CLASS,
            "else" to ELSE,
            "false" to FALSE,
            "for" to FOR,
            "fun" to FUN,
            "if" to IF,
            "nil" to NIL,
            "or" to OR,
            "print" to PRINT,
            "return" to RETURN,
            "super" to SUPER,
            "this" to THIS,
            "true" to TRUE,
            "var" to VAR,
            "while" to WHILE,
        )
    }

    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            start = current
            scanToken()
        }

        tokens.add(Token(EOF, "", null, line))
        return tokens
    }

    private fun isAtEnd(): Boolean {
        return current >= source.length
    }

    private fun scanToken() {
        val c = advance()
        when (c) {
            '(' -> addToken(LEFT_PAREN)
            ')' -> addToken(RIGHT_PAREN)
            '{' -> addToken(LEFT_BRACE)
            '}' -> addToken(RIGHT_BRACE)
            ',' -> addToken(COMMA)
            '.' -> addToken(DOT)
            '-' -> addToken(MINUS)
            '+' -> addToken(PLUS)
            '*' -> addToken(STAR)
            ';' -> addToken(SEMICOLON)
            '!' -> addToken(if (match('=')) BANG_EQUAL else BANG)
            '=' -> addToken(if (match('=')) EQUAL_EQUAL else EQUAL)
            '<' -> addToken(if (match('=')) LESS_EQUAL else LESS)
            '>' -> addToken(if (match('=')) GREATER_EQUAL else GREATER)
            '/' -> {
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance()
                } else {
                    addToken(SLASH)
                }
            }
            ' ', '\r', '\t' -> {}
            '\n' -> line++
            '"' -> string()
            else -> {
                if (c.isDigit()) {
                    number()
                } else if (c.isLetter()) {
                    identifier()
                } else {
                    Lox.error(line, "Unexpected character '$c'")
                }
            }
        }

    }

    private fun advance(): Char {
        return source[current++]
    }

    private fun peek(): Char {
        if (isAtEnd()) return '\u0000'
        return source[current]
    }

    private fun peekNext(): Char {
        if (current+1 >= source.length) return '\u0000'
        return source[current+1]
    }

    private fun addToken(type: TokenType, literal: Any? = null) {
        val text = source.substring(start, current)
        tokens.add(Token(type, text, literal, line))
    }

    private fun match(expected: Char): Boolean {
        if (isAtEnd()) return false
        if (source[current] != expected) return false

        current++
        return true
    }

    private fun string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++
            advance()
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string")
            return
        }

        // Close final "
        advance()

        addToken(STRING, source.substring(start+1, current-1))
    }

    private fun number() {
        while (peek().isDigit()) advance()

        if (peek() == '.' && peekNext().isDigit()) {
            advance()

            while (peek().isDigit()) advance()
        }

        addToken(NUMBER, source.substring(start, current).toDouble())
    }

    private fun identifier() {
        while (peek().isLetterOrDigit() || peek() == '_') advance()

        val text = source.substring(start, current)

        addToken(keywords[text] ?: IDENTIFIER)
    }
}
package dev.jhyub.klox

import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.size > 1) {
        println("Usage: klox [script]")
        exitProcess(64)
    } else if (args.size == 1) {
        Lox.runFile(args[0])
    } else {
        Lox.runPrompt()
    }
}

object Lox {
    private var hadError = false

    fun runFile(path: String) {
        val bytes = Files.readAllBytes(Paths.get(path))
        run(String(bytes, Charset.defaultCharset()))

        if (hadError) exitProcess(65)
    }

    fun runPrompt() {
        val isr = InputStreamReader(System.`in`)
        val br = BufferedReader(isr)

        while (true) {
            print("> ")
            val line = br.readLine() ?: break
            run(line)
            hadError = false
        }
    }

    private fun run(source: String) {

    }

    fun error(line: Int, message: String) {
        report(line, "", message)

    }

    private fun report(line: Int, where: String, message: String) {
        println("[line $line] Error $where: $message")
        hadError = true
    }
}

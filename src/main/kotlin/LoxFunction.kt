package dev.jhyub.klox

class LoxFunction(private val declaration: Stmt.Function, private val closure: Environment): LoxCallable {
    override fun arity(): Int = declaration.params.size

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val environment = Environment(closure)
        for ((i, param) in declaration.params.withIndex()) {
            environment.define(param.lexeme, arguments[i])
        }

        try {
            interpreter.executeBlock(declaration.body, environment)
        } catch (returnValue: Return){
            return returnValue.value
        }
        return null
    }

    override fun toString(): String = "<fn ${declaration.name.lexeme}>"
}
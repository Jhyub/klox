package dev.jhyub.klox

class LoxClass(val name: String, private val methods: Map<String, LoxFunction>): LoxCallable {
    override fun toString() = name

    override fun arity(): Int = findMethod("init")?.arity() ?: 0

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val instance = LoxInstance(this)
        val initializer = findMethod("init")
        initializer?.bind(instance)?.call(interpreter, arguments)
        return instance
    }

    fun findMethod(name: String): LoxFunction? {
        return methods[name]
    }
}
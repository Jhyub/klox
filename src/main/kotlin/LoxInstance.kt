package dev.jhyub.klox

class LoxInstance(val clazz: LoxClass) {
    private val fields: MutableMap<String, Any?> = mutableMapOf()

    override fun toString(): String = "${clazz.name} instance"

    fun get(name: Token): Any? {
        if (fields.containsKey(name.lexeme)) {
            return fields[name.lexeme]
        }
        clazz.findMethod(name.lexeme)?.let { return it.bind(this) }

        throw RuntimeError(name, "Undefined property ${name.lexeme}.")
    }

    fun set(name: Token, value: Any?) {
        fields[name.lexeme] = value
    }
}
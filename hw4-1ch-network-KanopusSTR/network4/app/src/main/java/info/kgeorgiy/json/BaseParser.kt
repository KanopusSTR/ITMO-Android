package info.kgeorgiy.json

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
open class BaseParser protected constructor(private val source: CharSource) {
    private var ch = 0xffff.toChar()

    init {
        take()
    }

    protected fun take(): Char {
        val result = ch
        ch = if (source.hasNext()) source.next() else END
        return result
    }

    protected fun test(expected: Char): Boolean {
        return ch == expected
    }

    protected fun take(expected: Char): Boolean {
        if (test(expected)) {
            take()
            return true
        }
        return false
    }

    protected fun expect(expected: Char) {
        if (!take(expected)) {
            throw error("Expected '$expected', found '$ch'")
        }
    }

    protected fun expect(value: String) {
        for (c in value.toCharArray()) {
            expect(c)
        }
    }

    protected fun eof(): Boolean {
        return take(END)
    }

    protected fun error(message: String): IllegalArgumentException {
        return source.error(message)
    }

    protected fun between(from: Char, to: Char): Boolean {
        return from <= ch && ch <= to
    }

    companion object {
        private const val END = '\u0000'
    }
}
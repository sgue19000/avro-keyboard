package com.avrokeyboard.app.ime.avro

/** Keep in sync with lib/avro/avro_engine.dart */
class AvroEngine {
    fun parse(input: String): String {
        if (input.isEmpty()) return ""
        val out = StringBuilder()
        val buf = StringBuilder()
        fun flush() {
            if (buf.isEmpty()) return
            out.append(parseWord(buf.toString()))
            buf.clear()
        }
        for (ch in input) {
            if ((ch.isLetter() && ch.code < 128) || ch == '`') buf.append(ch) else {
                flush()
                out.append(ch)
            }
        }
        flush()
        return out.toString()
    }

    fun parseWord(raw: String): String {
        if (raw.isEmpty()) return ""
        if (raw.startsWith("`")) return raw.substring(1)
        val word = fixCase(raw)
        exceptions[word]?.let { return it }
        val out = StringBuilder()
        var i = 0
        while (i < word.length) {
            val hit = match(word, i)
            out.append(hit.replace)
            i += hit.consumed
        }
        return out.toString()
    }

    private fun match(word: String, i: Int): Hit {
        for (p in patterns) {
            if (i + p.find.length > word.length) continue
            if (word.substring(i, i + p.find.length) != p.find) continue
            if (p.rules.isEmpty()) return Hit(p.find.length, p.replace)
            for (rule in p.rules) {
                if (rule.matches.all { ok(it, word, i, i + p.find.length) }) {
                    return Hit(p.find.length, rule.replace)
                }
            }
            return Hit(p.find.length, p.replace)
        }
        return Hit(1, word[i].toString())
    }

    private fun ok(m: Match, word: String, start: Int, end: Int): Boolean =
        if (m.prefix) {
            if (start == 0) false else check(m, word, start - 1)
        } else {
            if (end >= word.length) false else check(m, word, end)
        }

    private fun check(m: Match, word: String, index: Int): Boolean {
        if (index !in word.indices) return false
        val ch = word[index].lowercaseChar()
        return when (m.kind) {
            Kind.VOWEL -> ch in vowels
            Kind.CONSONANT -> ch in consonants
        }
    }

    private fun fixCase(text: String): String = buildString {
        for (ch in text) append(if (ch in "OITDNSRZ") ch else ch.lowercaseChar())
    }

    private data class Hit(val consumed: Int, val replace: String)
    private enum class Kind { VOWEL, CONSONANT }
    private data class Match(val prefix: Boolean, val kind: Kind)
    private data class Rule(val replace: String, val matches: List<Match>)
    private data class Pat(val find: String, val replace: String, val rules: List<Rule> = emptyList())

    companion object {
        private const val vowels = "aeiou"
        private const val consonants = "bcdfghjklmnpqrstvwxyz"
        private fun pV() = Match(true, Kind.VOWEL)
        private fun pC() = Match(true, Kind.CONSONANT)
        private fun sC() = Match(false, Kind.CONSONANT)
        private val patterns = listOf(
            Pat("ksh", "ক্ষ"), Pat("ggy", "জ্ঞ"), Pat("GG", "জ্ঞ"),
            Pat("cch", "ছ"), Pat("chh", "ছ"),
            Pat("ngo", "ঙ্গ"), Pat("nno", "ন্য"), Pat("nyo", "ন্য"),
            Pat("sw", "স্ব"),
            Pat("kh", "খ"), Pat("gh", "ঘ"), Pat("ch", "চ"), Pat("jh", "ঝ"),
            Pat("Th", "ঠ"), Pat("Dh", "ঢ"), Pat("th", "থ"), Pat("dh", "ধ"),
            Pat("ph", "ফ"), Pat("bh", "ভ"), Pat("sh", "শ"),
            Pat("Ng", "ঙ"), Pat("ng", "ং"), Pat("rh", "ড়"), Pat("Rh", "ঢ়"),
            Pat("aa", "আ"), Pat("ee", "ঈ"), Pat("oo", "ঊ"),
            Pat("oi", "ঐ", listOf(Rule("ৈ", listOf(pC())))),
            Pat("ou", "ঔ", listOf(Rule("ৌ", listOf(pC())))),
            Pat("a", "আ", listOf(Rule("া", listOf(pC())))),
            Pat("i", "ই", listOf(Rule("ি", listOf(pC())))),
            Pat("I", "ঈ", listOf(Rule("ী", listOf(pC())))),
            Pat("u", "উ", listOf(Rule("ু", listOf(pC())))),
            Pat("e", "এ", listOf(Rule("ে", listOf(pC())))),
            Pat("o", "ও", listOf(Rule("", listOf(pC(), sC())), Rule("ো", listOf(pC())))),
            Pat("O", "ও", listOf(Rule("ো", listOf(pC())))),
            Pat("y", "য", listOf(Rule("য়", listOf(pV())))),
            Pat("r", "র", listOf(Rule("্র", listOf(pC())))),
            Pat("k", "ক"), Pat("g", "গ"), Pat("c", "চ"), Pat("j", "জ"),
            Pat("T", "ট"), Pat("D", "ড"), Pat("t", "ত"), Pat("d", "দ"),
            Pat("N", "ণ"), Pat("n", "ন"), Pat("p", "প"), Pat("f", "ফ"),
            Pat("b", "ব"), Pat("m", "ম"), Pat("l", "ল"),
            Pat("S", "ষ"), Pat("s", "স"), Pat("h", "হ"),
            Pat("z", "য"), Pat("w", "ও"), Pat("v", "ভ"),
            Pat("x", "ক্স"), Pat("q", "ক"),
        )
        private val exceptions = mapOf(
            "acho" to "আছো",
            "achho" to "আছো",
            "accho" to "আছো",
            "dhonnobad" to "ধন্যবাদ",
            "dhonnyobad" to "ধন্যবাদ",
        )
    }
}

class AvroComposer(private val engine: AvroEngine = AvroEngine()) {
    var buffer: String = ""
        private set
    val preview: String get() = engine.parse(buffer)
    fun type(ch: String) { buffer += ch }
    fun backspace() { if (buffer.isNotEmpty()) buffer = buffer.dropLast(1) }
    fun commitWord(): String { val t = preview; buffer = ""; return t }
    fun clear() { buffer = "" }
    val isComposing: Boolean get() = buffer.isNotEmpty()
}

package app.simplecloud.plugin.core.shared.pretty

object StringPrettifier {

    /** Converts various string formats to different cases */
    fun prettify(input: String, case: PrettifyCase = PrettifyCase.TITLE): String = when (case) {
        PrettifyCase.TITLE -> toTitleCase(input)
        PrettifyCase.CAMEL -> toCamelCase(input)
        PrettifyCase.PASCAL -> toPascalCase(input)
        PrettifyCase.SENTENCE -> toSentenceCase(input)
        PrettifyCase.KEBAB -> toKebabCase(input)
        PrettifyCase.SNAKE -> toSnakeCase(input)
    }

    private fun toTitleCase(input: String): String = input
        .split(Regex("[\\s_-]"))
        .filter { it.isNotEmpty() }
        .joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }

    private fun toCamelCase(input: String): String = input
        .split(Regex("[\\s_-]"))
        .filter { it.isNotEmpty() }
        .mapIndexed { index, word ->
            if (index == 0) {
                word.lowercase()
            } else {
                word.lowercase().replaceFirstChar { it.uppercase() }
            }
        }
        .joinToString("")

    private fun toPascalCase(input: String): String = input
        .split(Regex("[\\s_-]"))
        .filter { it.isNotEmpty() }
        .joinToString("") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }

    private fun toSentenceCase(input: String): String = input
        .split(Regex("[\\s_-]"))
        .filter { it.isNotEmpty() }
        .joinToString(" ") { it.lowercase() }
        .replaceFirstChar { it.uppercase() }

    private fun toKebabCase(input: String): String = input
        .split(Regex("[\\s_]"))
        .filter { it.isNotEmpty() }
        .joinToString("-") { it.lowercase() }

    private fun toSnakeCase(input: String): String = input
        .split(Regex("[\\s-]"))
        .filter { it.isNotEmpty() }
        .joinToString("_") { it.lowercase() }
}

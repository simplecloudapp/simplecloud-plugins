package app.simplecloud.plugin.signs.shared.config.layout

enum class MatcherOperation {
    REGEX,
    EQUALS,
    EQUALS_IGNORE_CASE,
    NOT_EQUALS,
    CONTAINS,
    STARTS_WITH,
    ENDS_WITH,
    MATCHES_PATTERN,
    ;

    fun matches(key: String, value: String): Boolean = when (this) {
        REGEX -> Regex(value).containsMatchIn(key)
        EQUALS -> key == value
        EQUALS_IGNORE_CASE -> key.equals(value, ignoreCase = true)
        NOT_EQUALS -> key != value
        CONTAINS -> key.contains(value)
        STARTS_WITH -> key.startsWith(value)
        ENDS_WITH -> key.endsWith(value)
        MATCHES_PATTERN -> Regex(value).matches(key)
    }
}

package app.simplecloud.plugin.api.shared.pretty

import kotlin.test.*

class StringPrettifierTest {

    @Test
    fun testTitleCaseConversion() {
        assertEquals("Hello World", StringPrettifier.prettify("hello-world"))
        assertEquals("Hello World", StringPrettifier.prettify("hello_world"))
        assertEquals("Hello World", StringPrettifier.prettify("hello world"))
        assertEquals("Hello Beautiful World", StringPrettifier.prettify("hello-beautiful_world"))
    }

    @Test
    fun testCamelCaseConversion() {
        assertEquals("helloWorld", StringPrettifier.prettify("hello-world", PrettifyCase.CAMEL))
        assertEquals("helloWorld", StringPrettifier.prettify("hello_world", PrettifyCase.CAMEL))
        assertEquals("helloBeautifulWorld", StringPrettifier.prettify("hello beautiful world", PrettifyCase.CAMEL))
        assertEquals("myVariableName", StringPrettifier.prettify("my-variable-name", PrettifyCase.CAMEL))
    }

    @Test
    fun testPascalCaseConversion() {
        assertEquals("HelloWorld", StringPrettifier.prettify("hello-world", PrettifyCase.PASCAL))
        assertEquals("HelloWorld", StringPrettifier.prettify("hello_world", PrettifyCase.PASCAL))
        assertEquals("HelloBeautifulWorld", StringPrettifier.prettify("hello beautiful world", PrettifyCase.PASCAL))
        assertEquals("MyVariableName", StringPrettifier.prettify("my-variable-name", PrettifyCase.PASCAL))
    }

    @Test
    fun testSentenceCaseConversion() {
        assertEquals("Hello world", StringPrettifier.prettify("hello-world", PrettifyCase.SENTENCE))
        assertEquals("Hello beautiful world", StringPrettifier.prettify("hello_beautiful_world", PrettifyCase.SENTENCE))
        assertEquals("My variable name", StringPrettifier.prettify("my-variable-name", PrettifyCase.SENTENCE))
    }

    @Test
    fun testKebabCaseConversion() {
        assertEquals("hello-world", StringPrettifier.prettify("hello world", PrettifyCase.KEBAB))
        assertEquals("hello-beautiful-world", StringPrettifier.prettify("hello_beautiful_world", PrettifyCase.KEBAB))
        assertEquals("my-variable-name", StringPrettifier.prettify("my variable name", PrettifyCase.KEBAB))
    }

    @Test
    fun testSnakeCaseConversion() {
        assertEquals("hello_world", StringPrettifier.prettify("hello-world", PrettifyCase.SNAKE))
        assertEquals("hello_beautiful_world", StringPrettifier.prettify("hello beautiful world", PrettifyCase.SNAKE))
        assertEquals("my_variable_name", StringPrettifier.prettify("my-variable-name", PrettifyCase.SNAKE))
    }

    @Test
    fun testEmptyStringHandling() {
        assertEquals("", StringPrettifier.prettify(""))
        assertEquals("", StringPrettifier.prettify("", PrettifyCase.CAMEL))
        assertEquals("", StringPrettifier.prettify("", PrettifyCase.PASCAL))
    }

    @Test
    fun testMultipleDelimiterHandling() {
        assertEquals("Hello Beautiful World", StringPrettifier.prettify("hello-beautiful_world"))
        assertEquals("helloBeautifulWorld", StringPrettifier.prettify("hello-beautiful_world", PrettifyCase.CAMEL))
        assertEquals("HelloBeautifulWorld", StringPrettifier.prettify("hello-beautiful_world", PrettifyCase.PASCAL))
    }
}

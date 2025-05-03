package net.sebyte.ast

fun <T> Iterable<T>.parentString() = joinToString(prefix = "(", postfix = ")")

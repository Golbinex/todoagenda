/*
 * Copyright (C) 2018 yvolk (Yuri Volkov), http://yurivolkov.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.andstatus.todoagenda.util

import java.util.Optional
import java.util.function.Predicate
import java.util.function.Supplier

/** Adds convenience methods to [StringBuilder]  */
class MyStringBuilder(val builder: StringBuilder = StringBuilder()) : CharSequence, IsEmpty {
    fun <T> withCommaNonEmpty(label: CharSequence?, obj: T): MyStringBuilder {
        return withComma<T>(label, obj) { obj: T -> nonEmptyObj(obj) }
    }

    fun <T> withComma(label: CharSequence?, obj: T?, predicate: Predicate<T>): MyStringBuilder {
        return if (obj == null || !predicate.test(obj)) this else withComma(label, obj)
    }

    fun withComma(label: CharSequence?, obj: Any?, filter: Supplier<Boolean>): MyStringBuilder {
        return if (obj == null || !filter.get()!!) this else withComma(label, obj)
    }

    fun withComma(label: CharSequence?, obj: Any?): MyStringBuilder {
        return append(label, obj, ", ", false)
    }

    fun withCommaQuoted(label: CharSequence?, obj: Any?, quoted: Boolean): MyStringBuilder {
        return append(label, obj, ", ", quoted)
    }

    fun withComma(text: CharSequence?): MyStringBuilder {
        return withSeparator(text, ", ")
    }

    fun withSpaceQuoted(text: CharSequence?): MyStringBuilder {
        return append("", text, " ", true)
    }

    fun withSpace(text: CharSequence?): MyStringBuilder {
        return withSeparator(text, " ")
    }

    fun withSeparator(text: CharSequence?, separator: String): MyStringBuilder {
        return append("", text, separator, false)
    }

    fun atNewLine(label: CharSequence?, text: CharSequence?): MyStringBuilder {
        return append(label, text, ", \n", false)
    }

    fun atNewLine(text: CharSequence?): MyStringBuilder {
        return append("", text, ", \n", false)
    }

    fun append(label: CharSequence?, obj: Any?, separator: String, quoted: Boolean): MyStringBuilder {
        if (obj == null) return this
        val text = obj.toString()
        if (StringUtil.isEmpty(text)) return this
        if (builder.length > 0) builder.append(separator)
        if (StringUtil.nonEmpty(label)) builder.append(label).append(": ")
        if (quoted) builder.append("\"")
        builder.append(text)
        if (quoted) builder.append("\"")
        return this
    }

    fun append(text: CharSequence?): MyStringBuilder {
        if (StringUtil.nonEmpty(text)) {
            builder.append(text)
        }
        return this
    }

    override val length get() = builder.length
    override fun get(index: Int): Char = builder[index]

    override fun subSequence(start: Int, end: Int): CharSequence {
        return builder.subSequence(start, end)
    }

    override fun toString(): String {
        return builder.toString()
    }

    fun prependWithSeparator(text: CharSequence, separator: String): MyStringBuilder {
        if (text.length > 0) {
            builder.insert(0, separator)
            builder.insert(0, text)
        }
        return this
    }

    override fun isEmpty(): Boolean = length == 0

    companion object {
        const val COMMA = ","
        fun of(text: CharSequence): MyStringBuilder {
            return MyStringBuilder(java.lang.StringBuilder(text))
        }

        fun of(content: Optional<String>): MyStringBuilder {
            return content.map { text: String -> of(text) }.orElse(MyStringBuilder())
        }

        fun formatKeyValue(keyIn: Any?, valueIn: Any?): String {
            val key = objToTag(keyIn)
            if (keyIn == null) {
                return key
            }
            var value = "null"
            if (valueIn != null) {
                value = valueIn.toString()
            }
            return formatKeyValue(key, value)
        }

        /** Strips value from leading and trailing commas  */
        fun formatKeyValue(key: Any?, value: String): String {
            var out = ""
            if (!StringUtil.isEmpty(value)) {
                out = value.trim { it <= ' ' }
                if (out.substring(0, 1) == COMMA) {
                    out = out.substring(1)
                }
                val ind = out.lastIndexOf(COMMA)
                if (ind > 0 && ind == out.length - 1) {
                    out = out.substring(0, ind)
                }
            }
            return objToTag(key) + ":{" + out + "}"
        }

        fun objToTag(objTag: Any?): String {
            val tag: String
            tag = if (objTag == null) {
                "(null)"
            } else if (objTag is String) {
                objTag
            } else (objTag as? Enum<*>)?.toString()
                ?: if (objTag is Class<*>) {
                    objTag.simpleName
                } else {
                    objTag.javaClass.simpleName
                }
            return if (tag.trim { it <= ' ' }.isEmpty()) {
                "(empty)"
            } else tag
        }

        fun <T> nonEmptyObj(obj: T): Boolean {
            return !isEmptyObj<T>(obj)
        }

        fun <T> isEmptyObj(obj: T?): Boolean {
            if (obj is IsEmpty) return (obj as IsEmpty).isEmpty()
            if (obj is Number) return (obj as Number).toLong() == 0L
            return if (obj is String) StringUtil.isEmpty(obj as String?) else obj == null
        }

        fun appendWithSpace(builder: StringBuilder, text: CharSequence?): StringBuilder {
            return MyStringBuilder(builder).withSpace(text).builder
        }
    }
}

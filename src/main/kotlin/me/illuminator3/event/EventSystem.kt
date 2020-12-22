/*
 *    Copyright 2020 illuminator3
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package me.illuminator3.event

import java.lang.reflect.Method
import java.util.*
import java.util.function.Predicate
import kotlin.collections.ArrayList

object EventSystem
{
    @JvmStatic
    private val listeners: MutableList<Any> = ArrayList()

    @JvmStatic
    private val filters: MutableList<Predicate<Any>> = ArrayList()

    @JvmStatic
    fun addListener(listener: Any)
    {
        listeners += listener
    }

    @JvmStatic
    fun removeListener(listener: Any)
    {
        listeners.remove(listener)
    }

    @JvmStatic
    fun addFilter(filter: Predicate<Any>)
    {
        filters += filter
    }

    @JvmStatic
    fun removeFilter(filter: Predicate<Any>)
    {
        filters.remove(filter)
    }

    @JvmStatic
    fun call(event: Event)
    {
        event.onCall()

        listeners.filter { cls ->
            filters.onEach { filter ->
                if (filter.test(cls))
                    return@filter false
            }

            return@filter true
        }.onEach {
            try
            {
                val parameters: Array<out Any> = event.getParameters()
                val parameterClasses: List<Class<*>> = parameters.map { o -> o::class.java }.map { c -> transform(c)!! }

                var method: Method? = null

                it::class.java.declaredMethods.forEach { mth ->
                    val annotations: Array<Annotation> = mth.annotations
                    var hasAnnotation = false

                    annotations.onEach { an ->
                        if (an is HandleEvent)
                            hasAnnotation = true
                    }

                    if (!hasAnnotation)
                        return@forEach

                    val params: List<Class<*>> = mth.parameters.map { param -> param.type }.map { c -> transform(c)!! }

                    if (listsEqual(parameterClasses, params))
                    {
                        method = mth

                        return@forEach
                    }
                }

                method?.run {
                    invoke(it, *parameters /* KoTlIn Has pOiNTeRS?! */)
                }
            } catch (ignored: NoSuchMethodException)
            {} catch (ex: Exception)
            {
                throw EventException(ex)
            }
        }
    }
}

private fun listsEqual(list1: List<Any>, list2: List<Any>): Boolean
{
    if (list1.size != list2.size)
        return false

    val pairList: List<Pair<Any, Any>> = list1.zip(list2)

    return pairList.all { (e1, e2) ->
        e1 == e2
    }
}

@Suppress("UNCHECKED_CAST")
private fun transform(c: Class<*>): Class<*>?
{
    return if (JAVA_TO_PRIMITVE_KOTLIN.containsKey(c)) JAVA_TO_PRIMITVE_KOTLIN[c] else c
}

private val JAVA_TO_PRIMITVE_KOTLIN: Map<Class<*>, Class<*>> = mapOf(
    java.lang.Boolean::class.java to Boolean::class.javaPrimitiveType!!,
    java.lang.Byte::class.java to Byte::class.javaPrimitiveType!!,
    java.lang.Character::class.java to Char::class.javaPrimitiveType!!,
    java.lang.Double::class.java to Double::class.javaPrimitiveType!!,
    java.lang.Float::class.java to Float::class.javaPrimitiveType!!,
    java.lang.Integer::class.java to Int::class.javaPrimitiveType!!,
    java.lang.Long::class.java to Long::class.javaPrimitiveType!!,
    java.lang.Short::class.java to Short::class.javaPrimitiveType!!,
    java.lang.Void::class.java to Void::class.javaPrimitiveType!!,
)
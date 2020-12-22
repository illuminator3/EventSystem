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

import me.illuminator3.event.Event
import me.illuminator3.event.EventSystem
import me.illuminator3.event.HandleEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EventSystemTest
{
    companion object {
        @JvmStatic
        var listenerCalled: Boolean = false

        @JvmStatic
        var eventOnCall: Boolean = false
    }

    @BeforeAll
    fun configureTest()
    {
        EventSystem.addListener(TestListener())
    }

    @Test
    @DisplayName("EventCalling")
    fun testCallEvent()
    {
        EventSystem.call(TestEvent())

        assertThat(listenerCalled).isTrue
        assertThat(eventOnCall).isTrue
    }
}

class TestListener
{
    @HandleEvent
    fun onEvent(event: TestEvent, time: Long)
    {
        EventSystemTest.listenerCalled = true
    }
}

class TestEvent : Event
{
    override fun getParameters(): Array<out Any>
    {
        return arrayOf(this, System.currentTimeMillis())
    }

    override fun onCall()
    {
        EventSystemTest.eventOnCall = true
    }
}
package com.finflow.core.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Swaps `Dispatchers.Main` for a test dispatcher around each test.
 *
 * Every ViewModel test needs this because `viewModelScope` is hard-wired to the main
 * dispatcher, which does not exist in a JVM unit test. Doing it as a rule rather than
 * `@Before`/`@After` means a test that throws still resets the dispatcher — a leaked main
 * dispatcher fails whichever test happens to run next, which is a miserable thing to debug.
 *
 * `StandardTestDispatcher` is the default rather than `UnconfinedTestDispatcher`: it queues
 * coroutines instead of running them eagerly, so a test asserts on the state a real frame
 * would see, and has to call `runCurrent()`/`advanceUntilIdle()` deliberately.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val dispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {

    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)

    override fun finished(description: Description) = Dispatchers.resetMain()
}

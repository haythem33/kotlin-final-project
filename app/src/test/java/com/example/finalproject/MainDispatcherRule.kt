package com.example.finalproject

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * A JUnit rule that replaces the real Main dispatcher with a TEST dispatcher
 * for the duration of each test, then restores it afterwards.
 *
 * Why we need this: our ViewModel launches coroutines on viewModelScope, which
 * uses Dispatchers.Main. In a plain unit test (no Android device) there is no
 * real Main thread, so without this the coroutines would crash. The test
 * dispatcher also lets us CONTROL time — nothing runs until we say
 * advanceUntilIdle(), making tests deterministic.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val dispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

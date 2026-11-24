package com.movtery.zalithlauncher.coroutine

import com.movtery.zalithlauncher.ui.androidText
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

class TaskFlowCancellationTest {
    @Test fun `taskbar cancellation finishes cleanup and permits another flow`() = runBlocking {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        try {
            val id = "launch-test-${UUID.randomUUID()}"
            val started = CompletableDeferred<Unit>()
            val finished = CompletableDeferred<Unit>()
            val notifications = AtomicInteger()
            val first = TaskFlowExecutor(scope, publishToTaskBar = true)
            first.addPhase(buildPhase {
                addTask(title = androidText("Launch"), id = id) {
                    started.complete(Unit)
                    awaitCancellation()
                }
            })
            first.executePhasesAsync(onCancel = {
                notifications.incrementAndGet()
                finished.complete(Unit)
            })
            withTimeout(10_000) { started.await() }
            TaskSystem.cancelTask(id)
            withTimeout(10_000) { finished.await() }
            assertEquals(1, notifications.get())
            assertFalse(TaskSystem.containsTask(id))

            val completed = CompletableDeferred<Unit>()
            val retry = TaskFlowExecutor(scope)
            retry.addPhase(buildPhase { addTask(title = androidText("Retry")) {} })
            retry.executePhasesAsync(onComplete = { completed.complete(Unit) })
            withTimeout(10_000) { completed.await() }
        } finally {
            scope.cancel()
        }
    }

    @Test fun `cancel during startup notifies owner`() = runBlocking {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        try {
            val started = CompletableDeferred<Unit>()
            val cancelled = CompletableDeferred<Unit>()
            val executor = TaskFlowExecutor(scope)
            executor.executePhasesAsync(
                onStart = { started.complete(Unit); awaitCancellation() },
                onCancel = { cancelled.complete(Unit) }
            )
            withTimeout(10_000) { started.await() }
            executor.cancel()
            withTimeout(10_000) { cancelled.await() }
        } finally {
            scope.cancel()
        }
    }
}

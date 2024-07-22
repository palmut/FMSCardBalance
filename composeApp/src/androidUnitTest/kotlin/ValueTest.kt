import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertTrue

class ValueTest {

    @Test
    fun aaa() = runBlocking {
        val c = TestClass()
        assertTrue { c.value == 0 }
        executeAndWaitValueForChange({ c.value }) { c.check() }
        assertTrue { c.value == 1 }
    }
}

class TestClass {
    var value = 0
    private val scope = CoroutineScope(Job())

    fun check() {
        scope.launch {
            delay(100)
            value = 1
        }
    }
}

suspend fun <T> executeAndWaitValueForChange(value: () -> T, block: () -> Unit): T {
    val start = value()
    block()
    while (true) {
        delay(10)
        val current = value()
        if (current != start) return current
    }
}
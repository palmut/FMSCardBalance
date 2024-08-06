import data.DefaultBalanceRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertTrue

class RepositoryTest {

    @Test
    fun get_balance_test() {
        runBlocking {
            val repository = DefaultBalanceRepository()
            val response = repository.getBalance("+79217772926", "6486")
            assertTrue { response.status == "OK" }
        }
    }

    @Test
    fun get_balance_invalid_card() {
        runBlocking {
            val repository = DefaultBalanceRepository()
            val response = repository.getBalance("+79217772926", "<FAKE>")
            assertTrue { response.status == "VALIDATION_FAIL" }
        }
    }

    @Test
    fun get_balance_invalid_phone() {
        runBlocking {
            val repository = DefaultBalanceRepository()
            val response = repository.getBalance("<FAKE>", "6486")
            assertTrue { response.status == "VALIDATION_FAIL" }
        }
    }
}
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import net.palmut.fmscardbalance.data.DefaultBalanceRepository
import net.palmut.fmscardbalance.data.SharedPreferences
import net.palmut.fmscardbalance.store.MainStore
import net.palmut.fmscardbalance.store.MainStoreProvider
import net.palmut.fmscardbalance.store.mapper.map
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MainStoreTest {

    private fun getStore() = MainStoreProvider(
        storeFactory = DefaultStoreFactory(),
        repository = DefaultBalanceRepository()
    ).provide()

    @Test
    fun getCards() {
        val store = getStore()
        store.accept(MainStore.Intent.Cards)

        val list = store.state.data

        assertTrue {
            list.isEmpty().not()
        }
    }

    @Test
    fun phoneInputTest() {
        val store = getStore()
        val input = "123"
        store.accept(MainStore.Intent.PhoneInput(input))

        assertTrue { store.state.phoneState == input }
    }

    @Test
    fun newCardNotEmpty() {
        val store = getStore()
        val label = "food"
        val tail = "1234"

        store.accept(MainStore.Intent.NewCardState(label = label, tail = tail))

        assertTrue { store.state.newCardModel.label == label }
        assertTrue { store.state.newCardModel.tail == tail }
    }

    @Test
    fun openNewCardState() {
        val store = getStore()

        store.accept(MainStore.Intent.OpenNewCard(true))
        assertTrue { store.state.isOnNewCard }
        store.accept(MainStore.Intent.OpenNewCard(false))
        assertFalse { store.state.isOnNewCard }
    }

    @Test
    fun getBalanceLoadingState() {
        val store = getStore()
        val tail = "6665"

        store.accept(MainStore.Intent.Balance(tail))
        assertTrue {
            store.state.data.find { it.tail == tail }?.status == MainStore.Status.LOADING
        }
    }

    @Test
    fun addNewCard() {
        val store = getStore()
        val label = "news"
        val tail = "1234"

        store.accept(MainStore.Intent.NewCardState(label = label, tail = tail))
        store.accept(MainStore.Intent.AddCard)

        val card = store.state.data.find { it.tail == tail }

        assertTrue { card != null }
        assertTrue { card?.title == label }
    }

    @Test
    fun removeCard() {
        val store = getStore()
        val label = "news"
        val tail = "1234"

        store.accept(MainStore.Intent.NewCardState(label = label, tail = tail))
        store.accept(MainStore.Intent.AddCard)

        val card = store.state.data.find { it.tail == tail }

        assertTrue { card != null }

        store.accept(MainStore.Intent.RemoveCard(card!!.map()))

        assertTrue { store.state.data.find { it.tail == tail } == null }
    }
}
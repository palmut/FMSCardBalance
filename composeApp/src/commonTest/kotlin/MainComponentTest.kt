import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.mvikotlin.logging.store.LoggingStoreFactory
import com.arkivanov.mvikotlin.timetravel.store.TimeTravelStoreFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import net.palmut.fmscardbalance.DefaultRootComponentContext
import net.palmut.fmscardbalance.component.DefaultMainComponent
import net.palmut.fmscardbalance.component.MainComponent
import net.palmut.fmscardbalance.component.entity.NewCardState
import net.palmut.fmscardbalance.store.MainStore
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MainComponentTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    private fun getComponent(): MainComponent = DefaultMainComponent(
        DefaultRootComponentContext(
            componentContext = DefaultComponentContext(LifecycleRegistry()),
            storeFactory = LoggingStoreFactory(TimeTravelStoreFactory())
        )
    )

    @Test
    fun initialState() = runTest {
        val component = getComponent()
        val list = component.model.value.data

        assertTrue { component.model.value.data.isNotEmpty() }
    }

    @Test
    fun phoneInputTest() {
        val component = getComponent()
        val input = "123"
        component.setPhoneInput(input)
        assertTrue { component.model.value.phoneState == input }
    }

    @Test
    fun newCardNotEmpty() {
        val component = getComponent()
        val label = "food"
        val tail = "1234"

        component.setNewCardState(NewCardState(label = label, tail = tail))

        assertTrue { component.model.value.newCardState.label == label }
        assertTrue { component.model.value.newCardState.tail == tail }
    }

    @Test
    fun openNewCardState() {
        val component = getComponent()

        component.goToNewCard(true)
        assertTrue { component.model.value.isOnNewCard }
        component.goToNewCard(false)
        assertFalse { component.model.value.isOnNewCard }
    }

    @Test
    fun addNewCard() {
        val component = getComponent()
        val label = "news"
        val tail = "1234"

        component.setNewCardState(NewCardState(label = label, tail = tail))
        component.addNewCard()

        assertTrue {
            component.model.value.data.find { it.tail == tail } != null
        }
    }

    @Test
    fun removeCard() {
        val component = getComponent()
        val label = "news"
        val tail = "1234"

        component.setNewCardState(NewCardState(label = label, tail = tail))
        component.addNewCard()

        val card = component.model.value.data.find { it.tail == tail }
        component.removeCard(card!!)
    }
}
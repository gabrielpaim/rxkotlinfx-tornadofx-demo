package domain.persistence

import app.mainModule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.test.ClosingKoinTest
import org.koin.test.get

class PersistenceTest: ClosingKoinTest {

    @BeforeEach
    fun setup() {
        startKoin {
            modules(mainModule)
        }
        db = get()
    }
    private lateinit var db: Persistence


    @Test
    fun `database should be initialized with data`() {
        initializeData(db)
            .test()
            .await()
            .assertNoErrors()

        db
            .listAllCustomers()
            .test()
            .assertValueCount(8)

        db.loadSalesPerson(1).test().assertValue { it.firstName == "Joe" }
    }

}
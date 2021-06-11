package domain.persistence

import app.mainModule
import domain.Customer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.test.get
import org.koin.test.junit5.ClosingKoinTest

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

    @Test
    fun `save Customer in database` () {
        db.listAllCustomers()
            .test()
            .assertValueCount(0)

        db.saveCustomer(Customer("Joseph"))
            .test()

        db.listAllCustomers()
            .test()
            .assertValueCount(1)

        db.loadCustomer(1)
            .test()
            .assertValue{ it.name == "Joseph" && it.id == 1 }
    }

}
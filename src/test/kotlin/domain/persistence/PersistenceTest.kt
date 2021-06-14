package domain.persistence

import app.mainModule
import domain.Assignment
import domain.Customer
import domain.SalesPerson
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

    @Test
    fun `save salesPerson in database `() {
        db.listAllSalesPersons()
            .test()
            .assertValueCount(0)

        db.saveSalesPerson(SalesPerson("Eduardo", "Marinho"))
            .test()
            .assertValue { it.firstName == "Eduardo" && it.lastName == "Marinho" }

        db.loadSalesPerson(1)
            .test()
            .assertValue { it.firstName == "Eduardo" && it.lastName == "Marinho" }

        db.saveSalesPerson(SalesPerson("EduardoOoO", "marinho", 1))
            .test()
            .assertValue { it.firstName ==  "EduardoOoO" && it.lastName == "marinho" }
    }

    @Test
    fun `delete salePerson in database` () {
        db.saveSalesPerson(SalesPerson("Messias", "jose"))
            .test()
        db.saveSalesPerson(SalesPerson("Anderson", "Brabo"))
            .test()

//        db.loadSalesPerson(1)
//            .test()
//            .assertValue { it.firstName == "Messias" }
//
//        db.loadSalesPerson(2)
//            .test()
//            .assertValue { it.firstName == "Anderson" }

        db.listAllSalesPersons()
            .test()
            .assertValueCount(2)

        db.deleteSalesPerson(2)
            .test()
            .assertValue { it == 2 }

        db.listAllSalesPersons()
            .test()
            .assertValueCount(1)

    }

    @Test
    fun `list sales person items`() {
        db.saveSalesPerson(SalesPerson("Sales", "Person", 1)).blockingGet()
        db.saveCustomer(Customer("C1", 1)).blockingGet()
        db.saveCustomer(Customer("C2", 2)).blockingGet()
        db.saveAssignment(Assignment(salesPersonId = 1, customerId = 1, order = 1)).blockingGet()
        db.saveAssignment(Assignment(salesPersonId = 1, customerId = 2, order = 2)).blockingGet()

        db.listSalesPersonItems()
            .test()
            .assertValueCount(1)
            .assertValue {
                it.assignmentIds == listOf(1,2)
            }

    }

}
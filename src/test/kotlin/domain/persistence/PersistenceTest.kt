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
    }

    @Test
    fun `database should be initialized with data`() {
        val db: Persistence = get()
        initializeData(db) // reactive?
        db
            .listAllCustomers()
            .test()
            .assertValueCount(8)
    }

}
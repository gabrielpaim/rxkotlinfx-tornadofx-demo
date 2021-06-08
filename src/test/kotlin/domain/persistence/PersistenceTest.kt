package domain.persistence

import app.mainModule
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.test.ClosingKoinTest
import org.koin.test.get

class PersistenceTest: ClosingKoinTest {

    @Before
    fun setup() {
        startKoin {
            modules(mainModule)
        }
    }


    @Test
    fun test() {
        val db: Persistence = get()

    }

}
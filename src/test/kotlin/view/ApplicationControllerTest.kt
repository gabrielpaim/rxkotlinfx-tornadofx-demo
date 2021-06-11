package view

import app.mainModule
import domain.persistence.Persistence
import domain.persistence.initializeData
import io.reactivex.schedulers.Schedulers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.test.get
import org.koin.test.inject
import tornadofx.*

class ApplicationControllerTest : TornadoFxWithKoinTest {

    @BeforeEach
    fun setup() {
        startKoin {
            modules(mainModule)
        }
        controller = ApplicationController()
    }
    private lateinit var controller: ApplicationController

    @Test
    fun `saveAssignments with id null should throw an exception`() {
        val customerAssignmentIds = mutableListOf(1).observable()

        controller
            .saveAssignments(null, customerAssignmentIds )
            .subscribeOn(Schedulers.computation()) // not needed
            .test()
            .await() // not needed
            .assertError(IllegalArgumentException::class.java)
    }

    @Test
    fun `saveAssignment com id nao nulo mas nao persistido na base deveria retornar um erro`() {
        val customerAssignmentIds = mutableListOf(1).observable()

        controller
            .saveAssignments(1, customerAssignmentIds )
            .test()
            .assertValue { it == 1L }

        val db: Persistence = get()
//        initializeData(db).blockingAwait()

//        controller
//            .saveAssignments(1, customerAssignmentIds )
//            .test()
//            .assertValue { it == 1L }

    }

}
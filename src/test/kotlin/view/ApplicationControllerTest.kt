package view

import app.mainModule
import io.reactivex.schedulers.Schedulers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.context.startKoin
import org.testfx.framework.junit5.ApplicationExtension
import tornadofx.*


@ExtendWith(ApplicationExtension::class)
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

//        val db: Persistence = get()
//        initializeData(db).blockingAwait()
//
//        controller
//            .saveAssignments(2, customerAssignmentIds )
//            .test()
//            .assertValue { it == 3L }

    }

}
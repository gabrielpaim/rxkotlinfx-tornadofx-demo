package view

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.koin.core.component.KoinComponent
import org.koin.test.junit5.ClosingKoinTest
import tornadofx.*
import kotlin.reflect.KClass

interface TornadoFxWithKoinTest: ClosingKoinTest {

    companion object {
//        @BeforeAll
//        fun configureTornadoFxDi() {
//            FX.dicontainer = object : DIContainer, KoinComponent {
//                override fun <T : Any> getInstance(type: KClass<T>): T {
//                    return getKoin().get(clazz = type)
//                }
//            }
//        }
    }

    @BeforeEach
    fun configureTornadoFxDi() {
        FX.dicontainer = object : DIContainer {
            override fun <T : Any> getInstance(type: KClass<T>): T {
                return getKoin().get(clazz = type)
            }
        }
    }

}
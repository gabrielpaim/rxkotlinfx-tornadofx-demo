package app

import domain.persistence.initializeData
import javafx.scene.image.Image
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import tornadofx.*
import view.MainView
import kotlin.reflect.KClass

class MyApp : App(MainView::class, Styles::class) {
    init {
        addStageIcon(Image("/app/tornado-fx-logo.png"))

        startKoin {
            modules(mainModule)
        }

        FX.dicontainer = object : DIContainer, KoinComponent {
            override fun <T : Any> getInstance(type: KClass<T>): T {
                return getKoin().get(clazz = type)
            }
        }

    }
//    fun main (args: Array<String>){
//        launch<MyApp>(args)
//    }
}

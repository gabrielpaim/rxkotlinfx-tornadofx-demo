package app

import domain.DbTest
import javafx.scene.image.Image
import tornadofx.App
import tornadofx.addStageIcon
import tornadofx.launch
import view.MainView

class MyApp : App(MainView::class, Styles::class) {
    init {
        DbTest.db
        addStageIcon(Image("/app/tornado-fx-logo.png"))
    }
//    fun main (args: Array<String>){
//        launch<MyApp>(args)
//    }
}

package view

import domain.Customer
import domain.persistence.Persistence
import io.reactivex.Maybe
import javafx.scene.control.ButtonType
import javafx.scene.control.Dialog
import javafx.scene.image.ImageView
import javafx.stage.Stage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import tornadofx.*

class NewCustomerDialog: Dialog<Maybe<Customer>>(), KoinComponent {
    private val root = Form()

    val db: Persistence by inject()

    init {
        title = "Create New Customer"

        with(root) {
            fieldset("Customer Name") {
                textfield {
                    setResultConverter {
                        if (it == ButtonType.OK && text.isNotEmpty()) {
                            db.saveCustomer(Customer(text)).toMaybe()
                        } else {
                            Maybe.empty()
                        }
                    }
                }
            }
        }

        dialogPane.content = root
        dialogPane.buttonTypes.addAll(ButtonType.OK,ButtonType.CANCEL)
        graphic = ImageView(FX.primaryStage.icons[0])
        (dialogPane.scene.window as Stage).icons += FX.primaryStage.icons[0]
    }
}
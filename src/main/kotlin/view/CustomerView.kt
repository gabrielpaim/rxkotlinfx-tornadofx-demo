package view

import app.Styles
import app.currentSelections
import app.toSet
import com.github.thomasnield.rxkotlinfx.actionEvents
import com.github.thomasnield.rxkotlinfx.events
import com.github.thomasnield.rxkotlinfx.onChangedObservable
import com.github.thomasnield.rxkotlinfx.toMaybe
import domain.Customer
import domain.persistence.Persistence
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.toObservable
import javafx.collections.ObservableList
import javafx.geometry.Orientation
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.paint.Color
import tornadofx.*

class CustomerView : View() {
    private val controller: ApplicationController by inject()
    private var table: TableView<Customer> by singleAssign()

//    private val db: Persistence by di()

    var selectedCustomers: ObservableList<Customer> by singleAssign()

    override val root = borderpane {
        top = label("CUSTOMER").addClass(Styles.heading)

        center = tableview(controller.customers) {
            readonlyColumn("ID", Customer::id)
            readonlyColumn("NAME", Customer::name)

            selectionModel.selectionMode = SelectionMode.MULTIPLE

            //broadcast selections:
            // Forma 1:
            selectedCustomers = selectionModel.selectedItems

            // Forma 2:
//            selectionModel
//                .selectedItems
//                .onChangedObservable()
//                .map { it.filterNotNull().toSet() }
//                .subscribe(controller.selectedCustomers)

            //Import data and refresh event handling

            controller.refreshCustomers()

//            controller.refreshCustomers
//                .startWith(Unit)
//                .flatMapSingle {
//                    db.listAllCustomers().toList()
//                }.subscribeBy(
//                    onNext = { items.setAll(it) },
//                    onError = { alert(Alert.AlertType.ERROR, "PROBLEM!", it.message ?: "").show() }
//                )

            //handle search request
            controller.searchCustomers
                .subscribeBy(
                    onNext = { ids ->
                        moveToTopWhere { it.id in ids }
                        requestFocus()
                    },
                    onError  ={ it.printStackTrace() }
                )

            table = this
        }
        left = toolbar {
            orientation = Orientation.VERTICAL

            // search selected customers on AppliedCustomerView
            button("⇇\uD83D\uDD0E") {

                // Forma 1
                action {
                    controller.searchSelectedCustomers(selectedCustomers)
                }

                // Forma 2
//                actionEvents()
//                    .flatMapSingle {
//                        controller
//                            .selectedCustomers
//                            .take(1)
//                            .flatMap { it.toObservable() }
//                            .map { it.id ?: 1 }
//                            .toSet()
//                    }
//                    .subscribe(controller.searchCustomerUsages)
            }

            // search selected applied
            button("⇉\uD83D\uDD0E") {

                action {
                    controller.searchSelectedApplied()
                }

//                actionEvents()
//                    .flatMapSingle {
//                        controller
//                            .selectedSalesPeople
//                            .take(1)
//                            .flatMap { it.toObservable() }
//                            .flatMap { it.customerAssignments.toObservable() }
//                            .distinct()
//                            .toSet()
//                    }.subscribe(controller.searchCustomers)
            }

            button("⇇") {
                tooltip("Apply selected Customers to Selected Sales Persons (CTRL + ←)")

                useMaxWidth = true
                textFill = Color.GREEN

                val keyEvents = table.events(KeyEvent.KEY_PRESSED).filter { it.isControlDown && it.code == KeyCode.LEFT }
                val buttonEvents = actionEvents()

                Observable
                    .merge(keyEvents, buttonEvents)
                    .subscribe {
                        controller.applyCustomers(selectedCustomers)
                    }


//                Observable
//                    .merge(keyEvents, buttonEvents)
//                    .flatMapSingle {
//                    controller
//                        .selectedCustomers
//                        .take(1)
//                        .flatMap { it.toObservable() }
//                        .map { it.id ?: -1 }
//                        .toSet()
//                    }
//                    .subscribe(controller.applyCustomers)
            }
            //remove selected customers
            button("⇉") {
                tooltip("Remove selected Customers from Selected Sales Persons (CTRL + →)")

                useMaxWidth = true
                textFill = Color.RED

                val keyEvents = table.events(KeyEvent.KEY_PRESSED).filter { it.isControlDown && it.code == KeyCode.RIGHT }
                val buttonEvents = actionEvents()

                Observable
                    .merge(keyEvents, buttonEvents)
                    .subscribe {
                        controller.removeCustomerUsages(selectedCustomers)
                    }

//                Observable.merge(keyEvents,buttonEvents)
//                    .flatMapSingle {
//                    controller.selectedCustomers.take(1)
//                        .flatMap { it.toObservable() }
//                        .map { it.id ?: -1 }
//                        .toSet()
//                }.subscribe(controller.removeCustomerUsages)

            }
            //add customer button
            button("\u2795") {
                tooltip("Create a new Customer")
                useMaxWidth = true
                textFill = Color.BLUE

                actionEvents().map { Unit }.subscribe(controller.createNewCustomer)
            }

            //remove customer button
            button("✘") {
                tooltip("Remove selected Customers")
                useMaxWidth = true
                textFill = Color.RED

                action {
                    val deleteItems = table.selectionModel.selectedItems.mapNotNull { it.id }.toSet()

                    Alert(Alert.AlertType.WARNING, "Are you sure you want to delete these ${deleteItems.size} customers?", ButtonType.YES, ButtonType.NO)
                        .showAndWait()
                        .filter { it == ButtonType.YES }
                        .map { deleteItems }
                        .map { controller.deleteCustomers(it) }
                }
            }
        }

//        //create new Customer requests
//        controller.createNewCustomer
//            .flatMapMaybe { NewCustomerDialog().toMaybe() }
//            .flatMapMaybe { it }
//            .flatMapSingle {
//                db
//                    .loadCustomer(it.id)
//                    .toSingle()
//            }
//            .subscribe {
//                table.items.add(it)
//                table.selectionModel.clearSelection()
//                table.selectionModel.select(it)
//                table.requestFocus()
//            }

        //refresh on deletion
        controller.deletedCustomers
            .map { Unit }
            .subscribe(controller.refreshCustomers) //push this refresh customers

    }
}
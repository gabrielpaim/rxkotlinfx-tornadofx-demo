package view

import app.Styles
import com.github.thomasnield.rxkotlinfx.actionEvents
import com.github.thomasnield.rxkotlinfx.events
import com.github.thomasnield.rxkotlinfx.onChangedObservable
import domain.Customer
import domain.persistence.Persistence
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.toObservable
import javafx.geometry.Orientation
import javafx.scene.control.TableView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.BorderPane
import tornadofx.*

class AppliedCustomerView : View() {
    override val root = BorderPane()

    private val controller: ApplicationController by inject()
    private var table: TableView<Customer> by singleAssign()

    private val db: Persistence by di()

    init {
        with(root) {

            top = label("ASSIGNED CUSTOMERS").addClass(Styles.heading)

            center = tableview<Customer> {
                table = this

                readonlyColumn("ID", Customer::id)
                readonlyColumn("Name", Customer::name)

                // Broadcast selections
                selectionModel.selectedItems.onChangedObservable()
                    .map {
                        it
                            .asSequence()
                            .map { customer -> customer.id }
                            .filterNotNull()
                            .toSet()
                    }
                    .subscribe { controller.selectedApplications.onNext(it) }

                // Subscribe to selections in SalesPeopleView extract a list of customers

                // If multiple SalesPeople are selected, we consolidate their customers distinctly.
                // Otherwise we will push out a hot list of Customers for that one SalesPerson.
                // It will update automatically and the switchMap() will kill it when the selection changes
                controller.selectedSalesPeople
                    .switchMap { selectedPeople ->
                        //the switchMap() is amazing! it unsubscribes the previous mapped Observable when a new one comes in

                        if (selectedPeople.size == 1) {
                            selectedPeople
                                .toObservable()
                                .flatMap { it ->
                                    it.customerAssignments
                                        .onChangedObservable()
                                        .switchMapSingle { customersIds ->
                                            customersIds.toObservable()
                                                .flatMapSingle {
                                                    db.loadCustomer(it).toSingle()
                                                }
                                                .toList()
                                        }
                                }
                        } else {
                            selectedPeople
                                .toObservable()
                                .flatMap { it.customerAssignments.toObservable() }
                                .distinct()
                                .flatMapSingle {
                                    db.loadCustomer(it)
                                        .toSingle()
                                }
                                .toSortedList { x,y ->
                                    if (x.id != null && y.id != null) {
                                        x.id.compareTo(y.id)
                                    } else {
                                        -1
                                    }
                                }
//                                .map { listCustomer ->
//                                    listCustomer
//                                        .filter { customer ->
//                                            customer.id != -1
//                                        }
//                                }
                                .toObservable()
                        }
                    }.subscribeBy(
                        onNext = {
                            items.setAll(it)
                            selectWhere { it.id in selectionModel.selectedItems.asSequence().filterNotNull().map { it.id }.toSet() }
                            requestFocus()
                            resizeColumnsToFitContent()
                        },
                        onError = {
                            println("Error--${it.message}")
                        }
                    )
            }
            left = toolbar {
                orientation = Orientation.VERTICAL
                button("▲") {
                    tooltip("Move customer up (CTRL + ↑)")

                    //disable when multiple salespeople selected
                    controller.selectedSalesPeople.map { it.size > 1 }.subscribe { isDisable = it }

                    //broadcast move up requests
                    val keyEvents =  table.events(KeyEvent.KEY_PRESSED).filter { it.isControlDown && it.code == KeyCode.UP }
                    val buttonEvents = actionEvents()

                    Observable.merge(keyEvents, buttonEvents)
                        .filter { table.selectedItem?.id != null }
                        .map { table.selectedItem?.id }
                        .subscribe(controller.moveCustomerUp)


                    // re-select moved customer
                    controller.moveCustomerUp.subscribe { customerId ->
                        table.selectWhere { it.id == customerId  }
                    }
                    useMaxWidth = true
                }
                button("▼") {
                    tooltip("Move customer down (CTRL + ↓)")

                    //disable when multiple salespeople selected
                    controller.selectedSalesPeople.map { it.size > 1 }.subscribe { isDisable = it }

                    //broadcast move down requests
                    val keyEvents =  table.events(KeyEvent.KEY_PRESSED).filter { it.isControlDown && it.code == KeyCode.DOWN }
                    val buttonEvents = actionEvents()

                    Observable.merge(keyEvents, buttonEvents)
                        .filter { table.selectedItem != null }
                        .map { table.selectedItem!!.id }
                        .subscribe(controller.moveCustomerDown)


                    // re-select moved customer
                    controller.moveCustomerDown.subscribe { customerId ->
                        table.selectWhere { it.id == customerId  }
                    }

                    useMaxWidth = true
                }
                button("\uD83D\uDD0E⇉") {
                    actionEvents().flatMap {
                        controller.selectedApplications.take(1)
                    }.subscribe(controller.searchCustomers)
                }
            }
        }
    }
}
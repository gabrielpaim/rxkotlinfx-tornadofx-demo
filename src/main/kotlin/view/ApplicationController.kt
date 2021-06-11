package view

import app.toSet
import domain.Assignment
import domain.Customer
import domain.SalesPerson
import domain.persistence.Persistence
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import io.reactivex.rxkotlin.toObservable
import io.reactivex.rxkotlin.zipWith
import io.reactivex.subjects.BehaviorSubject
import javafx.collections.ObservableList
import tornadofx.*

class ApplicationController: Controller() {

    private val db : Persistence by di()

//    data class SearchCustomersUsagesEvent(val customerIds: Set<Int>) : FXEvent()
//    object RefreshSalesPerson: FXEvent()

    val searchCustomers = BehaviorSubject.create<Set<Int>>().apply {
        subscribe {
            println("searchCustomers emitted: $it")
        }
    }

    val searchCustomerUsages = BehaviorSubject.create<Set<Int>>().apply {
        subscribe {
            println("searchCustomerUsages emitted: $it")
        }
    }

    val applyCustomers = BehaviorSubject.create<Set<Int>>()
    val removeCustomerUsages = BehaviorSubject.create<Set<Int>>()

    val refreshSalesPeople = BehaviorSubject.create<Unit>()
    val refreshCustomers = BehaviorSubject.create<Unit>()

    val selectedSalesPeople = BehaviorSubject.create<Set<SalesPerson>>()
    val selectedApplications = BehaviorSubject.create<Set<Int>>()

    val moveCustomerUp = BehaviorSubject.create<Int>()
    val moveCustomerDown = BehaviorSubject.create<Int>()

    val saveAssignments = BehaviorSubject.create<Unit>()

    val createNewCustomer = BehaviorSubject.create<Unit>()
    val deleteCustomers = BehaviorSubject.create<Set<Int>>()
    val deletedCustomers = BehaviorSubject.create<Set<Int>>()

    val createNewSalesPerson = BehaviorSubject.create<Unit>()
    val deleteSalesPerson = BehaviorSubject.create<Set<Int>>()


    fun searchSelectedCustomers(customers: List<Customer>) {
//        fire(SearchCustomersUsagesEvent(customers.mapNotNull { it.id }.toSet()))
        searchCustomerUsages.onNext(customers.mapNotNull { it.id }.toSet())
    }

    fun applyCustomers(customers: List<Customer>) {
        applyCustomers.onNext(customers.mapNotNull { it.id }.toSet())
    }

    fun removeCustomerUsages(customers: List<Customer>) {
        removeCustomerUsages.onNext(customers.mapNotNull { it.id }.toSet())
    }

    fun searchSelectedApplied() {
        selectedSalesPeople
            .take(1)
            .flatMap { it.toObservable() }
            .flatMap { it.customerAssignments.toObservable() }
            .distinct()
            .toSet()




//        .flatMapSingle {
//            controller
//                .selectedSalesPeople
//                .take(1)
//                .flatMap { it.toObservable() }
//                .flatMap { it.customerAssignments.toObservable() }
//                .distinct()
//                .toSet()
//        }.subscribe(controller.searchCustomers)
    }

    fun handleAssignments(items: List<SalesPerson>) {
        saveAssignments
            .flatMapMaybe {
                items
                    .toObservable()
                    .flatMapMaybe { saveAssignments(it.id, it.customerAssignments).toMaybe() }
                    .reduce { x,y -> x + y}
                    .doOnSuccess { println("Committed $it changes") }
            }
            .map { }
            .subscribe(refreshSalesPeople)
    }

    //Compares original and new Customer ID assignments and writes them to database
    fun saveAssignments(id: Int?, customerAssignments: ObservableList<Int>): Single<Long> {
        return if (id == null) {
            Single.error(IllegalArgumentException("SalesPerson is not saved"))
        } else {
            val newItems = customerAssignments
                .toObservable()
                .zipWith(Observable.range(1,Int.MAX_VALUE))
                .map { (item, index) ->
                    Assignment(salesPersonId = id, customerId = item, order = index)
                }
                .toSet()

            val previousItems = db
                .listAllAssignmentsForSalesPerson(id)
                .toSet()

            //zip old and new assignments together, compare them, and write changes
            return Singles
                .zip(newItems, previousItems)
                .flatMapObservable { (new, previous) ->
                    Observable
                        .merge(
                            new.toObservable()
                                .filter { !previous.contains(it) }
                                .flatMapSingle { db.saveAssignment(it) },
                            previous.toObservable()
                                .filter { !new.contains(it) }
                                .flatMapSingle { db.deleteAssignment(it.id!!) }
                        )
                }
                .count()
        }
    }
}
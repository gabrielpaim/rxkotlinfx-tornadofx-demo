package view

import domain.Customer
import domain.SalesPerson
import io.reactivex.rxkotlin.toObservable
import io.reactivex.subjects.BehaviorSubject
import javafx.beans.property.SimpleObjectProperty
import tornadofx.*

class ApplicationController: Controller() {

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

    val selectedCustomers = BehaviorSubject.create<Set<Customer>>().apply {
        subscribe {
            println("selectedCustomers emitted: $it")
        }
    }
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

//    fun handleAssignments(items: List<SalesPerson>) {
//        // ------------ handle commits ---------------
//        // Forma 1
////        saveAssignments
////            .flatMap {
////                items
////                    .toObservable()
////                    .flatMapMaybe {
////                        it.saveAssignments().toMaybe()
////                    }
////            }
////            .map { }
////            .subscribe(refreshSalesPeople)
//////            .subscribe { fire(RefreshSalesPerson) }
//
//        // Forma 2
//            saveAssignments
//                .flatMapMaybe {
//                    items
//                        .toObservable()
//                        .flatMapMaybe { it.saveAssignments().toMaybe() }
//                        .reduce { x,y -> x + y}
//                        .doOnSuccess { println("Committed $it changes") }
//                }
//                .map { }
//                .subscribe(refreshSalesPeople)
//        // ----------------------------------------------------
//    }

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
}
package view

import domain.Customer
import domain.SalesPerson
import io.reactivex.subjects.BehaviorSubject
import tornadofx.Controller

class ApplicationController: Controller() {

    val searchCustomers = BehaviorSubject.create<Set<Int>>()
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

    fun searchSelectedCustomers(customers: List<Customer>) {
        searchCustomerUsages.onNext(customers.mapNotNull { it.id }.toSet())
    }

    fun applyCustomers(customers: List<Customer>) {
        applyCustomers.onNext(customers.mapNotNull { it.id }.toSet())
    }

    fun removeCustomerUsages(customers: List<Customer>) {
        removeCustomerUsages.onNext(customers.mapNotNull { it.id }.toSet())
    }
}
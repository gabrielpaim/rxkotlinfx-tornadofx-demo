package domain.persistence

import domain.Assignment
import domain.Customer
import domain.SalesPerson
import domain.SalesPersonItem
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Database.
 *
 * Obs: *save* means insert or update.
 */
interface Persistence {

    fun deleteCustomer(id: Int): Single<Int>

    fun saveCustomer(customer: Customer): Single<Customer>

    fun loadCustomer(id: Int?): Maybe<Customer>

    fun listAllCustomers(): Observable<Customer>

    fun deleteSalesPerson(id: Int): Single<Int>

    fun saveSalesPerson(salesPerson: SalesPerson): Single<SalesPerson>

    fun loadSalesPerson(id: Int): Maybe<SalesPerson>

    fun listAllSalesPersons(): Observable<SalesPerson>

    fun listAllAssignmentsForSalesPerson(salesPersonId: Int): Observable<Assignment>

    fun saveAssignment(assignment: Assignment): Single<Assignment>

    fun deleteAssignment(assignmentId: Int): Single<Int>

    fun listSalesPersonItems(): Observable<SalesPersonItem>

}
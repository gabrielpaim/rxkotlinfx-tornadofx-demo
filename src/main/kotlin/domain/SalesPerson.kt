package domain

import app.flatCollect
import app.toSet
import com.github.thomasnield.rxkotlinfx.addTo
import com.github.thomasnield.rxkotlinfx.onChangedObservable
import com.github.thomasnield.rxkotlinfx.toBinding
import domain.persistence.Persistence
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxjavafx.subscriptions.CompositeBinding
import io.reactivex.rxkotlin.*
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.paint.Color
import javafx.scene.text.Text
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.nield.rxkotlinjdbc.execute
import org.nield.rxkotlinjdbc.insert
import org.nield.rxkotlinjdbc.select

data class SalesPerson(val firstName: String,
                       val lastName: String,
                       val id: Int? = null) : KoinComponent {

    private val db: Persistence by inject()

    //We maintain a collection of bindings and disposables to unsubscribe them later
    private val bindings = CompositeBinding()
    private val disposables = CompositeDisposable()

    // Hold original customer assignments for dirty validation
    val originalAssignments by lazy {
        if (id != null) {
            FXCollections.observableArrayList<Int>().apply {
                db.listAllAssignmentsForSalesPerson(id)
                    .map { it.customerId }
                    .subscribe { add(it) }
                    .addTo(disposables)
            }
        } else {
            emptyList()
        }
    }


    // The staged Customer ID's for this SalesPerson
    val customerAssignments = FXCollections.observableArrayList(originalAssignments)


    // A Binding holding formatted concatenations of the CompanyClient ID's for this SalesPerson
    val customerAssignmentsConcat = customerAssignments.onChangedObservable()
                .map {
                    Text(it.joinToString("|")).apply {
                        if (originalAssignments != it) fill = Color.RED
                    }
                }
                .toBinding()
                .addTo(bindings)


    //Compares original and new Customer ID assignments and writes them to database
    fun saveAssignments(): Single<Long> {

        val newItems = customerAssignments.toObservable()
                .zipWith(Observable.range(1,Int.MAX_VALUE))
                .map { (item, index) ->  Assignment(-1,id,item,index)}
                .toSet()

        val previousItems =  db.listAllAssignmentsForSalesPerson(id)
//            assignmentsFor(id).toSet()

        //zip old and new assignments together, compare them, and write changes
        return Singles.zip(newItems, previousItems)
            .flatMapObservable { (new,old) ->

                Observable.merge(
                        new.toObservable().filter { !old.contains(it) }.flatMapSingle { (it) },
                        old.toObservable().filter { !new.contains(it) }.flatMapSingle { db.deleteAssignment(it.id) }
                )
            }.count()
    }

    fun delete(): Single<Int> {
        return if (id == null) {
            Single.error(IllegalArgumentException("User is not saved"))
        } else {
            db.deleteSalesPerson(id)
        }
    }


    /**Releases any reactive disposables associated with this SalesPerson.
     * This is very critical to prevent memory leaks with infinite hot Observables
     * because they do not know when they are complete
     */
    fun dispose() {
        bindings.dispose()
        disposables.dispose()
    }

    companion object {

//        val all = db.listAllSalesPersons()
//
//        fun forId(id: Int) = db.loadSalesPerson(id)
//
//        fun assignmentsFor(salesPersonId: Int) = db.listAllAssignmentsForSalesPerson(salesPersonId)
//
//        fun createNew(firstName: String, lastName: String) = db.saveSalesPerson(SalesPerson(firstName, lastName))
//
//        private fun writeAssignment(assignment: Assignment) = db.saveAssignment(assignment)
//
//        private fun removeAssignment(assignmentId: Int) = db.deleteAssignment(assignmentId)
    }
}
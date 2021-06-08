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
import javafx.scene.paint.Color
import javafx.scene.text.Text
import org.nield.rxkotlinjdbc.execute
import org.nield.rxkotlinjdbc.insert
import org.nield.rxkotlinjdbc.select

data class SalesPerson(val firstName: String,
                       val lastName: String,
                       val id: Int? = null) {

    //We maintain a collection of bindings and disposables to unsubscribe them later
    private val bindings = CompositeBinding()
    private val disposables = CompositeDisposable()

    // Hold original customer assignments for dirty validation
    val originalAssignments = FXCollections.observableArrayList<Int>().apply {
            assignmentsFor(id)
                    .map { it.customerId }
                    .subscribe { add(it) }
                    .addTo(disposables)
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
    fun saveAssignments(): Single<Long>? {

        val newItems = customerAssignments.toObservable()
                .zipWith(Observable.range(1,Int.MAX_VALUE))
                .map { (item, index) ->  Assignment(-1,id,item,index)}
                .toSet()

        val previousItems = assignmentsFor(id).toSet()

        //zip old and new assignments together, compare them, and write changes
        return Singles.zip(newItems, previousItems)
            .flatMapObservable { (new,old) ->

                Observable.merge(
                        new.toObservable().filter { !old.contains(it) }.flatMapSingle { writeAssignment(it) },
                        old.toObservable().filter { !new.contains(it) }.flatMapSingle { removeAssignment(it.id) }
                )
            }.count()
    }

    fun delete() = db.execute("DELETE FROM SALES_PERSON WHERE ID = ?")
        .parameter(id)
        .toSingle()


    /**Releases any reactive disposables associated with this SalesPerson.
     * This is very critical to prevent memory leaks with infinite hot Observables
     * because they do not know when they are complete
     */
    fun dispose() {
        bindings.dispose()
        disposables.dispose()
    }

    companion object {

        val all = db.listAllSalesPersons()

        fun forId(id: Int) = db.loadSalesPerson(id)

        // Retrieves all assigned CompanyClient ID's for a given SalesPerson
        fun assignmentsFor(salesPersonId: Int) =
                db.select("SELECT * FROM ASSIGNMENT WHERE SALES_PERSON_ID = :salesPersonId ORDER BY APPLY_ORDER")
                        .parameter("salesPersonId", salesPersonId)
                        .toObservable { Assignment(it.getInt("ID"), it.getInt("SALES_PERSON_ID"), it.getInt("CUSTOMER_ID"), it.getInt("APPLY_ORDER")) }
                        .flatCollect()

        // Creates a new SalesPerson
        fun createNew(firstName: String, lastName: String) = db.saveSalesPerson(SalesPerson(firstName, lastName))

        //commits assignments
        private fun writeAssignment(assignment: Assignment) =
                db.insert("INSERT INTO ASSIGNMENT (SALES_PERSON_ID, CUSTOMER_ID, APPLY_ORDER) VALUES (:salesPersonId, :customerId, :applyOrder)")
                        .parameter("salesPersonId", assignment.salesPersonId)
                        .parameter("customerId", assignment.customerId)
                        .parameter("applyOrder", assignment.order)
                        .toSingle { it.getInt(1) }

        //deletes assignments
        private fun removeAssignment(assignmentId: Int) =
                db.execute("DELETE FROM ASSIGNMENT WHERE ID = :id")
                        .parameter("id",assignmentId)
                        .toSingle()
    }
}
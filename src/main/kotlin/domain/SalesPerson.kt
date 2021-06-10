package domain

import app.toSet
import com.github.thomasnield.rxkotlinfx.addTo
import com.github.thomasnield.rxkotlinfx.onChangedObservable
import com.github.thomasnield.rxkotlinfx.toBinding
import domain.persistence.Persistence
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxjavafx.subscriptions.CompositeBinding
import io.reactivex.rxkotlin.Singles
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.toObservable
import io.reactivex.rxkotlin.zipWith
import javafx.collections.FXCollections
import javafx.scene.paint.Color
import javafx.scene.text.Text
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


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
    val customerAssignmentsConcat = customerAssignments
        .onChangedObservable()
        .map {
            Text(it.joinToString("|")).apply {
                if (originalAssignments != it) fill = Color.RED
            }
        }
        .toBinding()
        .addTo(bindings)
    // TODO: make it work


    //Compares original and new Customer ID assignments and writes them to database
    fun saveAssignments(): Single<Long> {
        return if (id == null) {
            Single.error(IllegalArgumentException("SalesPerson $this is not saved"))
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

    /**Releases any reactive disposables associated with this SalesPerson.
     * This is very critical to prevent memory leaks with infinite hot Observables
     * because they do not know when they are complete
     */
    fun dispose() {
        bindings.dispose()
        disposables.dispose()
    }

}
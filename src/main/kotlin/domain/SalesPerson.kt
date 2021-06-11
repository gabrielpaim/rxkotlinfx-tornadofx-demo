package domain

import com.github.thomasnield.rxkotlinfx.addTo
import com.github.thomasnield.rxkotlinfx.onChangedObservable
import com.github.thomasnield.rxkotlinfx.toBinding
import domain.persistence.Persistence
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxjavafx.subscriptions.CompositeBinding
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import javafx.collections.FXCollections
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


data class SalesPerson(val firstName: String,
                       val lastName: String,
                       val id: Int? = null) : KoinComponent {

    private val db: Persistence by inject()

    //We maintain a collection of bindings and disposables to unsubscribe them later
    val bindings = CompositeBinding()
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
//    val customerAssignmentsConcat = customerAssignments
//        .onChangedObservable()
//        .map { newList ->
//            Text(newList.joinToString("|")).apply {
//                if (originalAssignments != newList) fill = Color.RED
//            }
//        }
//        .toBinding()
//        .addTo(bindings)

    val customerAssignmentsConcat = customerAssignments
        .onChangedObservable()
        .map {
            it to originalAssignments
        }
        .subscribeOn(Schedulers.computation())
        .toBinding()
        .addTo(bindings)


    /**Releases any reactive disposables associated with this SalesPerson.
     * This is very critical to prevent memory leaks with infinite hot Observables
     * because they do not know when they are complete
     */
    fun dispose() {
        bindings.dispose()
        disposables.dispose()
    }

}
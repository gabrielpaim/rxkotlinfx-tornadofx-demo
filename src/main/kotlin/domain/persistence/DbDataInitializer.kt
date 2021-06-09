package domain.persistence

import domain.Customer
import domain.SalesPerson
import io.reactivex.Completable
import io.reactivex.Observable

fun initializeData(db: Persistence): Completable {
    return with(db) {
        createCustomers().andThen(createSalesPersons())
    }
}

private fun Persistence.createCustomers(): Completable {
    val list = listOf(
        "Alpha Analytics",
        "Rexon Solutions",
        "Travis Technologies",
        "Anex Applications",
        "Edvin Enterprises",
        "T-Boom Consulting",
        "Nield Industrial",
        "Dash Inc"
    )

    return Observable
        .fromIterable(list)
        .map { name -> Customer(name) }
        .flatMapSingle { saveCustomer(it) }
        .ignoreElements()
}

private fun Persistence.createSalesPersons(): Completable {
    val list = listOf(
        "Joe" to "McManey",
        "Heidi" to "Howell",
        "Eric" to "Wentz",
        "Jonathon" to "Smith",
        "Samantha" to "Stewart",
        "Jillian" to "Michelle"
    )

    return Observable
        .fromIterable(list)
        .map { (firstName, lastName) -> SalesPerson(firstName, lastName) }
        .flatMapSingle { saveSalesPerson(it) }
        .ignoreElements()
}

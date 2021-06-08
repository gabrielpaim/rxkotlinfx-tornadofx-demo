package domain.persistence

import domain.Customer
import domain.SalesPerson

// TODO: fazer todo ele reactive, sem blockingGet
fun initializeData(db: Persistence) {
    db.createCustomers()
    db.createSalesPersons()
}

private fun Persistence.createCustomers() {
    listOf(
        "Alpha Analytics",
        "Rexon Solutions",
        "Travis Technologies",
        "Anex Applications",
        "Edvin Enterprises",
        "T-Boom Consulting",
        "Nield Industrial",
        "Dash Inc"
    )
        .map { name -> Customer(name) }
        .forEach {
            saveCustomer(it).blockingGet()
        }
}

private fun Persistence.createSalesPersons() {
    listOf(
        "Joe" to "McManey",
        "Heidi" to "Howell",
        "Eric" to "Wentz",
        "Jonathon" to "Smith",
        "Samantha" to "Stewart",
        "Jillian" to "Michelle"
    ).map { (firstName, lastName) -> SalesPerson(firstName, lastName) }
        .forEach { saveSalesPerson(it).blockingGet() }
}

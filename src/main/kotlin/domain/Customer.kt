package domain


data class Customer(val name: String, val id: Int? = null) {

    // TODO remove
    companion object {

        val all = db.listAllCustomers()

        fun forId(id: Int) = db.loadCustomer(id)

        fun createNew(name: String) = db.saveCustomer(Customer(name))

    }
}
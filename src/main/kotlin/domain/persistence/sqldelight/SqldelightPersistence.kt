//package domain.persistence.sqldelight
//
//import app.db.DatabaseTest
//import com.squareup.sqldelight.logs.LogSqliteDriver
//import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
//import domain.Customer
//import domain.persistence.Persistence
//import io.reactivex.Maybe
//import io.reactivex.Single
//
//class SqldelightPersistence private constructor(db: DatabaseTest): Persistence {
//
//    private val q = db.queriesQueries
//
//    override fun deleteCustomer(id: Int): Single<Int> {
//        return Single.fromCallable {
//            q.deleteCustomer(id)
//            id
//        }
//    }
//
//
//    override fun saveCustomer(customer: Customer): Single<Customer> {
//        return Single.fromCallable {
//
//            // Check if customer is already saved
//            if (customer.id != null && q.getCustomer(customer.id).executeAsOneOrNull() != null) {
//                // update customer
//                q.updateCustomer(customer.name, customer.id)
//                customer
//            } else {
//                // insert new customer
//                val id: Int? = q.transactionWithResult {
//                    q.insertCustomer(customer.name)
//                    q.lastInsertRowId().executeAsOneOrNull()?.toInt()
//                }
//                customer.copy(id = id)
//            }
//        }
//    }
//
//    override fun loadCustomer(id: Int): Maybe<Customer> {
//        TODO("Not yet implemented")
//    }
//
//    companion object {
//
//        fun create(url: String): SqldelightPersistence {
//            val driver = LogSqliteDriver(JdbcSqliteDriver(url).also {
//                DatabaseTest.Schema.create(it)
//            }) {
//                println("SQL: $it");
//            }
//
//            val transacter = DatabaseTest(driver)
//            return SqldelightPersistence(transacter)
//        }
//    }
//}
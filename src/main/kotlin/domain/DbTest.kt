package domain

import app.db.AppdbQueries
import app.db.Customer
import app.db.DatabaseTest
import com.squareup.sqldelight.logs.LogSqliteDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver

object DbTest {
    private val driver = LogSqliteDriver(JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also {
        DatabaseTest.Schema.create(it)
    }) {
        println("SQL: $it");
    }

//    val db = DatabaseTest(driver).also {
//        it.appdbQueries.saveCustomer(Customer(18723L, "lixo", "loxo"))
//    }

    val db = DatabaseTest(driver)

    val test = db.appdbQueries.saveCustomer(Customer(18723L, "lixo", "loxo"))
}
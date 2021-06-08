package domain.persistence.rxjava_jdbc

import app.flatCollect
import domain.Assignment
import domain.Customer
import domain.SalesPerson
import domain.persistence.Persistence
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import org.nield.rxkotlinjdbc.execute
import org.nield.rxkotlinjdbc.insert
import org.nield.rxkotlinjdbc.select
import java.sql.Connection
import java.sql.DriverManager

class RxjavaJdbcPersistence(private val db: Connection): Persistence {

    override fun deleteCustomer(id: Int): Single<Int> {
        return db
            .execute("DELETE FROM CUSTOMER WHERE ID = ?")
            .parameter(id)
            .toSingle()
    }

    override fun saveCustomer(customer: Customer): Single<Customer> {

        fun insert(): Single<Customer> {
            return db
                .insert("INSERT INTO CUSTOMER (NAME) VALUES (:name)")
                .parameter("name",customer.name)
                .toSingle {
                    customer.copy(id = it.getInt("ID"))
                }
        }

        fun update(): Single<Customer> {
            return db
                .insert("UPDATE CUSTOMER (NAME) VALUES (:name)")
                .parameter("name",customer.name)
                .toSingle { customer }
        }

        return loadCustomer(customer.id)
            .toObservable()
            .flatMapSingle { update() }
            .switchIfEmpty(insert().toObservable())
            .singleOrError()
    }


    override fun loadCustomer(id: Int?): Maybe<Customer> {
        if (id == null) return Maybe.empty()

        return db
            .select("SELECT * FROM CUSTOMER WHERE ID = ?")
            .parameter(id)
            .toMaybe { Customer(it.getString("NAME"), it.getInt("ID")) }
    }

    override fun listAllCustomers(): Observable<Customer> {
        return db.select("SELECT * FROM CUSTOMER")
            .toObservable { Customer(it.getString("NAME"), it.getInt("ID")) }
    }

    override fun deleteSalesPerson(id: Int): Single<Int> {
        TODO("Not yet implemented")
    }

    override fun saveSalesPerson(salesPerson: SalesPerson): Single<SalesPerson> {
        return if (salesPerson.id != null && loadSalesPerson(salesPerson.id).blockingGet() != null) {
            // TODO update
            Single.error(IllegalStateException("TODO"))
        } else {
            db.insert("INSERT INTO SALES_PERSON (FIRST_NAME,LAST_NAME) VALUES (:firstName,:lastName)")
                .parameter("firstName", salesPerson.firstName)
                .parameter("lastName", salesPerson.lastName)
                .toSingle {
                    val id = it.getInt("ID")
                    salesPerson.copy(id=id)
                }
        }
    }

    override fun loadSalesPerson(id: Int): Maybe<SalesPerson> {
        return db.select("SELECT * FROM SALES_PERSON WHERE ID = ?")
            .parameter(id)
            .toMaybe { SalesPerson(it.getString("FIRST_NAME"), it.getString("LAST_NAME"), it.getInt("ID")) }
    }

    override fun listAllSalesPersons(): Observable<SalesPerson> {
        return db.select("SELECT * FROM SALES_PERSON")
            .toObservable {
                SalesPerson(it.getString("FIRST_NAME"), it.getString("LAST_NAME"), it.getInt("ID"))
            }
            .flatCollect()
    }


    // Retrieves all assigned CompanyClient ID's for a given SalesPerson
    fun assignmentsFor(salesPersonId: Int) =
        db.select("SELECT * FROM ASSIGNMENT WHERE SALES_PERSON_ID = :salesPersonId ORDER BY APPLY_ORDER")
            .parameter("salesPersonId", salesPersonId)
            .toObservable { Assignment(it.getInt("ID"), it.getInt("SALES_PERSON_ID"), it.getInt("CUSTOMER_ID"), it.getInt("APPLY_ORDER")) }
            .flatCollect()

    // Creates a new SalesPerson

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


    companion object {
        fun create(url: String): RxjavaJdbcPersistence {
            val connection = DriverManager.getConnection(url).apply {
                // Create Tables
                javaClass.getResourceAsStream("/jdbc/schema-db.sql")?.let { stream ->
                    stream.bufferedReader().useLines { lines ->
                        lines.forEach {
                            execute(it).toSingle().blockingGet()
                        }
                    }
                }

//                execute("CREATE TABLE CUSTOMER (ID INTEGER PRIMARY KEY, NAME VARCHAR)")
//                    .toSingle()
//                    .blockingGet()
//
//                execute("CREATE TABLE SALES_PERSON (ID INTEGER PRIMARY KEY, FIRST_NAME VARCHAR, LAST_NAME VARCHAR)")
//                    .toSingle()
//                    .blockingGet()
//
//                execute("CREATE TABLE ASSIGNMENT (ID INTEGER PRIMARY KEY, " +
//                        "CUSTOMER_ID INTEGER, SALES_PERSON_ID INTEGER, APPLY_ORDER INTEGER)")
//                    .toSingle()
//                    .blockingGet()
            }

            return RxjavaJdbcPersistence(connection)
        }
    }
}
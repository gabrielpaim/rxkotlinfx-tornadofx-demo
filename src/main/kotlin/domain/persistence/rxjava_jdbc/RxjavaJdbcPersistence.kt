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
            .execute("DELETE FROM CUSTOMER WHERE ID = :id")
            .parameter("id", id)
            .toSingle()
    }

    override fun saveCustomer(customer: Customer): Single<Customer> {

        fun insert(): Single<Customer> {
            return db
                .insert("INSERT INTO CUSTOMER (NAME) VALUES (:name)")
                .parameter("name",customer.name)
                .toSingle { customer.copy(id = it.getInt(1)) }
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
        db
            .execute("DELETE FROM SALES_PERSON WHERE ID = :id")
            .parameter("id", id)
            .toSingle()
        return Single.just(id)
    }

    override fun saveSalesPerson(salesPerson: SalesPerson): Single<SalesPerson> {
        return if (salesPerson.id != null && loadSalesPerson(salesPerson.id).blockingGet() != null) {
            return db.insert("UPDATE SALES_PERSON SET FIRST_NAME = ?, LAST_NAME  = ? where ID = ? ")
                .parameter(salesPerson.firstName)
                .parameter( salesPerson.lastName)
                .parameter(salesPerson.id)
                .toSingle {
                    salesPerson
                }
        } else {
            db.insert("INSERT INTO SALES_PERSON (FIRST_NAME,LAST_NAME) VALUES (:firstName,:lastName)")
                .parameter("firstName", salesPerson.firstName)
                .parameter("lastName", salesPerson.lastName)
                .toSingle {
                    val id = it.getInt(1)
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

    override fun listAllAssignmentsForSalesPerson(salesPersonId: Int): Observable<Assignment> {
        return db.select("SELECT * FROM ASSIGNMENT WHERE SALES_PERSON_ID = :salesPersonId ORDER BY APPLY_ORDER")
            .parameter("salesPersonId", salesPersonId)
            .toObservable {
                Assignment(it.getInt("ID"), it.getInt("SALES_PERSON_ID"), it.getInt("CUSTOMER_ID"), it.getInt("APPLY_ORDER"))
            }
            .flatCollect()
    }

    override fun saveAssignment(assignment: Assignment): Single<Assignment> {
        return db.insert("INSERT INTO ASSIGNMENT (SALES_PERSON_ID, CUSTOMER_ID, APPLY_ORDER) VALUES (:salesPersonId, :customerId, :applyOrder)")
            .parameter("salesPersonId", assignment.salesPersonId)
            .parameter("customerId", assignment.customerId)
            .parameter("applyOrder", assignment.order)
            .toSingle {
                assignment.copy(id = it.getInt(1))
            }
    }

    override fun deleteAssignment(assignmentId: Int): Single<Int> {
        return db.execute("DELETE FROM ASSIGNMENT WHERE ID = :id")
            .parameter("id",assignmentId)
            .toSingle()
    }

    companion object {
        fun create(url: String): RxjavaJdbcPersistence {

            val connection = DriverManager.getConnection(url).apply {

                // Create Tables
                javaClass.getResourceAsStream("/jdbc/schema-db.sql")?.let { stream ->
                    stream.bufferedReader().useLines { lines ->
                        lines
                            .filterNot { it.isBlank() }
                            .forEach {
                                execute(it).toSingle().blockingGet()
                            }
                    }
                }
            }

            return RxjavaJdbcPersistence(connection)
        }
    }
}
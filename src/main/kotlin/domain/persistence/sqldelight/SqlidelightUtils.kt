package domain.persistence.sqldelight

import app.db.CustomerEntity
import domain.Customer

fun Customer.toDbEntity() = CustomerEntity(id, firstName, lastName)
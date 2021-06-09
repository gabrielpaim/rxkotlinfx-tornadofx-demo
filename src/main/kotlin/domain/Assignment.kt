package domain

data class Assignment(val id: Int? = null,
                      val salesPersonId: Int,
                      val customerId: Int,
                      val order: Int)
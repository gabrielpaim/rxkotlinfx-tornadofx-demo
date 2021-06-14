package domain

data class SalesPersonItem(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val assignmentIds: List<Int>
)

//package furhatos.app.agent.flow.memory
import java.sql.DriverManager
import java.util.*

// the model class
data class User(val id: Int, val name: String)
data class One_shot(val id: Int, val name: String, val diet: List<String>, val allergies: Array<String>)
data class Long_term (val user_id: Int, val previous_recommendations: List<String>, val favourite_meals: List<String>, val favourite_ingredients: List<String>, val least_favourite_ingredients: List<String>)
data class Short_term (val user_id: Int, val preferences: List<String>, val prior_meal: String, val time: Date, val left_overs: List<String>)


fun main() {

    val jdbcUrl = "jdbc:postgresql://localhost:5432/postgres"

    // get the connection
    val connection = DriverManager
        .getConnection(jdbcUrl , "postgres", "postgres")

    // prints true if the connection is valid
    println(connection.isValid(0))

    // the query is only prepared not executed
    val query = connection.prepareStatement("SELECT * FROM users")

    // the query is executed and results are fetched
    val result = query.executeQuery()

    // an empty list for holding the results
    val users = mutableListOf<User>()

    while (result.next()) {

        // getting the value of the id column
        val id = result.getInt("id")

        // getting the value of the name column
        val name = result.getString("name")

        /*
        constructing a User object and
        putting data into the list
         */
        users.add(User(id, name))
    }
    /*
    [User(id=1, name=Kohli), User(id=2, name=Rohit),
    User(id=3, name=Bumrah), User(id=4, name=Dhawan)]
     */
    println(users)
}
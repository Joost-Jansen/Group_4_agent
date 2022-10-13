package furhatos.app.agent.flow.memory.long_term

data class long_term (val user_id: Int, val previous_recommendations: List<String>, val favourite_meals: List<String>, val favourite_ingredients: List<String>, val least_favourite_ingredients: List<String>){

}

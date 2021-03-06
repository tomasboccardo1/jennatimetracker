
/**
 * @author Leandro Larroulet
 * Date: 07/10/2010
 * Time: 17:20:54
 */
class ScoreManager {

  public static void addPoints(User user, String category, String subCategory){
    User actualUser = User.get(user.id)
    def criteria = ScoreCategory.createCriteria()
    def scoreCategory = criteria.get {
      eq("category", category)
      eq("subCategory", subCategory)
      eq("company", user.company)
      maxResults(1)
      firstResult(0)
    }

    if (actualUser && scoreCategory) {
      user.addToScores(new Score(user: user, company: user.company, date: new Date(), points: scoreCategory.points, scoreCategory: scoreCategory ))
    }
  }

}

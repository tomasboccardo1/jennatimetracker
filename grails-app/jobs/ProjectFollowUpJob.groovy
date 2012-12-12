
/**
 * @author Alejandro Gomez (alejandro.gomez@fdvsolutions.com)
 * Date: Mar 2, 2012
 * Time: 10:52:36 PM
 */
class ProjectFollowUpJob {

    def group = 'jenna-jobs'
    def name = 'project-follow-up-job'

    def sessionRequired = true
    def concurrent = false

    def projectFollowUpService

    static triggers = {
        cron name: 'everyMondayTrigger', cronExpression: "0 0 8 ? * MON"
    }

    def execute() {
		Company.all.each { Company company ->
			projectFollowUpService.sendEmailsToTeamLeaders(company)
		}
    }
}

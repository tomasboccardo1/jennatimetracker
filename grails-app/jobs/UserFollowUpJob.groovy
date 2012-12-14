class UserFollowUpJob {

    def group = 'jenna-jobs'
    def name = 'project-follow-up-job'
    def sessionRequired = true
    def concurrent = false
    def userFollowUpService

    static triggers = {
    }

    def execute() {
        Company.all.each { Company company ->
            userFollowUpService.sendEmailsToFollowers(company)
        }
    }
}

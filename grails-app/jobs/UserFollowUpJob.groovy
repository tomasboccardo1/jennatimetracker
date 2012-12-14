class UserFollowUpJob {

    def group = 'jenna-jobs'
    def name = 'user-follow-up-job'
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

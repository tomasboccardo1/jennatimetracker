class BadMoodDescriptionHeadsUpJob {

    def group = 'jenna-jobs'
    def name = 'bad-mood-description-heads-up-job'
    def sessionRequired = true
    def concurrent = false
    def badMoodDescriptionHeadsUpService

    static triggers = {
    }

    def execute() {
        Company.all.each { Company company ->
            badMoodDescriptionHeadsUpService.sendMailToAdm(company)
        }
    }
}

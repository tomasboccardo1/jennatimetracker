class IncompleteEffortsAlarmJob {

    def group = 'jenna-jobs'
    def name = 'incomplete-efforts-alarm-job'
    def sessionRequired = true
    def concurrent = false
    def incompleteEffortsAlarmService

    static triggers = {
    }

    def execute() {
        Company.all.each { Company company ->
            incompleteEffortsAlarmService.sendWarningMessageToUsers(company)
        }
    }
}

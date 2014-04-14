import org.springframework.context.MessageSource

import java.text.SimpleDateFormat

class HeadsUpService {

    boolean transactional = false

    MessageSource messageSource
    CompanyService companyService
    EmailNotificationService emailNotificationService
    def grailsApplication
    def databaseService

    def sendNewKnowledgeReport(company) {
        def today = new Date().clearTime()
        def from = today - 7
        def to = today + 1

        Calendar cal = Calendar.getInstance()
        int weekNumber = cal.get(Calendar.WEEK_OF_YEAR)
        int year = cal.get(Calendar.YEAR)


        def newKnowledge = companyService.listNewLearnings(company, from, to)

        if (newKnowledge.empty) {
            log.debug("No new knowledge for " + company.name)
            return
        }

        def knowledgePerUser = new HashMap().withDefault { 0 }

        List<User> usersWithMorePoints = []
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd")

        Integer userPoints, maxPoints = 0;
        String fromS = sdf.format(from)
        String toS = sdf.format(to)

        for (learning in newKnowledge) {
            if (!knowledgePerUser.get(learning.user)) {

                userPoints = (databaseService.getUsersPoints(fromS, toS, learning.user.company, learning.user.id))[0]?.points
                if (userPoints!= null && userPoints > 0) {
                    knowledgePerUser.put(learning.user, userPoints)

                    if (userPoints > maxPoints) {
                        maxPoints = userPoints
                        usersWithMorePoints = [((Learning) learning).user]
                    } else if (userPoints == maxPoints) {
                        usersWithMorePoints.add(((Learning) learning).user)
                    }
                }
            }
        }

        company.employees.each { employee ->
            log.debug("Preparing report for ${employee} (locale: ${employee.locale})")
            def model = [
                    recipient: employee, company: company, newKnowledge: newKnowledge,
                    usersWithMorePoints: usersWithMorePoints, maxPoints: maxPoints, weekNumber: weekNumber, year: year,
                    from: messageSource.getMessage("default.date.formatted.short", [from] as Object[], employee.locale),
                    to: messageSource.getMessage("default.date.formatted.short", [to] as Object[], employee.locale)
            ]
            emailNotificationService.sendNotification(employee, messageSource.getMessage('knowledge.heads.up.subject', null, employee.locale), 'knowledgeHeadsUp', model)
            log.debug("Report for ${employee} sent")
        }
    }

    def sendMoodWarningReport(company) {
        def today = new Date().clearTime()
        def from = today - 7
        def to = today
        def projects = Project.createCriteria().list {
            eq('company', company)
            eq('active', true)
            eq('deleted', false)
            gt('endDate', from)
            lt('startDate', to)
        }
        projects.each { project ->
            def moods = UserMood.executeQuery(
                    '''select um
from UserMood um join um.user u join u.assignments a
where a.project = :project and a.deleted = false and a.startDate < :to and a.endDate > :from 
order by um.date desc''',
                    [project: project, from: from, to: to])
            def moodPerUser = new HashMap().withDefault { [] }
            moods.each { m ->
                moodPerUser[m.user].add(m.value)
            }
            def moodWarnings = moodPerUser.findAll { it.value.size() >= 2 && (it.value[-1] <= 2 || it.value[-1] < it.value[-2] - 2) }
            if (moodWarnings) {
                def model = [
                        recipient: project.teamLeader, moodWarnings: moodWarnings,
                        from: messageSource.getMessage("default.date.formatted.short", [from] as Object[], project.teamLeader.locale),
                        to: messageSource.getMessage("default.date.formatted.short", [to] as Object[], project.teamLeader.locale)
                ]
                emailNotificationService.sendNotification(project.teamLeader, messageSource.getMessage('mood.heads.up.subject', [project] as Object[], project.teamLeader.locale), 'moodHeadsUp', model)
                log.debug("Report for ${project.teamLeader} sent")
            }
        }
    }
}

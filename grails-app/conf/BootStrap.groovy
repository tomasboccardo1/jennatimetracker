import org.codehaus.groovy.grails.commons.ApplicationAttributes
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.hibernate.event.EventListeners

class BootStrap {

    def springSecurityService
    GrailsApplication grailsApplication

    def init = { servletContext ->

        // Adding SoftDeleteListener to override default onDelete behaviour.
        def ctx = servletContext.getAttribute(ApplicationAttributes.APPLICATION_CONTEXT)
        def sessionFactory = ctx.sessionFactory
        EventListeners eventListeners = sessionFactory.eventListeners
        eventListeners.deleteEventListeners[0] = new SoftDeleteListener()

        System.setProperty('user.language', 'en')
        System.setProperty('user.country', 'US')
        Locale.setDefault(Locale.ENGLISH)

        if (!Permission.findByName(Permission.ROLE_COMPANY_ADMIN)) {
            def companyAdminRole = new Permission(name: Permission.ROLE_COMPANY_ADMIN,
                    description: 'The company administrator permission')
            companyAdminRole.save(flush: true)
        }

        if (!Permission.findByName(Permission.ROLE_SYSTEM_ADMIN)) {
            def systemAdminRole = new Permission(name: Permission.ROLE_SYSTEM_ADMIN,
                    description: 'The administrator permission')
            systemAdminRole.save(flush: true)
        }

        if (!Permission.findByName(Permission.ROLE_PROJECT_LEADER)) {
            def projectLeaderRole = new Permission(name: Permission.ROLE_PROJECT_LEADER,
                    description: 'The project leader permission')
            projectLeaderRole.save(flush: true)
        }

        if (!Permission.findByName(Permission.ROLE_USER)) {
            def userRole = new Permission(name: Permission.ROLE_USER,
                    description: 'The user permission')
            userRole.save(flush: true)
        }


        def clientCategory = TagCategory.findByName(TagCategory.CATEGORY_CLIENT)
        if (!clientCategory) {
            clientCategory = new TagCategory(name: TagCategory.CATEGORY_CLIENT)
            clientCategory.save()
        }
        def projectCategory = TagCategory.findByName(TagCategory.CATEGORY_PROJECT)
        if (!projectCategory) {
            projectCategory = new TagCategory(name: TagCategory.CATEGORY_PROJECT)
            projectCategory.save()
        }
        def taskCategory = TagCategory.findByName(TagCategory.CATEGORY_TASK)
        if (!taskCategory) {
            taskCategory = new TagCategory(name: TagCategory.CATEGORY_TASK)
            taskCategory.save()
        }

        // FIXME: Is there any better way to force Singleton beans to be loaded eagerly?
        grailsApplication.getMainContext().getBean("jabberService")
        fireUpJobs()
        checkForAdminUser()


    }

    /**
     * Fire up the jobs
     * As there's no (easy/proper) way to access the config from the static triggers definition,
     * we have to schedule the Jobs here (and pray not to forget any)
     */

    def fireUpJobs() {

        if (grailsApplication.config.chat.cronExpression)
            ChattingJob.schedule(grailsApplication.config.chat.cronExpression)
        else
            log.info("No cron definition for ChattingJob: chat.cronExpression")

        if (grailsApplication.config.chat.cronExpression)
            InviteCoworkersJob.schedule(grailsApplication.config.chat.cronExpression)
        else
            log.info("No cron definition for InviteCoworkersJob: chat.cronExpression")

        if (grailsApplication.config.badMoodDescriptionHeadsUp.cronExpression)
            BadMoodDescriptionHeadsUpJob.schedule(grailsApplication.config.badMoodDescriptionHeadsUp.cronExpression)
        else

            log.info("No cron definition for BadMoodDescriptionHeadsUpJob: badMoodDescriptionHeadsUp.cronExpression")

        if (grailsApplication.config.projectFollowUp.cronExpression)
            ProjectFollowUpJob.schedule(grailsApplication.config.projectFollowUp.cronExpression)
        else
            log.info("No cron definition for ProjectFollowUpJob: projectFollowUp.cronExpression")

        if (grailsApplication.config.userFollowUp.cronExpression)
            UserFollowUpJob.schedule(grailsApplication.config.userFollowUp.cronExpression)
        else
            log.info("No cron definition for UserFollowUpJob: userFollowUp.cronExpression ")

        if (grailsApplication.config.incompleteEffortsAlarm.cronExpression)
            IncompleteEffortsAlarmJob.schedule(grailsApplication.config.incompleteEffortsAlarm.cronExpression)
        else
            log.info("No cron definition for IncompleteEffortsAlarmJob: incompleteEffortsAlarm.cronExpression")

        if (grailsApplication.config.knowledgeHeadsUp.cronExpression)
            NewKnowledgesHeadUpJob.schedule(grailsApplication.config.knowledgeHeadsUp.cronExpression)
        else
            log.info("No cron definition for NewKnowledgesHeadUpJob: knowledgeHeadsUp.cronExpression")

        if (grailsApplication.config.moodWarningHeadsUp.cronExpression)
            MoodWarningHeadsUpJob.schedule(grailsApplication.config.moodWarningHeadsUp.cronExpression)
        else
            log.info("No cron definition for MoodWarningHeadsUpJob: moodWarningHeadsUp.cronExpression")

        // FIXME: Reminders are not fully-implemented, so we're deactivating this
        //ReminderJob.schedule(grailsApplication.config.chat.cronExpression)

    }



    def checkForAdminUser() {
        log.info("Check for admin user.")


        def admin = User.find("from User u where u.enabled = true and exists (from u.permissions p where p.name = :admin)", ['admin': Permission.ROLE_SYSTEM_ADMIN])
        if (!admin) {

            def systemAdminRole = new Permission(name: Permission.ROLE_SYSTEM_ADMIN,
                    description: 'The administrator permission')

            log.info("*** Creating admin user")

            Company aCompany = new Company()
            aCompany.setName("Default Company")
            aCompany.save()
            aCompany.refresh()

            def testUser = new User(name: "System Administrator", account: 'admin', enabled: true, password: 'j33naAdm1n'
                    , permissions: systemAdminRole, company: aCompany, locale: Locale.getDefault(), timeZone: TimeZone.getDefault())
                    .save(flush: true)

            testUser.save(flush: true);

            log.info("\n***********************************************************" +
                    "\n*** ADMIN LOGIN: user = admin, password = j33naAdm1n\n" +
                    "\n***********************************************************\n\n")

        }
    }
}

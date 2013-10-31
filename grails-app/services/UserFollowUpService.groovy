import de.andreasschmitt.export.ExportService
import org.apache.commons.io.FileUtils
import org.springframework.context.MessageSource
import org.springframework.context.NoSuchMessageException
import reports.Formatters

class UserFollowUpService {

    boolean transactional = false
    MessageSource messageSource
    ExportService exportService
    EmailNotificationService emailNotificationService
    DatabaseService databaseService


    def exportUserFollowUp(format, locale, outputStream, userName, reports) {

        List fields = ['project', 'date', 'comment', 'timeSpent']
        Map labels = [
                'project': getMessage(locale, 'project.name'),
                'date': getMessage(locale, 'effort.date'),
                'comment': getMessage(locale, 'effort.comment'),
                'timeSpent': getMessage(locale, 'effort.timeSpent')
        ]

        Formatters.locale = locale
        Map formatters = [date: Formatters.dateFormatter, timeSpent: Formatters.floatFormatter]
        Map parameters = [title: userName, 'column.widths': [0.1, 0.1, 0.4, 0.1]]
        exportService.export(format, outputStream, reports, fields, labels, formatters, parameters)
    }


    def sendEmailsToFollowers(company) {

        Permission permission = Permission.findByName(Permission.ROLE_COMPANY_ADMIN)
        def maxDate = new Date().clearTime() + 1
        def minDate = maxDate - 7


        User.withTransaction {

            def users = User.executeQuery(
                    '''select u from User u
                    where u.company = :company and u.deleted = false ''',
                    [company: company])

            users.each { User u ->
                if (u.permissions.contains(permission)) {
                    u.usersFollowed.each { User followed ->

                        def reportFile = File.createTempFile('UsersFollowed', '.pdf')
                        def outputStream = new FileOutputStream(reportFile)

                        def report = databaseService.getWeekWorkReport(followed.id).collect {
                            [project: it.getAt('project'), date: it.getAt('date'), comment: it.getAt('comment'), timeSpent: it.getAt('effort')]
                        }

                        if (report) {
                            exportUserFollowUp('pdf', u.locale, outputStream, followed.name, report)

                            def model = [
                                    recipient: u.name,
                                    user: followed,
                                    from: messageSource.getMessage("default.date.formatted.short", [minDate] as Object[], u.locale),
                                    to: messageSource.getMessage("default.date.formatted.short", [maxDate] as Object[], u.locale)
                            ]

                            emailNotificationService.sendNotification(u,
                                    messageSource.getMessage('email.userFollowUp.subject', [followed.name] as Object[], u.locale),
                                    'userFollowUp',
                                    model,
                                    [reportFile])
                        }
                        FileUtils.deleteQuietly(reportFile)
                    }
                }
            }
        }
    }

    String getMessage(Locale _locale, String _msgKey, Object[] _args = null) {
        try {
            return messageSource.getMessage(_msgKey, _args, _locale)
        } catch (NoSuchMessageException ex) {
            return "Missing message: $_msgKey"
        }
    }
}

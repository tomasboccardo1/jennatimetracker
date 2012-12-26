import de.andreasschmitt.export.ExportService
import org.apache.commons.io.FileUtils
import org.springframework.context.MessageSource
import org.springframework.context.NoSuchMessageException
import reports.Formatters

class BadMoodDescriptionHeadsUpService {

    boolean transactional = false
    MessageSource messageSource
    ExportService exportService
    EmailNotificationService emailNotificationService


    def exportBadMood(format, locale, outputStream, reports) {

        List fields = ['user', 'date', 'value', 'comment']
        Map labels = [
                'user': getMessage(locale, 'user.name'),
                'date': getMessage(locale, 'mood.date'),
                'value': getMessage(locale, 'mood.value'),
                'comment': getMessage(locale, 'mood.comment')
        ]

        Formatters.locale = locale
        Map formatters = [date: Formatters.dateFormatter]
        Map parameters = [title: getMessage(locale, 'badMood.heads.up.subject'), 'column.widths': [0.1, 0.1, 0.4, 0.1]]
        exportService.export(format, outputStream, reports, fields, labels, formatters, parameters)
    }

    String getMessage(Locale _locale, String _msgKey, Object[] _args = null) {
        try {
            return messageSource.getMessage(_msgKey, _args, _locale)
        } catch (NoSuchMessageException ex) {
            return "Missing message: $_msgKey"
        }
    }

    def sendMailToAdm(Company company) {

        Permission p = Permission.findByName(Permission.ROLE_SYSTEM_ADMIN)
        def date = new Date().clearTime()

        def users = User.executeQuery(
                '''select u from User u
                    where u.company = :company and u.deleted = false ''',
                [company: company])

        def final badMoods = UserMood.executeQuery(
                '''select distinct uM from UserMood uM
                    where uM.company = :company and uM.deleted = false and uM.value<3 and uM.date= :date''',
                [company: company,date: date])

        badMoods=badMoods.collect{
            [user: it.user.getAt('name'), date: it.getAt('date'), value: it.getAt('value'), comment: it.getAt('comment')]
        }

        if (badMoods) {
            users.each { User adm ->
                if (adm.permissions.contains(p)) {

                    def reportFile = File.createTempFile('badMoodReport', '.pdf')
                    def outputStream = new FileOutputStream(reportFile)

                    exportBadMood('pdf', adm.locale, outputStream, badMoods)
                    def model = [
                            recipient: adm.name,
                            date: messageSource.getMessage("default.date.formatted.short", [date] as Object[], adm.locale)
                    ]

                    emailNotificationService.sendNotification(adm,
                            messageSource.getMessage('badMood.heads.up.subject', [adm.name] as Object[], adm.locale),
                            'badMoodReport',
                            model,
                            [reportFile])
                    FileUtils.deleteQuietly(reportFile)
                }

            }
        }
    }
}


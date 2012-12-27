import org.springframework.context.MessageSource
import org.springframework.context.NoSuchMessageException


class IncompleteEffortsAlarmService {

    boolean transactional = false
    MessageSource messageSource
    EmailNotificationService emailNotificationService
    DatabaseService databaseService


    def sendWarningMessageToUsers(company) {

        def maxDate = new Date().clearTime()
        def minDate = maxDate - 7


        def users = User.executeQuery(
                '''select u from User u
                    where u.company = :company and u.deleted = false ''',
                [company: company])


        users.each { User u ->

            def efforts = Effort.executeQuery(
                    '''select ef from Effort ef
                        where  ef.user= :user and ef.date < :to and ef.date > :from''',
                    [user: u, from: minDate, to: maxDate])

            if (efforts && !efforts.isEmpty()) {

                def moodPerUser = new HashMap().withDefault { [] }
                efforts.each { ef ->
                    moodPerUser[ef.date].add(ef.timeSpent)
                }

                def dailyWorkingHours=u.dailyWorkingHours
                def count=0;

                moodPerUser.each() {ef->
                    ef.each(){
                        count+=it.value
                    }
                }

                def percentage = 0.75
                if (count<dailyWorkingHours*percentage){

                    def model = [
                            recipient: u.name,
                            from: messageSource.getMessage("default.date.formatted.short", [minDate] as Object[], u.locale),
                            to: messageSource.getMessage("default.date.formatted.short", [maxDate] as Object[], u.locale)
                    ]

                    emailNotificationService.sendNotification(u,
                            messageSource.getMessage('email.userFollowUp.subject', [followed.name] as Object[], u.locale),
                            'incompleteEffortsHeadsUp',
                            model,
                            [reportFile])
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

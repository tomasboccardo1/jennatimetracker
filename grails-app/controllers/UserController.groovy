import antlr.StringUtils
import org.apache.commons.lang.RandomStringUtils

class UserController extends BaseController {

    def springSecurityService
    def emailerService
    def jabberService
    DatabaseService databaseService

    def index = { redirect(action: "list", params: params) }

    def beforeInterceptor = [action: this.&auth]

    def auth() {
        try {
            findLoggedUser()
            return true
        } catch (Exception e) {
            redirect(controller: 'login', action: 'auth')
            return false
        }
    }

    // the delete, save and update actions only accept POST requests
    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def list = {
        def user = findLoggedUser()
        def userInstanceTotal = User.countByCompany(user.company)
        def userInstanceList = User.withCriteria {
            eq("company", user.company)
            order(params.get('sort', 'name'), params.get('order', 'asc'))
        }
        [userInstanceList: userInstanceList, userInstanceTotal: userInstanceList.size(),currentUser: user]
    }

    def create = {
        def userInstance = new User()
        userInstance.properties = params
        return [userInstance: userInstance]
    }

    def save = {
        def userInstance = new User(params)
        def User currentUser = findLoggedUser()
        userInstance.setCompany(currentUser.getCompany())
        userInstance.setEnabled(Boolean.TRUE)
        userInstance.setLocale(currentUser.getLocale())
        userInstance.setTimeZone(currentUser.getTimeZone())
        def pass = RandomStringUtils.randomAlphanumeric(8);
        userInstance.setPassword(pass)
        userInstance.chatTime = currentUser.chatTime
        userInstance.humour = "sweet"
        userInstance.localChatTime = TimeZoneUtil.toSystemTime(userInstance.chatTime, userInstance.timeZone)
        userInstance.dailyWorkingHours=8
        Company company = Company.findById(currentUser.getCompany().getId())

        if (!userInstance.hasErrors() && userInstance.save()) {

            // chatService
            def permissions = []
            permissions << Permission.findByName(Permission.ROLE_USER);
            company.addToEmployees(userInstance)
            company.save()
            permissions.each { permission ->
                permission.addToUsers(userInstance)
            }

            try {
                jabberService.addAccount(userInstance.account, userInstance.name)
            } catch (Exception e) {
                log.error(e.getMessage(),e)
            }
            flash.message = "user.created"
            flash.args = [userInstance.id]
            flash.defaultMessage = "User ${userInstance.id} created"
            sendNotificationEmailToNewUser(userInstance, pass)
            redirect(action: "show", id: userInstance.id)
        } else {
            render(view: "create", model: [userInstance: userInstance])
        }
    }

    def show = {
        def userInstance = User.get(params.id)
        if (!userInstance) {
            flash.message = "user.not.found"
            flash.args = [params.id]
            flash.defaultMessage = "User not found with id ${params.id}"
            redirect(action: "list")
        } else {
            return [userInstance: userInstance]
        }
    }


    def showReports = {

        def User currentUser = findLoggedUser()
        def user = User.get(params.id)

        //Si ya no lo estaba siguiendo lo agrego a la lista
        if (!currentUser.usersFollowed.contains(user)){
            currentUser.addToUsersFollowed(user)
            currentUser.save()
        }

        def moodWeekReport = databaseService.getMoodReport(user.id).collect {
            [date: it.getAt('date'), moodValue: it.getAt('value')]
        }
        def workWeekReport = databaseService.getWeekWorkReport(user.id).collect {
            [date: it.getAt('date'), timeSpent: it.getAt('effort'), project: it.getAt('project'), comment: it.getAt('comment')]
        }
        def knowledgeWeekReport = databaseService.getKnowledge(user.id).collect {
            [date: it.getAt('date'), knowledge: it.getAt('knowledge')]
        }
        def votedKnowledgeWeekReport = databaseService.getVotedKnowledge(user.id).collect {
            [date: it.getAt('date'), knowledge: it.getAt('gettedKnowledge'), user: it.getAt("votedUserName")]
        }

        return [user: user, moodReport: moodWeekReport, workReport: workWeekReport, knowledgeReport: knowledgeWeekReport, votedKnowledge: votedKnowledgeWeekReport]
    }

    def unFollow = {

        def User currentUser = findLoggedUser()
        def user = User.get(params.id)
        if (currentUser.usersFollowed.contains(user)){
            currentUser.removeFromUsersFollowed(user)
            currentUser.save()
        }
        flash.message = "user.unfollowMessage"
        flash.args = [user.name]
        redirect(action: "list")
    }


    def edit = {
        def userInstance = User.get(params.id)
        if (!userInstance) {
            flash.message = "user.not.found"
            flash.args = [params.id]
            flash.defaultMessage = "User not found with id ${params.id}"
            redirect(action: "list")
        } else {
            return [userInstance: userInstance, availablesChatTime: TimeZoneUtil.getAvailablePromptTimes(), timeZones: TimeZoneUtil.getAvailableTimeZones(), locales: LocaleUtil.getAvailableLocales(), skills: Skill.list()]
        }
    }

    def update = {
        def userInstance = User.get(params.id)
        if (userInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (userInstance.version > version) {

                    userInstance.errors.rejectValue("version", "user.optimistic.locking.failure", "Another user has updated this User while you were editing")
                    render(view: "edit", model: [userInstance: userInstance])
                    return
                }
            }
            userInstance.properties = params
            userInstance.localChatTime = TimeZoneUtil.toSystemTime(userInstance.chatTime, userInstance.timeZone)
            if (!userInstance.hasErrors() && userInstance.save()) {
                flash.message = "user.updated"
                flash.args = [params.id]
                flash.defaultMessage = "User ${params.id} updated"
                redirect(action: "show", id: userInstance.id)
            } else {
                render(view: "edit", model: [userInstance: userInstance])
            }
        } else {
            flash.message = "user.not.found"
            flash.args = [params.id]
            flash.defaultMessage = "User not found with id ${params.id}"
            redirect(action: "edit", id: params.id)
        }
    }

    def delete = {
        def userInstance = User.get(params.id)
        if (userInstance) {
            try {
                userInstance.deleted = true
                userInstance.save(flush: true)
                flash.message = "user.deleted"
                flash.args = [params.id]
                flash.defaultMessage = "User ${params.id} deleted"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "user.not.deleted"
                flash.args = [params.id]
                flash.defaultMessage = "User ${params.id} could not be deleted"
                redirect(action: "show", id: params.id)
            }
        } else {
            flash.message = "user.not.found"
            flash.args = [params.id]
            flash.defaultMessage = "User not found with id ${params.id}"
            redirect(action: "list")
        }
    }

    void sendNotificationEmailToNewUser(User user, String password) {
        User currentUser = findLoggedUser()
        def email = [
                to: [user.account],
                subject: g.message(code: 'email.account.created'),
                from: g.message(code: 'application.email'),
                text: g.message(code: 'email.account.created.body', args: [user.account, currentUser.name, currentUser.company, password])
        ]
        emailerService.sendEmails([email])

    }

}

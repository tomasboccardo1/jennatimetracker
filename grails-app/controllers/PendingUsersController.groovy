class PendingUsersController extends BaseController {

    def index = { redirect(action: "list", params: params) }
    def emailerService
    def springSecurityService

    static Map allowedMethods = [invite: 'POST', dismiss: 'POST']

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

    def list = {

        User currentUser = findLoggedUser()

        def requestedInvitationsList = InviteMe.findAllByCompany(currentUser.company)
        return [requestedInvitations: requestedInvitationsList]
    }

    def invite = {


        InviteMe inviteMe = InviteMe.get(params.inviteId)

        if (inviteMe) {

            List<User> userList = OAuthID.executeQuery("select oa.user from OAuthID oa where oa.user.account = ?", [inviteMe.email])

            inviteMe.invited = new Date()
            inviteMe.save(flush: true)
            //If an user was registered via oauth provider we do not send a verification email.
            if (userList.size() == 0) {
                try {
                    sendInvitation(inviteMe)
                    flash.message = g.message(code: "invitation.sent", args: [inviteMe.name])
                } catch (Exception e) {
                    log.error("Problem sending invitation", e)
                    flash.message = g.message(code: "invitation.not.sent", args: [inviteMe.name])
                }
            } else {
                flash.message = g.message(code: "invitation.accepted", args: [inviteMe.email])
                def user = userList.get(0)
                user.enabled = true;
                user.save(flush: true);
            }
        }

        User currentUser = findLoggedUser()
        def requestedInvitationsList = InviteMe.findAllByCompany(currentUser.company)
        render(view: "list", model: [requestedInvitations: requestedInvitationsList])
    }

    def dismiss = {
        User currentUser = findLoggedUser()

        InviteMe inviteMe = InviteMe.get(params.dismissId)
        if (inviteMe) {
            inviteMe.delete(flush: true)
            flash.message = g.message(code: "invitation.request.dismissed", args: [inviteMe.name])
        } else {
            flash.message = g.message(code: "invitation.request.not.dismissed", args: [inviteMe.name])
        }

        def requestedInvitationsList = InviteMe.findAllByCompany(currentUser.company)
        render(view: "list", model: [requestedInvitations: requestedInvitationsList])

    }

    void sendInvitation(InviteMe inviteMe) {
        Invitation invitation = new Invitation()
        invitation.inviter = findLoggedUser()
        invitation.invitee = inviteMe.email
        invitation.invited = new Date()
        invitation.code = springSecurityService.encodePassword(String.valueOf(System.currentTimeMillis()) + invitation.invitee)
        invitation.validate()

        if (!invitation.hasErrors()) {
            invitation.save()
            def email = [
                    to: [invitation.invitee],
                    subject: g.message(code: 'invitation.mail.subject'),
                    from: g.message(code: 'application.email'),
                    text: g.message(code: 'invitation.mail.body', args: [createLink(absolute: true, controller: "register", action: "acceptInvitation", params: [code: invitation.code])])
            ]
            emailerService.sendEmails([email])
        }

    }
}

/*
 * Copyright 2012 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import OAuthID
import Permission
import User
import grails.plugin.springsecurity.oauth.OAuthToken
import oauth.Provider
import oauth.ProviderFactory
import org.codehaus.groovy.grails.plugins.springsecurity.GormUserDetailsService
import org.codehaus.groovy.grails.plugins.springsecurity.GrailsUser
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.springframework.security.core.authority.GrantedAuthorityImpl
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.savedrequest.DefaultSavedRequest

/**
 * Simple helper controller for handling OAuth authentication and integrating it
 * into Spring Security.
 */
class SpringSecurityOAuthController {

    public static final String SPRING_SECURITY_OAUTH_TOKEN = 'springSecurityOAuthToken'

    def grailsApplication
    def oauthService
    def springSecurityService
    def emailerService
    def jabberService

    def onSuccess = {
        /* Validate the 'provider' URL. Any errors here are either misconfiguration
           or web crawlers (or malicious users).*/

        if (!params.provider) {
            renderError 400, "The Spring Security OAuth callback URL must include the 'provider' URL parameter."
            return
        }

        def sessionKey = oauthService.findSessionKeyForAccessToken(params.provider)
        if (!session[sessionKey]) {
            renderError 500, "No OAuth token in the session for provider '${params.provider}'!"
            return
        }

        // Create the relevant authentication token and attempt to log in.
        OAuthToken oAuthToken = createAuthToken(params.provider, session[sessionKey])

        def user
        if (oAuthToken.principal instanceof GrailsUser) {
            //User create by Oauth login
            user = User.findByAccount(oAuthToken.principal.username)
        } else {
            //Existing domain user or not existing user coming from oauth connect
            user = User.findByAccount(oAuthToken.principal);
            //If is and existing user of domain we update the oauthToken and redirect to login then.
            if (user)
                oAuthToken = updateOAuthToken(oAuthToken, user)
        }

        if (user) {
            if (user.enabled)
                authenticateAndRedirect(oAuthToken, defaultTargetUrl)
            else
                flash.message = "registration.email.sent"
            render view: 'askToLinkOrCreateAccount', model: [account: user.account]
        } else {
            /* This OAuth account hasn't been registered against an internal
            / account yet. Give the oAuthID the opportunity to create a new
            / internal account or link to an existing one.*/
            session[SPRING_SECURITY_OAUTH_TOKEN] = oAuthToken
            def token = session[oauthService.findSessionKeyForAccessToken(params.provider)]
            ProviderFactory providerFactory = new ProviderFactory()
            Provider provider = providerFactory.makeProvider(params.provider, token)
            def email = provider.getEmail();
            session.setAttribute("provider", provider)

            def redirectUrl = SpringSecurityUtils.securityConfig.oauth.registration.askToLinkOrCreateAccountUri
            assert redirectUrl, "grails.plugins.springsecurity.oauth.registration.askToLinkOrCreateAccountUri" +
                    " configuration option must be set!"
            log.debug "Redirecting to askToLinkOrCreateAccountUri: ${redirectUrl}"
            render view: 'askToLinkOrCreateAccount', model: [account: email]
        }
    }

    def onFailure = {
        authenticateAndRedirect(null, defaultTargetUrl)
    }

    /**
     * Associates an OAuthID with an existing account. Needs the user's password to ensure
     * that the user owns that account, and authenticates to verify before linking.
     */
    def linkAccount = { OAuthLinkAccountCommand command ->

        OAuthToken oAuthToken = session[SPRING_SECURITY_OAUTH_TOKEN]
        assert oAuthToken, "There is no auth token in the session!"

        if (request.post) {
            boolean linked = command.validate() && User.withTransaction { status ->
                User user = User.findByAccountAndPassword(
                        command.account, springSecurityService.encodePassword(command.password))
                if (user) {
                    user.addToOAuthIDs(provider: oAuthToken.providerName, accessToken: oAuthToken.socialId, user: user)
                    if (user.validate() && user.save()) {
                        oAuthToken = updateOAuthToken(oAuthToken, user)
                        return true
                    }
                } else {
                    command.errors.rejectValue("account", "OAuthLinkAccountCommand.account.not.exists")
                }

                status.setRollbackOnly()
                return false
            }

            if (linked) {
                authenticateAndRedirect(oAuthToken, defaultTargetUrl)
                return
            }
        }

        render view: 'askToLinkOrCreateAccount', model: [linkAccountCommand: command]
        return
    }

    def createAccount = { OAuthCreateAccountCommand command ->
        OAuthToken oAuthToken = session[SPRING_SECURITY_OAUTH_TOKEN]
        assert oAuthToken, "There is no auth token in the session!"

        def provider = session.getAttribute("provider")
        def email = provider.getEmail();
        def name = provider.getName()
        def locale = provider.getLocale() == null ? "es" : provider.getLocale()

        if (request.post) {
            if (!springSecurityService.loggedIn) {

                def company = Company.findByName(command.company)

                boolean created = command.validate() && User.withTransaction { status ->

                    User user = new User(
                            account: email,
                            password: command.password1,
                            name: name,
                            company: company,
                            chatTime: command.chatTime,
                            timeZone: command.timeZone,
                            locale: new Locale(locale),
                            enabled: false)

                    user.addToOAuthIDs(provider: oAuthToken.providerName, accessToken: oAuthToken.socialId, user: user)

                    if (!user.validate() || !user.save()) {
                        status.setRollbackOnly()
                        return false
                    }

                    def userRole = Permission.findByName(Permission.ROLE_USER);
                    def permissions = []
                    permissions << userRole

                    permissions.each { permission ->
                        permission.addToUsers(user)
                    }

                    //Add user account to chattingJob
                    jabberService.addAccount(user.account, user.name)
                    oAuthToken = updateOAuthToken(oAuthToken, user)
                    return true
                }

                if (created) {
                    InviteMe inviteMe = InviteMe.findByEmailAndCompany(email, company)
                    if (inviteMe == null) {
                        inviteMe = new InviteMe()
                        inviteMe.name = name
                        inviteMe.email = email
                        inviteMe.requested = new Date()
                        inviteMe.company = company
                        inviteMe.save()
                    }

                    rememberCompanyOwnerPendingInvitations(company)
                    flash.message = "registration.email.sent"
                    flash.args = [email]
                }
            }
        }
        render view: 'askToLinkOrCreateAccount', model: [createAccountCommand: command, account: email]
    }

    List findCompanyOwners(Company company) {
        def owners = User.createCriteria().list {
            eq('company', company)
            permissions {
                eq('name', Permission.ROLE_COMPANY_ADMIN)
            }
        }
        return owners
    }

    protected renderError(code, msg) {
        log.error msg + " (returning ${code})"
        render status: code, text: msg
    }

    protected OAuthToken createAuthToken(providerName, scribeToken) {
        def providerService = grailsApplication.mainContext.getBean("${providerName}SpringSecurityOAuthService")
        OAuthToken oAuthToken = providerService.createAuthToken(scribeToken)

        def oAuthID = OAuthID.findByProviderAndAccessToken(oAuthToken.providerName, oAuthToken.socialId)
        if (oAuthID) {
            updateOAuthToken(oAuthToken, oAuthID.user)
        }

        return oAuthToken
    }

    void rememberCompanyOwnerPendingInvitations(Company company) {

        def ownersList = findCompanyOwners(company)

        ownersList.each { User owner ->
            def email = [
                    to: [owner.account],
                    subject: g.message(code: 'invitation.requested.subject'),
                    from: g.message(code: 'application.email'),
                    text: g.message(code: 'invitation.requested.body')
            ]
            emailerService.sendEmails([email])
        }
    }

    protected OAuthToken updateOAuthToken(OAuthToken oAuthToken, User user) {
        def conf = SpringSecurityUtils.securityConfig

        // user
        String usernamePropertyName = conf.userLookup.usernamePropertyName
        String passwordPropertyName = conf.userLookup.passwordPropertyName
        String enabledPropertyName = conf.userLookup.enabledPropertyName
        String accountExpiredPropertyName = conf.userLookup.accountExpiredPropertyName
        String accountLockedPropertyName = conf.userLookup.accountLockedPropertyName
        String passwordExpiredPropertyName = conf.userLookup.passwordExpiredPropertyName

        String username = user."${usernamePropertyName}"
        String password = user."${passwordPropertyName}"
        boolean enabled = enabledPropertyName ? user."${enabledPropertyName}" : true
        boolean accountExpired = accountExpiredPropertyName ? user."${accountExpiredPropertyName}" : false
        boolean accountLocked = accountLockedPropertyName ? user."${accountLockedPropertyName}" : false
        boolean passwordExpired = passwordExpiredPropertyName ? user."${passwordExpiredPropertyName}" : false

        // authorities
        String authoritiesPropertyName = conf.userLookup.authoritiesPropertyName
        String authorityPropertyName = conf.authority.nameField
        Collection<?> userAuthorities = user."${authoritiesPropertyName}"
        def authorities = userAuthorities.collect { new GrantedAuthorityImpl(it."${authorityPropertyName}") }

        oAuthToken.principal = new GrailsUser(username, password, enabled, !accountExpired, !passwordExpired,
                !accountLocked, authorities ?: GormUserDetailsService.NO_ROLES, user.id)
        oAuthToken.authorities = authorities
        oAuthToken.authenticated = true

        return oAuthToken
    }


    protected Map getDefaultTargetUrl() {
        def config = SpringSecurityUtils.securityConfig
        def savedRequest = session[DefaultSavedRequest.SPRING_SECURITY_SAVED_REQUEST_KEY]
        def defaultUrlOnNull = grailsApplication.config.grails.serverURL
        if (savedRequest && !config.successHandler.alwaysUseDefault) {
            return [url: (savedRequest.redirectUrl ?: defaultUrlOnNull)]
        } else {
            return [uri: (config.successHandler.defaultTargetUrl ?: defaultUrlOnNull)]
        }
    }

    protected void authenticateAndRedirect(OAuthToken oAuthToken, redirectUrl) {
        session.removeAttribute SPRING_SECURITY_OAUTH_TOKEN

        SecurityContextHolder.context.authentication = oAuthToken
        redirect(redirectUrl instanceof Map ? redirectUrl : [uri: redirectUrl])
    }

}

class OAuthCreateAccountCommand {

    String password1
    String password2
    String company
    String chatTime
    TimeZone timeZone

    static constraints = {

        password1(nullable: false, blank: false, size: 8..32)
        password2 nullable: false, blank: false, validator: { password2, command ->
            if (command.password1 != password2) {
                return 'OAuthCreateAccountCommand.password.error.mismatch'
            }
        }
    }
}

class OAuthLinkAccountCommand {

    String account
    String password

    static constraints = {
        password(nullable: false, blank: false)
        account blank: false
    }

}

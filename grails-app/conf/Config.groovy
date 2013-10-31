import grails.util.GrailsUtil

grails.config.locations = [
        customconf.CustomDataSource, // Environment-specific DB config
        customconf.CustomJabberBot, // How the bot connects
        customconf.CustomEmailNotificationsConfig, // connection credentials and config for email sending
        customconf.CustomSearchable
]

grails.plugin.databasemigration.updateOnStart = true
grails.plugin.databasemigration.updateOnStartFileNames = ['changelog.groovy']

grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [
        html: ['text/html', 'application/xhtml+xml'],
        xml: ['text/xml', 'application/xml'],
        text: 'text/plain',
        js: 'text/javascript',
        rss: 'application/rss+xml',
        atom: 'application/atom+xml',
        css: 'text/css',
        csv: 'text/csv',
        pdf: 'application/pdf',
        rtf: 'application/rtf',
        excel: 'application/vnd.ms-excel',
        ods: 'application/vnd.oasis.opendocument.spreadsheet',
        all: '*/*',
        json: ['application/json', 'text/json'],
        form: 'application/x-www-form-urlencoded',
        multipartForm: 'multipart/form-data'
]
                                         /** Password Encoder
 The digest algorithm to use.
 Supports the named Message Digest Algorithms in the Java environment.
 http://java.sun.com/j2se/1.4.2/docs/guide/security/CryptoSpec.html#AppA
 */

// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"

// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true

grails.dbconsole.enabled = false
grails.dbconsole.urlRoot = '/admin/dbconsole'

// Disabling bundling and minified resources for development
environments {
    development {
        grails.resources.debug = true
    }
}

jenna.availableHumours = ['sweet', 'angry']
jenna.availableLanguages = ['en', 'es']
jenna.defaultHumour = 'sweet'
jenna.defaultLanguage = 'en'
jenna.authorizations = [
        'CreateProjectRequestHandler': [Permission.ROLE_SYSTEM_ADMIN, Permission.ROLE_PROJECT_LEADER],
        'CreateAssignmentRequestHandler': [Permission.ROLE_SYSTEM_ADMIN, Permission.ROLE_PROJECT_LEADER],
        'ActiveAssignmentsRequestHandler': [Permission.ROLE_USER]
]

// Avatars: gravatar.com
avatarPlugin {
    gravatarRating = "G"
}

// log4j configuration
log4j = { root ->

    root.level = org.apache.log4j.Level.WARN

    appenders {
        'null' name: "stacktrace" // Shutting down the StackTrace logger
        //console name: 'stdout', layout: pattern(conversionPattern: '%d{dd MMM yyyy HH:mm:ss,SSS} [%15.15t] %-5p %30.30c %x - %m%n')
        rollingFile name: 'file', maxFileSize: 1024 * 1024 * 1024, maxBackupIndex: 10, file: '/tmp/jenna.log', layout: pattern(conversionPattern: '%d{dd MMM yyyy HH:mm:ss,SSS} [%15.15t] %-5p %30.30c %x - %m%n')
    }

    error file: 'org.codehaus.groovy.grails.web.servlet',  //  controllers
            'org.codehaus.groovy.grails.web.pages', //  GSP
            'org.codehaus.groovy.grails.web.sitemesh', //  layouts
            'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
            'org.codehaus.groovy.grails.web.mapping', // URL mapping
            'org.codehaus.groovy.grails.commons', // core / classloading
            'org.codehaus.groovy.grails.plugins', // plugins
            'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
            'org.springframework',
            'org.hibernate',
            'org.apache.commons.digester'
    warn file: 'org.mortbay.log'
    info file: 'grails.app'
    info file: 'log4j.logger.org.springframework.security'
}


environments {
    production {
        // conversations expire in 2 minutes
        conversationExpiracy = 1000 * 60 * 2
        chat.cronExpression = '0 0 0-23 ? * *'
        reminder.cronExpression = '0 0 * ? * *'
        userFollowUp.cronExpression = '0 0 9 ? * MON'
        projectFollowUp.cronExpression = '0 0 9 ? * FRI'
        knowledgeHeadsUp.cronExpression = '0 0 9 ? * MON'
        moodWarningHeadsUp.cronExpression = '0 0 9 ? * MON-FRI'
    }
}



grails.plugins.springsecurity.active = true

grails.plugins.springsecurity.userLookup.userDomainClassName = 'User'
grails.plugins.springsecurity.userLookup.usernamePropertyName = 'account'
grails.plugins.springsecurity.userLookup.passwordPropertyName = 'password'
grails.plugins.springsecurity.userLookup.enabledPropertyName = 'enabled'
grails.plugins.springsecurity.userLookup.authoritiesPropertyName = 'permissions'

grails.plugins.springsecurity.authority.className = 'Permission'
grails.plugins.springsecurity.authority.nameField = 'name'

/** Authentication Processing Filter */
grails.plugins.springsecurity.failureHandler.defaultFailureUrl = '/login/authfail?login_error=1'
grails.plugins.springsecurity.failureHandler.ajaxAuthFailUrl = '/login/authfail?ajax=true'
grails.plugins.springsecurity.successHandler.defaultTargetUrl = '/'
grails.plugins.springsecurity.successHandler.alwaysUseDefault = false
grails.plugins.springsecurity.apf.filterProcessesUrl = '/j_spring_security_check'

/** Anonymous Processing Filter */
grails.plugins.springsecurity.anon.key = 'foo'
grails.plugins.springsecurity.anon.userAttribute = 'anonymousUser,ROLE_ANONYMOUS'

/** authenticationEntryPoint */
grails.plugins.springsecurity.auth.loginFormUrl = '/login/auth'
grails.plugins.springsecurity.auth.forceHttps = 'false'
grails.plugins.springsecurity.auth.ajaxLoginFormUrl = '/login/authAjax'

/** Logout Filter */
grails.plugins.springsecurity.logout.afterLogoutUrl = '/'

/** AccessDeniedHandler
 *  Set errorPage to null, if you want to get error code 403 (FORBIDDEN).
 */
grails.plugins.springsecurity.adh.errorPage = '/login/denied'
grails.plugins.springsecurity.adh.ajaxErrorPage = '/login/deniedAjax'
grails.plugins.springsecurity.ajaxHeader = 'X-Requested-With'


grails.plugins.springsecurity.password.algorithm = 'MD5'
//Use Base64 text ( true or false )
grails.plugins.springsecurity.password.encodeHashAsBase64 = false

/** RememberMe Services */
grails.plugins.springsecurity.rememberMe.cookieName = 'jenna_remember_me'
grails.plugins.springsecurity.rememberMe.alwaysRemember = false
grails.plugins.springsecurity.rememberMe.tokenValiditySeconds = 1209600 //14 days
grails.plugins.springsecurity.rememberMe.parameter = '_spring_security_remember_me'
grails.plugins.springsecurity.rememberMe.key = 'jennaRocks'

/** LoggerListener
 * ( add 'log4j.logger.org.springframework.security=info,stdout'
 * to log4j.*.properties to see logs )
 */
grails.plugins.springsecurity.registerLoggerListener = true

/** Use annotations from Controllers to define security rules */
grails.plugins.springsecurity.controllerAnnotations.matcher = 'ant'
grails.plugins.springsecurity.controllerAnnotations.lowercase = true
grails.plugins.springsecurity.controllerAnnotations.staticRules = [:]
grails.plugins.springsecurity.rejectIfNoRule = false

grails.plugins.springsecurity.securityConfigType = "InterceptUrlMap"
grails.plugins.springsecurity.basic.realmName = 'Jenna Realm'
grails.plugins.springsecurity.useBasicAuth = false;

grails.plugins.springsecurity.interceptUrlMap = [
        '/login/**': ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/admin/**': [Permission.ROLE_SYSTEM_ADMIN],
        '/quartz/**': [Permission.ROLE_SYSTEM_ADMIN],
        '/effort/**': [Permission.ROLE_USER, Permission.ROLE_PROJECT_LEADER, Permission.ROLE_COMPANY_ADMIN, Permission.ROLE_SYSTEM_ADMIN],
        '/project/**': [Permission.ROLE_PROJECT_LEADER],
        '/assignment/**': [Permission.ROLE_PROJECT_LEADER],
        '/permission/**': [Permission.ROLE_PROJECT_LEADER],
        '/tags/**': [Permission.ROLE_PROJECT_LEADER],
        '/chart/**': [Permission.ROLE_PROJECT_LEADER],
        '/report/**': [Permission.ROLE_PROJECT_LEADER, Permission.ROLE_COMPANY_ADMIN, Permission.ROLE_SYSTEM_ADMIN],
        '/company/**': [Permission.ROLE_SYSTEM_ADMIN],
        '/user/**': [Permission.ROLE_USER, Permission.ROLE_PROJECT_LEADER, Permission.ROLE_COMPANY_ADMIN, Permission.ROLE_SYSTEM_ADMIN],
        '/**': ['IS_AUTHENTICATED_ANONYMOUSLY']
]

/** use switchUserProcessingFilter */
grails.plugins.springsecurity.useSwitchUserFilter = false
grails.plugins.springsecurity.switchUser.switchUserUrl = '/j_spring_security_switch_user'
grails.plugins.springsecurity.switchUser.exitUserUrl = '/j_spring_security_exit_user'
grails.plugins.springsecurity.switchUser.targetUrl = '/'

/*
use email notification while registration and report sending FIXME?
useMail = ConfigurationHolder.config['mailSender']['useMail']
mailHost = ConfigurationHolder.config['mailSender']['mailHost']
mailPort = ConfigurationHolder.config['mailSender']['mailPort']
mailUsername = ConfigurationHolder.config['mailSender']['mailUsername']
mailPassword = ConfigurationHolder.config['mailSender']['mailPassword']
mailProtocol = ConfigurationHolder.config['mailSender']['mailProtocol']
mailFrom = ConfigurationHolder.config['mailSender']['mailFrom']
javaMailProperties = ConfigurationHolder.config['mailSender']['javaMailProperties']
*/

// HttpSessionEventPublisher
grails.plugins.springsecurity.useHttpSessionEventPublisher = false;
// User caching
grails.plugins.springsecurity.cacheUsers = !GrailsUtil.isDevelopmentEnv()

// Port mappings
grails.plugins.springsecurity.portMapper.httpPort = 8080
grails.plugins.springsecurity.portMapper.httpsPort = 8443

// Secure channel filter (http/https)
//grails.plugins.springsecurity.secureChannel.definition = ''
grails.plugins.springsecurity.secureChannel.definition = [
        '/**':        'ANY_CHANNEL'
]
// Ip restriction filter
grails.plugins.springsecurity.ipRestrictions = [:]







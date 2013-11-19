import grails.util.GrailsUtil
import org.scribe.builder.api.FacebookApi
import org.scribe.builder.api.GoogleApi



grails.config.locations = [
        customconf.CustomDataSource,
        customconf.CustomJabberBot,
        customconf.CustomEmailNotificationsConfig,
        customconf.CustomSearchable
]

environments {
    production {
        grails.serverURL = "http://apps.fdvlabs.com/projectguide"
    }
    development {
        grails.serverURL = "http://localhost:8080/projectguide"
    }
    testing {
        grails.serverURL= "http://192.168.1.31:8080/projectguide"
    }
}

grails {
    plugin {
        databasemigration.updateOnStart = true
        databasemigration.updateOnStartFileNames = ['changelog.groovy']
    }
}

grails {
    mime {
        // Enables the parsing of file extensions from URLs into the request format
        file.extensions = true
        use.accept.header = false
        types = [
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
    }
}

// Disabling bundling and minified resources for development and testing environments
environments {
    development {
        grails.resources.debug = true
    }
    testing{
        grails.resources.debug =true
    }
}

// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"

// Enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
grails.dbconsole.enabled = false
grails.dbconsole.urlRoot = '/admin/dbconsole'

jenna {
    availableHumours = ['sweet', 'angry']
    availableLanguages = ['en', 'es']
    defaultHumour = 'sweet'
    defaultLanguage = 'en'
    authorizations = [
            'CreateProjectRequestHandler': [Permission.ROLE_SYSTEM_ADMIN, Permission.ROLE_PROJECT_LEADER],
            'CreateAssignmentRequestHandler': [Permission.ROLE_SYSTEM_ADMIN, Permission.ROLE_PROJECT_LEADER],
            'ActiveAssignmentsRequestHandler': [Permission.ROLE_USER]
    ]
}

avatarPlugin {
    gravatarRating = "G"
}

log4j = { root ->

    root.level = org.apache.log4j.Level.WARN

    appenders {
        // Shutting down the StackTrace logger
        'null' name: "stacktrace"
        //console name: 'stdout', layout: pattern(conversionPattern: '%d{dd MMM yyyy HH:mm:ss,SSS} [%15.15t] %-5p %30.30c %x - %m%n')
        rollingFile name: 'file',
                maxFileSize: 1024 * 1024 * 1024,
                maxBackupIndex: 10,
                file: '/tmp/jenna.log',
                layout: pattern(conversionPattern: '%d{dd MMM yyyy HH:mm:ss,SSS} [%15.15t] %-5p %30.30c %x - %m%n')
    }

    error file: 'org.codehaus.groovy.grails.web.servlet',  //  Controllers
            'org.codehaus.groovy.grails.web.pages', //  GSP
            'org.codehaus.groovy.grails.web.sitemesh', //  Layouts
            'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
            'org.codehaus.groovy.grails.web.mapping', // URL mapping
            'org.codehaus.groovy.grails.commons', // Core / classloading
            'org.codehaus.groovy.grails.plugins', // Plugins
            'org.codehaus.groovy.grails.orm.hibernate', // Hibernate integration
            'org.springframework',
            'org.hibernate',
            'org.apache.commons.digester'
    warn file: 'org.mortbay.log'
    info file: 'grails.app'
    info file: 'log4j.logger.org.springframework.security'
}

environments {
    production {
        conversationExpiracy = 1000 * 60 * 2
        chat.cronExpression = '0 0 0-23 ? * *'
        reminder.cronExpression = '0 0 * ? * *'
        userFollowUp.cronExpression = '0 0 9 ? * MON'
        projectFollowUp.cronExpression = '0 0 9 ? * FRI'
        knowledgeHeadsUp.cronExpression = '0 0 9 ? * MON'
        moodWarningHeadsUp.cronExpression = '0 0 9 ? * MON-FRI'
    }
}

grails {
    plugins {
        springsecurity {

            active = true
            userLookup.userDomainClassName = 'User'
            userLookup.usernamePropertyName = 'account'
            userLookup.passwordPropertyName = 'password'
            userLookup.enabledPropertyName = 'enabled'
            userLookup.authoritiesPropertyName = 'permissions'
            authority.className = 'Permission'
            authority.nameField = 'name'

            /** Authentication Processing Filter */
            failureHandler.defaultFailureUrl = '/login/authfail?login_error=1'
            failureHandler.ajaxAuthFailUrl = '/login/authfail?ajax=true'
            successHandler.defaultTargetUrl = '/'
            successHandler.alwaysUseDefault = false
            apf.filterProcessesUrl = '/j_spring_security_check'

            /** Anonymous Processing Filter */
            anon.key = 'foo'
            anon.userAttribute = 'anonymousUser,ROLE_ANONYMOUS'

            /** authenticationEntryPoint */
            auth.loginFormUrl = '/login/auth'
            auth.forceHttps = 'false'
            auth.ajaxLoginFormUrl = '/login/authAjax'

            /** Logout Filter */
            logout.afterLogoutUrl = '/'

            /** AccessDeniedHandler
             *  Set errorPage to null, if you want to get error code 403 (FORBIDDEN).
             */
            adh.errorPage = '/login/denied'
            adh.ajaxErrorPage = '/login/deniedAjax'
            ajaxHeader = 'X-Requested-With'
            password.algorithm = 'MD5'
            //Use Base64 text ( true or false )
            password.encodeHashAsBase64 = false

            /** RememberMe Services */
            rememberMe.cookieName = 'jenna_remember_me'
            rememberMe.alwaysRemember = false
            rememberMe.tokenValiditySeconds = 1209600 //14 days
            rememberMe.parameter = '_spring_security_remember_me'
            rememberMe.key = 'jennaRocks'

            /** LoggerListener
             * ( add 'log4j.logger.org.springframework.security=info,stdout'
             * to log4j.*.properties to see logs )
             */
            registerLoggerListener = true

            /** Use annotations from Controllers to define security rules */
            controllerAnnotations.matcher = 'ant'
            controllerAnnotations.lowercase = true
            controllerAnnotations.staticRules = [:]
            rejectIfNoRule = false

            securityConfigType = "InterceptUrlMap"
            basic.realmName = 'Jenna Realm'
            useBasicAuth = false;

            interceptUrlMap = [
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
            useSwitchUserFilter = false
            switchUser.switchUserUrl = '/j_spring_security_switch_user'
            switchUser.exitUserUrl = '/j_spring_security_exit_user'
            switchUser.targetUrl = '/'

            // HttpSessionEventPublisher
            useHttpSessionEventPublisher = false;
            // User caching
            cacheUsers = !GrailsUtil.isDevelopmentEnv()

            // Port mappings
            portMapper.httpPort = 8080
            portMapper.httpsPort = 8443

            // Secure channel filter (http/https)
            secureChannel.definition = [
                    '/**': 'ANY_CHANNEL'
            ]
            // Ip restriction filter
            ipRestrictions = [:]

        }
    }
}

oauth {
    providers {
        google {
            api = GoogleApi
            key = "829584903408.apps.googleusercontent.com"
            secret = "xZwkbo5uCZf2kgzB7avhjF-9"
            scope = "https://www.googleapis.com/auth/userinfo.email " +
                    "https://www.googleapis.com/auth/userinfo.profile"
            callback = "${grails.serverURL}/oauth/google/callback"
            successUri = "${grails.serverURL}/springSecurityOAuth/onSuccess?provider=google"
        }
        facebook {
            api = FacebookApi
            key = "1378928025682476"
            secret = "881c6999711c47c9f5060e0e1a11ea8e"
            callback = "${grails.serverURL}/oauth/facebook/callback"
            successUri = "${grails.serverURL}/springSecurityOAuth/onSuccess?provider=facebook"
        }
    }
    debug = true
}

// Added by the Spring Security OAuth plugin:
grails.plugins.springsecurity.oauth.domainClass = 'OAuthID'
grails.plugins.springsecurity.oauth.registration.askToLinkOrCreateAccountUri = "${grails.serverURL}/springSecurityOAuth/askToLinkOrCreateAccount"

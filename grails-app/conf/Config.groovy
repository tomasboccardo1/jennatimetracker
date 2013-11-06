grails.config.locations = [
        customconf.CustomDataSource,
        customconf.CustomJabberBot,
        customconf.CustomEmailNotificationsConfig,
        customconf.CustomSearchable,
        customconf.CustomSecurityConfig
]

grails.plugin.databasemigration.updateOnStart = true
grails.plugin.databasemigration.updateOnStartFileNames = ['changelog.groovy']

// Enables the parsing of file extensions from URLs into the request format
grails.mime.file.extensions = true
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

// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"

// Enabled native2ascii conversion of i18n properties files
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



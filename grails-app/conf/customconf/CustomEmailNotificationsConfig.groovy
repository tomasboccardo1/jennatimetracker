package customconf

mailSender {
    useMail = true
    mailHost = 'smtp.gmail.com'
    mailPort = 587
    mailProtocol = 'smtp'
    javaMailProperties = ['mail.smtp.auth': 'true', 'mail.smtp.starttls.enable': 'true', 'mail.smtp.starttls.required': 'true']
}

environments {
    development {
        mailSender {
            mailUsername = 'developer@fdvsolutions.com'
            mailFrom = 'developer@fdvsolutions.com'
            mailPassword = 'superFDV'
        }
    }
    production {
        mailSender {
            mailUsername = 'developer@fdvsolutions.com'
            mailFrom = 'developer@fdvsolutions.com'
            mailPassword = 'superFDV'
        }
    }
}

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
            mailUsername = 'federicofarina22@gmail.com'
            mailFrom = 'federicofarina22@gmail.com'
            mailPassword = '74123698'
        }
    }
    production {
        mailSender {
            mailUsername = 'federicofarina22@gmail.com'
            mailPassword = 'federicofarina22@gmail.com'
            mailFrom = '74123698'
        }
    }
}

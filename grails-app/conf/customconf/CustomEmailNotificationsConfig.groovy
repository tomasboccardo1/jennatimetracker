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
            mailUsername = 'developerfdv@gmail.com'
            mailFrom = 'developerfdv@gmail.com'
            mailPassword = 'elchuu123'
        }
    }
    production {
        mailSender {
            mailUsername = 'developerfdv@gmail.com'
            mailPassword = 'developerfdv'
            mailFrom = 'developerfdv@gmail.com'
        }
    }
}

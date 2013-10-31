import org.springframework.mail.MailException
import org.springframework.mail.MailSender
import org.springframework.mail.javamail.MimeMessageHelper

import javax.mail.MessagingException
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class EmailerService {

    boolean transactional = false

    MailSender mailSender

    /**
     * Send a list of emails
     *
     * @param mails a list of maps
     */
    def sendEmails(mails) {
        def messages = mails.collect { mail ->
            MimeMessage mimeMessage = mailSender.createMimeMessage()
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "ISO-8859-1")

            /**
             * Arreglo berreta porque en las llamadas se estan enviando strings pero tambien listas de strings
             **/
            if(mail.to instanceof InternetAddress)
                helper.to = mail.to
            else
                helper.to = new InternetAddress(mail.to[0])

            helper.subject = mail.subject
            helper.setText(mail.text, true)

            /**
             * Arreglo berreta porque en las llamadas se estan enviando strings pero tambien listas de strings
             **/
            if(mail.from instanceof InternetAddress)
                helper.from = mail.from
            else
                helper.from = new InternetAddress(mail.from[0])

            if (mail.attachments) {
                mail.attachments.each { attach ->
                    helper.addAttachment(attach.name, attach)
                }
            }
            mimeMessage
        }
        try {
            mailSender.send messages as MimeMessage[]
        } catch (MailException ex) {
            log.error('Failed to send emails', ex)
        } catch (MessagingException ex) {
            log.error('Failed to send emails', ex)
        }
    }
}

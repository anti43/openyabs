/*
 *  This file is part of YaBS.
 *
 *      YaBS is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      YaBS is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.mail;

import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import mpv5.logging.Log;

/**
 * A simple mail implemtenation, taken from
 * http://www.tutorials.de/forum/java/255387-email-mit-javamail-versenden.html
 * 
 */
public class SimpleMail {

    private final String recepient;

    /**
     * Generate and send a mail
     * @param subject
     * @param message
     * @param sender
     * @param recepient
     */
    public SimpleMail(String subject, String message, String sender, String recepient) {
        sendMail(null, null, null, sender, recepient, subject, message);
        this.recepient = recepient;
    }

    /**
     * Send a mail
     * @param smtpHost
     * @param username
     * @param password
     * @param senderAddress
     * @param recipientsAddress
     * @param subject
     * @param text
     */
    private void sendMail(String smtpHost, String username, String password, String senderAddress, String recipientsAddress, String subject, String text) {
        MailAuthenticator auth = new MailAuthenticator(username, password);

        Properties properties = null;
        Session session = null;
        if (smtpHost != null) {
            properties = new Properties();
            properties.put("mail.smtp.host", smtpHost);
            if (username != null) {
                properties.put("mail.smtp.auth", "true");
            }
            session = Session.getDefaultInstance(properties, auth);
        }
        


        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(senderAddress));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(
                    recipientsAddress, false));
            msg.setSubject(subject);
            msg.setText(text);
            msg.setSentDate(new Date());
            Transport.send(msg);
        } catch (Exception e) {
            Log.Debug(e);
        }
    }

    class MailAuthenticator extends Authenticator {

        private final String user;
        private final String password;

        public MailAuthenticator(String user, String password) {
            this.user = user;
            this.password = password;
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(this.user, this.password);
        }
    }
}

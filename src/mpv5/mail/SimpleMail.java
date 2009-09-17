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

import java.io.File;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Popup;
import mpv5.utils.export.Export;
import mpv5.utils.jobs.Waiter;

/**
 * A simple mail implementation, taken from
 * http://www.tutorials.de/forum/java/255387-email-mit-javamail-versenden.html
 * 
 */
public class SimpleMail implements Waiter {

    private String smtpHost ="";
    private String username ="";
    private String password ="";
    private String senderAddress ="";
    private String recipientsAddress ="";
    private String subject ="";
    private String text ="";
    private boolean useTls;
    private File attachment;

    /**
     * 
     * @param smtpHost
     * @param username
     * @param password
     * @param senderAddress
     * @param recipientsAddress
     * @param subject
     * @param text
     * @throws MessagingException
     */
    public SimpleMail(String smtpHost, String username, String password, String senderAddress, String recipientsAddress, String subject, String text) throws MessagingException {
        this.smtpHost = smtpHost;
        this.username = username;
        this.password = password;
        this.senderAddress = senderAddress;
        this.recipientsAddress = recipientsAddress;
        this.subject = subject;
        this.text = text;
    }

    public SimpleMail() {
    }

    /**
     * @author Sateesh Rudrangi
     * @param smtpHost
     * @param username
     * @param password
     * @param from
     * @param recipientsAddress
     * @param subject
     * @param text
     * @param attachment
     * @throws MessagingException
     */
    private void sendMail() throws MessagingException {
        MailAuthenticator auth = new MailAuthenticator(username, password);

        Properties properties = null;
        Session session = null;
        properties = new Properties();
        properties.put("mail.smtp.host", smtpHost);
        if (username != null) {
            properties.put("mail.smtp.auth", "true");
        }
        if (useTls) {
            properties.put("mail.smtp.starttls.enable", "true");
        }

        session = Session.getDefaultInstance(properties, auth);

        // Define message
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(senderAddress));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipientsAddress));
        message.setSubject(subject);

        // create the message part
        MimeBodyPart messageBodyPart = new MimeBodyPart();

        //fill message
        messageBodyPart.setText(text);
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);

        if (attachment != null) {
            // Part two is attachment
            messageBodyPart = new MimeBodyPart();
            DataSource source = new FileDataSource(attachment);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(attachment.getName());
            multipart.addBodyPart(messageBodyPart);
        }

        // Put parts in message
        message.setContent(multipart);

        // Send the message
        Transport.send(message);
        Log.Debug(this, "Mail sent: " + message);
        Popup.notice(Messages.MAIL_SENT + " " + recipientsAddress);
    }

    public void set(Object object, Exception exception) throws Exception {
        if (exception == null) {
            try {
                if (object instanceof Export) {
                    setAttachment(((Export) object).getTargetFile());
                    sendMail();
                } else {
                    setAttachment((File) object);
                    sendMail();
                }
            } catch (Exception e) {
                throw e;
            } finally {
            }
        } else {
            throw exception;
        }
    }

    /**
     * Set the configuration for mails
     * @param c
     * @throws UnsupportedOperationException
     */
    public void setMailConfiguration(MailConfiguration c) throws UnsupportedOperationException {
        if (c != null) {
            setPassword(c.getPassword());
            setSenderAddress(c.getSenderAddress());
            setSmtpHost(c.getSmtpHost());
            setUsername(c.getUsername());
            setUseTls(c.isUseTls());
        } else {
            throw new UnsupportedOperationException("MailConfig cannot be null!");
        }
    }

    /**
     * @return the smtpHost
     */
    public String getSmtpHost() {
        return smtpHost;
    }

    /**
     * @param smtpHost the smtpHost to set
     */
    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the senderAddress
     */
    public String getSenderAddress() {
        return senderAddress;
    }

    /**
     * @param senderAddress the senderAddress to set
     */
    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }

    /**
     * @return the recipientsAddress
     */
    public String getRecipientsAddress() {
        return recipientsAddress;
    }

    /**
     * @param recipientsAddress the recipientsAddress to set
     */
    public void setRecipientsAddress(String recipientsAddress) {
        this.recipientsAddress = recipientsAddress;
    }

    /**
     * @return the subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * @param subject the subject to set
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * @param text the text to set
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * @return the attachment
     */
    public File getAttachment() {
        return attachment;
    }

    /**
     * @param attachment the attachment to set
     */
    public void setAttachment(File attachment) {
        this.attachment = attachment;
    }

    /**
     * @return the useTls
     */
    public boolean isUseTls() {
        return useTls;
    }

    /**
     * @param useTls the useTls to set
     */
    public void setUseTls(boolean useTls) {
        this.useTls = useTls;
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

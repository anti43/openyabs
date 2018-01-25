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

import com.sun.mail.smtp.SMTPSSLTransport;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Notificator;
import mpv5.ui.dialogs.Popup;
import mpv5.utils.export.Export;
import mpv5.utils.jobs.Waiter;

/**
 * A simple mail implementation, taken from
 * http://www.tutorials.de/forum/java/255387-email-mit-javamail-versenden.html
 *
 */
public class SimpleMail implements Waiter {

    private String smtpHost = "";
    private String smtpPort = "";
    private String username = "";
    private String password = "";
    private String senderAddress = "";
    private String recipientsAddress = "";
    private String subject = "";
    private String text = "";
    private String bccAddress = null;
    private List<String> cc = new ArrayList<String>();
    private List<String> bcc = new ArrayList<String>();
    private List<String> rec = new ArrayList<String>();
    private boolean useTls;
    private boolean useSSL;
    private boolean useSmtps;
    private File attachment;
    private String ccAddress;

    /**
     *
     * @param smtpHost
     * @param smtpPort
     * @param username
     * @param password
     * @param useTls
     * @param useSSL
     * @param useSmtps
     * @param senderAddress
     * @param recipientsAddress
     * @param subject
     * @param text
     * @throws MessagingException
     */
    public SimpleMail(String smtpHost, String smtpPort, String username, String password, boolean useTls,boolean useSSL,boolean useSmtps, String senderAddress, String recipientsAddress, String subject, String text) throws MessagingException {
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.username = username;
        this.password = password;
        this.useTls   = useTls;
        this.useSSL   = useSSL;
        this.useSmtps = useSmtps;
        this.senderAddress = senderAddress;
        this.recipientsAddress = recipientsAddress;
        this.subject = subject;
        this.text = text;
        this.sendMail();
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
    private void sendMail() {
        Runnable runnable = new Runnable() {

            @Override
            public void run() {

                if (!useSmtps) {
                    try {
                        sendSmptmail();
                    } catch (MessagingException ex) {
                        Log.Debug(ex);
                    }
                } else {
                    try {
                        sendSmptsMail();
                    } catch (Exception ex) {
                        Notificator.raiseNotification(new RuntimeException("From: " + senderAddress + " to: " + recipientsAddress , ex), true);
                    }  
                }

            }
        };
        new Thread(runnable).start();
    }

    @Override
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
            setSmtpPort(c.getSmtpPort());
            setUsername(c.getUsername());
            setUseTls(c.isUseTls());
            setUseSSL(c.isUseSSL());
            setUseSmtps(c.isUseSmtps());
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
     * @return the smtpHost
     */
    public String getSmtpPort() {
        return smtpPort;
    }

    /**
     * @param smtpPort
     */
    public void setSmtpPort(String smtpPort) {
        this.smtpPort = smtpPort;
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
        String[] ccs = recipientsAddress.split(";");
        for (int i = 0; i < ccs.length; i++) {
            String ccc = ccs[i];
            addAddress(ccc);
        }
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

    /**
     * @return the useSSL
     */
    public boolean isUseSSL() {
        return useSSL;
    }

    /**
     * @param useSSL the useSSL to set
     */
    public void setUseSSL(boolean useSSL) {
        this.useSSL = useSSL;
    }

    /**
     * @return the useSmtps
     */
    public boolean isUseSmtps() {
        return useSmtps;
    }

    /**
     * @param useSmtps the useSmtps to set
     */
    public void setUseSmtps(boolean useSmtps) {
        this.useSmtps = useSmtps;
    }

    private void sendSmptmail() throws MessagingException {
        Log.Debug(this, "Sending mail via SMTP");
        mpv5.YabsViewProxy.instance().getProgressbar().setIndeterminate(true);
        MailAuthenticator auth = new MailAuthenticator(username, password);

        Properties properties;
        Session session;
        properties = new Properties();
        properties.put("mail.smtp.localhost", smtpHost);
        properties.put("mail.smtp.host", smtpHost);

        if (useSSL) {
            properties.put("mail.smtp.ssl.enable", true);
            properties.put("mail.smtp.starttls.enable", "true");
//            properties.put("mail.smtp.auth.mechanisms", "XOAUTH2");
//            properties.put(OAuth2SaslClientFactory.OAUTH_TOKEN_PROP, oauthToken);
        }
        
        properties.put("mail.smtp.port", (smtpPort!=null&&smtpPort.length()>1?smtpPort:"25"));
        if (username != null) {
            properties.put("mail.smtp.auth", "true");
        }
        if (useTls) {
            properties.put("mail.smtp.starttls.enable", "true");
        }

        session = Session.getInstance(properties, auth);
        if (Log.getLoglevel() == Log.LOGLEVEL_DEBUG)
            session.setDebug(true);
        // Define message
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(senderAddress));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipientsAddress));
        if (getBccAddress() != null) {
            message.addRecipient(Message.RecipientType.BCC, new InternetAddress(bccAddress));
        }
        message.setSubject(subject);
        message.setSentDate(new Date());

        // create the message part
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent( text, "text/html; charset=utf-8" );
        //fill message
        //messageBodyPart.setText(text);
        
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
        try {
            // Send the message
            Transport.send(message);
            mpv5.YabsViewProxy.instance().getProgressbar().setIndeterminate(false);
            Log.Debug(this, "Mail sent: " + message);
            Popup.notice(Messages.MAIL_SENT + " " + recipientsAddress);
        } catch (MessagingException messagingException) {
            Popup.error(messagingException);
            Log.Debug(this, messagingException.getLocalizedMessage());
        } finally {
// close the connection
        }
    }

    /**
     * From http://www.nepherte.be/send-mail-over-smtps-in-java/
     * @throws NoSuchProviderException
     * @throws MessagingException
     */
    private void sendSmptsMail() throws NoSuchProviderException, MessagingException {
        mpv5.YabsViewProxy.instance().getProgressbar().setIndeterminate(true);
        Log.Debug(this, "Sending mail via SMTPS");
        // create properties
        Properties props = System.getProperties();
        MailAuthenticator auth = new MailAuthenticator(username, password);

        props.put("mail.smtps.auth", Boolean.toString(username != null));
        props.put("mail.smtps.starttls.enable", Boolean.toString(useTls));
// < -- it is important you use the correct port. smtp uses 25, smtps 465 -->
        props.put("mail.smtps.port", (smtpPort!=null&&smtpPort.length()>1?smtpPort:"465"));
// < -- put the smtps server host address here -->
        props.put("mail.smtps.host", smtpHost);

//
//        props.put("mail.smtp.port", "587");
//        props.put("mail.smtp.auth", (username != null));
//        props.put("mail.smtp.starttls.enable", useTls);

// create session
        Session session = Session.getDefaultInstance(props, auth);
//        session.setDebug(true);

        // Define message
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(senderAddress));
        message.setSentDate(new Date());
        
        for (int i = 0; i < rec.size(); i++) {
            String recc = rec.get(i);
            try {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(recc));
            } catch (MessagingException messagingException) {
                Notificator.raiseNotification(messagingException);
            }
        }
        for (int i = 0; i < bcc.size(); i++) {
            String bbc = bcc.get(i);
            try {
                message.addRecipient(Message.RecipientType.BCC, new InternetAddress(bbc));
            } catch (MessagingException messagingException) {
                Notificator.raiseNotification(messagingException);
            }
        }
        for (int i = 0; i < cc.size(); i++) {
            String ccc = cc.get(i);
            try {
                message.addRecipient(Message.RecipientType.CC, new InternetAddress(ccc));
            } catch (MessagingException messagingException) {
                Notificator.raiseNotification(messagingException);
            }
        }
        message.setSubject(subject);

        // create the message part
        MimeBodyPart messageBodyPart = new MimeBodyPart();

        //fill message
        messageBodyPart.setContent( text, "text/html; charset=utf-8" );
        //messageBodyPart.setText(text);
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
        message.saveChanges();
// transport the message
// < -- we will send the message over smtps -->
        SMTPSSLTransport transport = (SMTPSSLTransport) session.getTransport("smtps");

// connect to server
        try {
            // < -- fill in email address and password -->
            transport.connect();
// send the message
            transport.sendMessage(message, message.getAllRecipients());
            mpv5.YabsViewProxy.instance().getProgressbar().setIndeterminate(false);
            Log.Debug(this, "Mail sent: " + message);
            Popup.notice(Messages.MAIL_SENT + " " + recipientsAddress);
        } catch (MessagingException messagingException) {
            Popup.error(messagingException);
            Log.Debug(this, messagingException.getLocalizedMessage());
        } finally {
// close the connection
            transport.close();
        }
    }

    /**
     * @return the bccAddress
     */
    public String getBccAddress() {
        return bccAddress;
    }

    /**
     * @param bccAddress the bccAddress to set
     */
    public void setBccAddress(String bccAddress) {
        this.bccAddress = bccAddress;
        String[] ccs = bccAddress.split(";");
        for (int i = 0; i < ccs.length; i++) {
            String ccc = ccs[i];
            addBCCAddress(ccc);
        }
    }

    public void addCCAddress(String ccc) {
        cc.add(ccc);
    }

    public void setCCAddress(String cc) {
        this.ccAddress = cc;
        String[] ccs = cc.split(";");
        for (int i = 0; i < ccs.length; i++) {
            String ccc = ccs[i];
            addCCAddress(ccc);
        }
    }

    private void addAddress(String ccc) {
        rec.add(ccc);
    }

    private void addBCCAddress(String ccc) {
        bcc.add(ccc);
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

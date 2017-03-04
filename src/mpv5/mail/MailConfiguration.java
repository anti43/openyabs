package mpv5.mail;

/**
 *
 * @author anti
 */
public class MailConfiguration {

    private String smtpHost;
    private String smtpPort;
    private String username;
    private String password;
    private String senderAddress;
    private boolean useTls;
    private boolean useSmtps;
    private Boolean useSSL;

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

    /**
     * @return the smtpPort
     */
    public String getSmtpPort() {
        return smtpPort;
    }

    /**
     * @param smtpPort the smtpPort to set
     */
    public void setSmtpPort(String smtpPort) {
        this.smtpPort = smtpPort;
    }

    /**
     * @param useSSL the useSSL to set
     */
    public void setUseSSL(Boolean useSSL) {
        this.useSSL = useSSL;
    }
    
    /**
     * @return the useSSL
     */
    public boolean isUseSSL() {
        return useSSL;
    }
}

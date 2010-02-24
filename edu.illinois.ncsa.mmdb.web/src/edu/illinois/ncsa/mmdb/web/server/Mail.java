/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Send email for notification purposes.
 * 
 * @author Luigi Marini
 * 
 */
public class Mail
{
    /** Commons logging **/
    private static Log          log                     = LogFactory.getLog( Mail.class );

    private static final String USER_ACTIVATION_SUBJECT = "Account Activated";
    private static final String USER_ACTIVATION_BODY    = "Your account has been activated. \n\n-The Management";

    private static final String NEW_PASSWORD_SUBJECT    = "New Password";
    private static final String NEW_PASSWORD_BODY       = "Your new password is: ";

    private static final String NEW_USER_SUBJECT        = "New User";
    private static final String NEW_USER_BODY           = "A new user has registered: ";

    private static Properties   configuration           = new Properties();

    public static void setProperties( Properties mail )
    {
        configuration.clear();
        configuration.putAll( mail );
    }

    /**
     * Notify user of authorization change.
     * 
     * @param userAddress
     */
    public static void userAuthorized( String userAddress )
    {
        sendMessage( userAddress, USER_ACTIVATION_SUBJECT, USER_ACTIVATION_BODY );
    }

    public static void sendNewPassword( String email, String newPassword )
    {
        sendMessage( email, NEW_PASSWORD_SUBJECT, NEW_PASSWORD_BODY + newPassword );
    }

    /**
     * Notify admin of new user registration.
     * 
     * @param userAddress
     */
    public static void userAdded( String userAddress )
    {
        sendMessage( configuration.getProperty( "mail.from" ), NEW_USER_SUBJECT, NEW_USER_BODY + userAddress ); //$NON-NLS-1$
    }

    private static void sendMessage( String rcpt, String subject, String body )
    {
        String from = configuration.getProperty( "mail.from" ); //$NON-NLS-1$
        String presubj = configuration.getProperty( "mail.subject", "[MEDICI]" ); //$NON-NLS-1$
        String fullname = configuration.getProperty( "mail.fullname", "Medici" ); //$NON-NLS-1$

        Session session = Session.getDefaultInstance( configuration, null );
        MimeMessage message = new MimeMessage( session );
        try {
            message.setFrom( new InternetAddress( from, fullname ) );
            message.addRecipient( Message.RecipientType.TO, new InternetAddress( rcpt ) );
            message.setSubject( String.format( "%s %s", presubj, subject ) ); //$NON-NLS-1$
            message.setText( body );
            Transport.send( message );
            log.debug( String.format( "Mail sent to %s with subject '%s'", rcpt, subject ) );
        } catch ( MessagingException e ) {
            log.error( String.format( "Unable to send mail sent to %s with subject '%s'", rcpt, subject ), e );
        } catch ( UnsupportedEncodingException e ) {
            log.error( String.format( "Unable to send mail sent to %s with subject '%s'", rcpt, subject ), e );
        }
    }
}

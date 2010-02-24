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

    private static final String USER_ACTIVATION_SUBJECT = "[Medici] Account Activated";
    private static final String USER_ACTIVATION_BODY    = "Your account has been activated. \n\n-The Management";

    private static final String NEW_PASSWORD_SUBJECT    = "[Medici] New Password";
    private static final String NEW_PASSWORD_BODY       = "Your new password is: ";

    private static final String NEW_USER_SUBJECT        = "[Medici] New User";
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

    private static void sendMessage( String to, String subject, String body )
    {
        Session session = Session.getDefaultInstance( configuration, null );
        MimeMessage message = new MimeMessage( session );
        try {
            message.setFrom( new InternetAddress( session.getProperty( "mail.from" ), session.getProperty( "mail.fullname" ) ) ); //$NON-NLS-1$ //$NON-NLS-2$
            message.addRecipient( Message.RecipientType.TO, new InternetAddress( to ) );
            message.setSubject( subject );
            message.setText( body );
            Transport.send( message );
            log.debug( String.format( "Mail sent to %s with subject '%s'", to, subject ) );
        } catch ( MessagingException e ) {
            log.error( String.format( "Unable to send mail sent to %s with subject '%s'", to, subject ), e );
        } catch ( UnsupportedEncodingException e ) {
            log.error( String.format( "Unable to send mail sent to %s with subject '%s'", to, subject ), e );
        }
    }
}

/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server;

import java.io.IOException;
import java.io.InputStream;
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
public class Mail {

	private static final String MEDICI_FROM = "Medici";

	private static final String CONFIGURATION_PATH = "mail.properties";

	private static final String USER_ACTIVATION_SUBJECT = "[Medici] Account Activated";

	private static final String USER_ACTIVATION_BODY = "Your account has been activated. \n\n-The Management";

	private static final String NEW_PASSWORD_SUBJECT = "[Medici] New Password";

	private static final String NEW_PASSWORD_BODY = "Your new password is: ";
	
	private static final String NEW_USER_SUBJECT = "[Medici] New User";

	private static final String NEW_USER_BODY = "A new user has registered: ";

	private static Properties configuration = new Properties();

	/** Commons logging **/
	private static Log log = LogFactory.getLog(Mail.class);


	static {
		loadConfig();
	}

	/**
	 * Load mail configuration file mail.properties. File should be available in
	 * the WEB-INF/classes directory.
	 */
	private static void loadConfig() {
		// context location
		String path = CONFIGURATION_PATH;

		if (!path.startsWith("/")) {
			path = "/" + path;
		}

		log.debug("Trying to load property files " + path);

		InputStream input = Mail.class.getResourceAsStream(path);
		try {
			configuration.load(input);
		} catch (IOException e) {
			log.error("Unable to load the mail configuration file", e);
		} finally {
			try {
				if (input != null)
					input.close();
			} catch (IOException ex) {
				log.error("Unable to load the mail configuration file", ex);
			}
		}
	}
	
	/**
	 * Notify user of authorization change.
	 * 
	 * @param userAddress
	 */
	public static void userAuthorized(String userAddress) {
		Session session = Session.getDefaultInstance(configuration, null);
		MimeMessage message = new MimeMessage(session);
		try {
			String from = session.getProperty("mail.from");
			message.setFrom(new InternetAddress(from, MEDICI_FROM));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(
					userAddress));
			message.setSubject(USER_ACTIVATION_SUBJECT);
			message.setText(USER_ACTIVATION_BODY);
			Transport.send(message);
			log.debug("Mail sent to " + userAddress
					+ " to notify of change in permissions.");
		} catch (MessagingException e) {
			log.error("Unable to send email message for users authorized to "
					+ userAddress, e);
		} catch (UnsupportedEncodingException e) {
			log.error("Unable to send email message for users authorized to "
					+ userAddress, e);
		}
	}

	public static void sendNewPassword(String email, String newPassword)
			throws UnsupportedEncodingException, MessagingException {
		Session session = Session.getDefaultInstance(configuration, null);
		MimeMessage message = new MimeMessage(session);

		String from = session.getProperty("mail.from");
		message.setFrom(new InternetAddress(from, MEDICI_FROM));
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(
				email));
		message.setSubject(NEW_PASSWORD_SUBJECT);
		message.setText(NEW_PASSWORD_BODY + newPassword);
		Transport.send(message);
		log.debug("Mail sent to " + email
				+ " to notify of change in permissions.");
	}

	/**
	 * Notify admin of new user registration.
	 * 
	 * @param userAddress
	 */
	public static void userAdded(String userAddress) {
		Session session = Session.getDefaultInstance(configuration, null);
		MimeMessage message = new MimeMessage(session);
		String from = session.getProperty("mail.from");
		try {
			message.setFrom(new InternetAddress(from, MEDICI_FROM));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(
					from, MEDICI_FROM));
			message.setSubject(NEW_USER_SUBJECT);
			message.setText(NEW_USER_BODY + userAddress);
			Transport.send(message);
			log.debug("Mail sent to " + from
					+ " to notify a new user has registered.");
		} catch (MessagingException e) {
			log.error("Unable to send email message for new user registration to "
					+ from, e);
		} catch (UnsupportedEncodingException e) {
			log.error("Unable to send email message for new user registration to "
					+ from, e);
		}
	}

}

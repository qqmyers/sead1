package edu.illinois.ncsa.mmdb.web.server;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.TripleMatcher;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.Triple;
import org.tupeloproject.rdf.terms.Foaf;
import org.tupeloproject.rdf.terms.Rdf;

import edu.illinois.ncsa.bard.jaas.PasswordDigest;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.mmdb.MMDB;

/**
 * 
 * @author Luigi Marini
 * 
 */
public class PasswordManagement {
	
	private static final String SYMBOLS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	
	/**
	 * 
	 * @param name
	 * @param email
	 * @param password
	 * @throws OperatorException
	 */
	public static void addUser(String name, String email, String password)
			throws OperatorException {
		String passwordDigest = PasswordDigest.digest(password);
		Resource personURI = Resource.uriRef(PersonBeanUtil.getPersonID(email));

		Context context = TupeloStore.getInstance().getContext();

		context.addTriple(personURI, MMDB.HAS_PASSWORD,
				passwordDigest);
		context.addTriple(personURI, Rdf.TYPE, Foaf.PERSON);
		context.addTriple(personURI, Foaf.NAME, name);
		context.addTriple(personURI, Foaf.MBOX, email);
	}

	/**
	 * 
	 * @param length
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static String generateNewPassword(int length)
			throws NoSuchAlgorithmException {

		SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

		char[] buf = new char[length];
		for (int idx = 0; idx < buf.length; ++idx) {
			buf[idx] = SYMBOLS.charAt(random.nextInt(SYMBOLS.length()));
		}

		return new String(buf);
	}
	
	public static void updatePassword(String userUri, String newPassword)
			throws OperatorException {
		Resource user = Resource.uriRef(userUri);
		Resource predicate = MMDB.HAS_PASSWORD;
		String passwordDigest = PasswordDigest.digest(newPassword);
		Context context = TupeloStore.getInstance().getContext();
		
		// remove old password digests
		TripleMatcher tripleMatcher = new TripleMatcher();
		tripleMatcher.setSubject(user);
		tripleMatcher.setPredicate(predicate);
		context.perform(tripleMatcher);
		for (Triple triple : tripleMatcher.getResult()) {
			context.removeTriples(triple);
		}
		
		// add new password digest
		context.addTriple(user, predicate, passwordDigest);
	}

	/**
	 * For testing purposes.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		for (int i = 0; i < 100; i++) {
			try {
				System.out.println(generateNewPassword(8));
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}

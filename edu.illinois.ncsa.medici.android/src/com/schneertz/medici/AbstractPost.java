package com.schneertz.medici;

import java.io.IOException;

import org.apache.commons.httpclient.HttpException;

/**
 * Base class for facilities for posting to Medici
 */
public abstract class AbstractPost {
	Creds creds; // tumblr credentials
	String medici; // medici to post to
	String text; // text to post

	public Creds getCreds() {
		return creds;
	}

	public void setCreds(Creds creds) {
		this.creds = creds;
	}
	
	public void setCreds(String email, String password) {
		setCreds(new Creds(email,password));
	}
	
	public String getMedici() {
		return medici;
	}

	public void setMedici(String medici) {
		this.medici = medici;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	String contentType;
	void setContentType(String mimeType) {
		contentType=mimeType;
	}
	String getContentType() {
		return contentType;
	}
	
	/**
	 * Post the content, blocking until medici responds
	 * @return the HTTP status of the response
	 * @throws IOException 
	 * @throws HttpException 
	 */
	public abstract int post() throws HttpException, IOException;
}

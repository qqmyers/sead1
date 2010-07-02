package com.schneertz.medici;

import org.apache.commons.httpclient.Cookie;

public class Creds {
	String email;
	String password;
	Cookie cookie;
	
	public Creds() { }
	public Creds(String email, String password) {
		setEmail(email);
		setPassword(password);
	}
	public Creds(Cookie cookie) {
		setCookie(cookie);
	}
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public Cookie getCookie() {
		return cookie;
	}
	public void setCookie(Cookie cookie) {
		this.cookie = cookie;
	}
}

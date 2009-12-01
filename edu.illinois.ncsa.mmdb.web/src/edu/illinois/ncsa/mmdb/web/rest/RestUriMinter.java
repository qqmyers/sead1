package edu.illinois.ncsa.mmdb.web.rest;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.util.Base64;

public class RestUriMinter {
	static RestUriMinter singleton;
	MessageDigest messageDigest = null;
	String seed = "";
	
	public static RestUriMinter getInstance() {
		if(singleton == null) {
			singleton = new RestUriMinter();
		}
		return singleton;
	}
	
	public String mintUri(Map<Resource,Object> metadata) {
		if(messageDigest == null) {
			try {
				messageDigest = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) {
				// this should not happen
				e.printStackTrace();
			}
		}
		String prefix = "data";
		if(metadata != null) {
			if(RestService.IMAGE_TYPE.equals(metadata.get(Rdf.TYPE))) {
				prefix = "img";
			}
			if(metadata.containsKey(RestService.FORMAT_PROPERTY) &&
				metadata.get(RestService.FORMAT_PROPERTY).toString().startsWith("image/")) {
				prefix = "img";
			}
			if(RestService.COLLECTION_TYPE.equals(metadata.get(Rdf.TYPE))) {
				prefix = "col";
			}
		}
		seed += System.currentTimeMillis() + Runtime.getRuntime().freeMemory();
		try {
			byte[] md = messageDigest.digest(seed.getBytes("US-ASCII"));
			String b64 = Base64.encodeBytes(md).replaceAll("/","_").replaceAll("\\+","-").replaceFirst("=*$","");
			seed = b64;
			return "tag:medici@uiuc.edu,2009:"+prefix+"_" + b64;
		} catch (UnsupportedEncodingException e) {
			// this should not happen
			e.printStackTrace();
			return null;
		}
	}
}

package org.sead.acr.common.utilities;

import java.util.HashMap;
import java.util.Map;

public class MimeMap {

	private static Map<String, String> mimeMap = new HashMap<String, String>();

	static {
		setMimeMap();
	}

	public static String findCategory(String fileExt) {

		if (mimeMap.get(fileExt) == null)
			return "Other";
		return findCategoryFromType(mimeMap.get(fileExt));
	}

	public static String findCategoryFromType(String mimetype) {

		if (mimetype == null)
			return "Other";

		if (mimetype.startsWith("image/")) {
			return "Image";
		} else if (mimetype.startsWith("video/")) {
			return "Video";
		} else if (mimetype.startsWith("audio/")) {
			return "Audio";
		} else if (mimetype.endsWith("x-tgif")) {
			return "3D";
		} else if (mimetype.startsWith("text/")) {
			return "Document";
		} else if (mimetype.endsWith("pdf")) {
			return "Document";
		} else if (mimetype.endsWith("word")) {
			return "Document";
		} else if (mimetype.contains("wordprocessingml")) {
			return "Document";
		} else if (mimetype.endsWith("powerpoint")) {
			return "Document";
		} else if (mimetype.endsWith("excel")) {
			return "Document";
		} else if (mimetype.contains("spreadsheetml")) {
			return "Document";
		} else {
			return "Other";
		}
	}

	private static void setMimeMap() {

		// add some common mime types
		mimeMap.put("evy", "application/envoy"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("fif", "application/fractals"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("spl", "application/futuresplash"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("hta", "application/hta"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("acx", "application/internet-property-stream"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("hqx", "application/mac-binhex40"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("doc", "application/msword"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("dot", "application/msword"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("bin", "application/octet-stream"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("class", "application/octet-stream"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("dms", "application/octet-stream"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("exe", "application/octet-stream"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("lha", "application/octet-stream"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("lzh", "application/octet-stream"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("oda", "application/oda"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("axs", "application/olescript"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("pdf", "application/pdf"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("prf", "application/pics-rules"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("p10", "application/pkcs10"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("crl", "application/pkix-crl"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("ai", "application/postscript"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("eps", "application/postscript"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("ps", "application/postscript"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("rtf", "application/rtf"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("setpay", "application/set-payment-initiation"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("setreg", "application/set-registration-initiation"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("xla", "application/vnd.ms-excel"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("xlc", "application/vnd.ms-excel"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("xlm", "application/vnd.ms-excel"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("xls", "application/vnd.ms-excel"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("xlt", "application/vnd.ms-excel"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("xlw", "application/vnd.ms-excel"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("sst", "application/vnd.ms-pkicertstore"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("cat", "application/vnd.ms-pkiseccat"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("stl", "application/vnd.ms-pkistl"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("pot,", "application/vnd.ms-powerpoint"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("pps", "application/vnd.ms-powerpoint"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("ppt", "application/vnd.ms-powerpoint"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("mpp", "application/vnd.ms-project"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("wcm", "application/vnd.ms-works"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("wdb", "application/vnd.ms-works"); //$NON-NLS-1$   //$NON-NLS-2$    
		mimeMap.put("wks", "application/vnd.ms-works"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("wps", "application/vnd.ms-works"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("hlp", "application/winhlp"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("bcpio", "application/x-bcpio"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("cdf", "application/x-cdf"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("z", "application/x-compress"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("tgz", "application/x-compressed"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("cpio", "application/x-cpio"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("csh", "application/x-csh"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("dcr", "application/x-director"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("dir", "application/x-director"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("dxr", "application/x-director"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("dvi", "application/x-dvi"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("gtar", "application/x-gtar"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("gz", "application/x-gzip"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("hdf", "application/x-hdf"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("ins", "application/x-internet-signup"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("isp", "application/x-internet-signup"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("iii", "application/x-iphone"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("js", "application/x-javascript"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("latex", "application/x-latex"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("xml", "application/xml"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("mdb", "application/x-msaccess"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("crd", "application/x-mscardfile"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("clp", "application/x-msclip"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("dll", "application/x-msdownload"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("m13", "application/x-msmediaview"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("m14", "application/x-msmediaview"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("mvb", "application/x-msmediaview"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("wmf", "application/x-msmetafile"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("mny", "application/x-msmoney"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("pub", "application/x-mspublisher"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("scd", "application/x-msschedule"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("trm", "application/x-msterminal"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("wri", "application/x-mswrite"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("pma", "application/x-perfmon"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("pmc", "application/x-perfmon"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("pml", "application/x-perfmon"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("pmr", "application/x-perfmon"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("pmw", "application/x-perfmon"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("p12", "application/x-pkcs12"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("pfx", "application/x-pkcs12"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("p7b", "application/x-pkcs7-certificates"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("spc", "application/x-pkcs7-certificates"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("p7r", "application/x-pkcs7-certreqresp"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("p7c", "application/x-pkcs7-mime"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("p7m", "application/x-pkcs7-mime"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("p7s", "application/x-pkcs7-signature"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("sh", "application/x-sh"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("shar", "application/x-shar"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("swf", "application/x-shockwave-flash"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("sit", "application/x-stuffit"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("sv4cpio", "application/x-sv4cpio"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("sv4crc", "application/x-sv4crc"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("tar", "application/x-tar"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("tcl", "application/x-tcl"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("tex", "application/x-tex"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("texi", "application/x-texinfo"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("texinfo", "application/x-texinfo"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("obj", "application/x-tgif"); //$NON-NLS-1$   //$NON-NLS-2$
		mimeMap.put("roff", "application/x-troff"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("t", "application/x-troff"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("tr", "application/x-troff"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("man", "application/x-troff-man"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("me", "application/x-troff-me"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("ms", "application/x-troff-ms"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("avi", "application/x-troff-msvideo"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("ustar", "application/x-ustar"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("src", "application/x-wais-source"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("cer", "application/x-x509-ca-cert"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("crt", "application/x-x509-ca-cert"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("der", "application/x-x509-ca-cert"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("pko", "application/ynd.ms-pkipko"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("zip", "application/zip"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("au", "audio/basic"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("snd", "audio/basic"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("mid", "audio/mid"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("rmi", "audio/mid"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("mp3", "audio/mpeg"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("aif", "audio/x-aiff"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("aifc", "audio/x-aiff"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("aiff", "audio/x-aiff"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("m3u", "audio/x-mpegurl"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("ra", "audio/x-pn-realaudio"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("ram", "audio/x-pn-realaudio"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("wav", "audio/x-wav"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("wma", "audio/x-ms-wma"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("aac", "audio/aac"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("flac", "audio/x-flac"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("gsm", "audio/x-gsm"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("bmp", "image/bmp"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("cod", "image/cis-cod"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("gif", "image/gif"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("ief", "image/ief"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("jp2", "image/jp2"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("jpe", "image/jpeg"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("jpeg", "image/jpeg"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("jpg", "image/jpeg"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("jfif", "image/pipeg"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("svg", "image/svg+xml"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("tif", "image/tiff"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("tiff", "image/tiff"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("ras", "image/x-cmu-raster"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("cmx", "image/x-cmx"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("ico", "image/x-icon"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("pnm", "image/x-portable-anymap"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("pbm", "image/x-portable-bitmap"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("pgm", "image/x-portable-graymap"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("ppm", "image/x-portable-pixmap"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("rgb", "image/x-rgb"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("xbm", "image/x-xbitmap"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("xpm", "image/x-xpixmap"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("xwd", "image/x-xwindowdump"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("mht", "message/rfc822"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("mhtml", "message/rfc822"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("nws", "message/rfc822"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("css", "text/css"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("csv", "text/csv"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("323", "text/h323"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("htm", "text/html"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("html", "text/html"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("stm", "text/html"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("uls", "text/iuls"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("bas", "text/plain"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("c", "text/plain"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("h", "text/plain"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("txt", "text/plain"); //$NON-NLS-1$   //$NON-NLS-2$
		mimeMap.put("rtx", "text/richtext"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("sct", "text/scriptlet"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("tsv", "text/tab-separated-values"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("htt", "text/webviewhtml"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("htc", "text/x-component"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("xml", "text/xml"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("etx", "text/x-setext"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("vcf", "text/x-vcard"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("mp2", "video/mpeg"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("mpa", "video/mpeg"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("mpe", "video/mpeg"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("mpeg", "video/mpeg"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("mpg", "video/mpeg"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("mpv2", "video/mpeg"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("mov", "video/quicktime"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("qt", "video/quicktime"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("lsf", "video/x-la-asf"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("lsx", "video/x-la-asf"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("asf", "video/x-ms-asf"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("asr", "video/x-ms-asf"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("asx", "video/x-ms-asf"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("avi", "video/x-msvideo"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("movie", "video/x-sgi-movie"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("flr", "x-world/x-vrml"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("vrml", "x-world/x-vrml"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("wrl", "x-world/x-vrml"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("wrz", "x-world/x-vrml"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("xaf", "x-world/x-vrml"); //$NON-NLS-1$   //$NON-NLS-2$   
		mimeMap.put("xof", "x-world/x-vrml"); //$NON-NLS-1$   //$NON-NLS-2$ 
		mimeMap.put("png", "image/png"); //$NON-NLS-1$   //$NON-NLS-2$
		mimeMap.put("mts", "video/avchd"); //$NON-NLS-1$   //$NON-NLS-2$
		mimeMap.put("mod", "video/mpeg"); //$NON-NLS-1$   //$NON-NLS-2$
		mimeMap.put("ptm", "application/x-ptm");
	}

}

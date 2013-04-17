




//function SetMimeMap() {
	
	var mimeMap = new Object();
	
	mimeMap["evy"] ="application/envoy"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["fif"] ="application/fractals"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["spl"] ="application/futuresplash"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["hta"] ="application/hta"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["acx"] ="application/internet-property-stream"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["hqx"] ="application/mac-binhex40"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["doc"] ="application/msword"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["dot"] ="application/msword"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["bin"] ="application/octet-stream"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["class"] = "application/octet-stream"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["dms"] ="application/octet-stream"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["exe"] ="application/octet-stream"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["lha"] ="application/octet-stream"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["lzh"] ="application/octet-stream"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["oda"] ="application/oda"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["axs"] ="application/olescript"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["pdf"] ="application/pdf"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["prf"] ="application/pics-rules"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["p10"] ="application/pkcs10"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["crl"] ="application/pkix-crl"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["ai"] ="application/postscript"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["eps"] ="application/postscript"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["ps"] ="application/postscript"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["rtf"] ="application/rtf"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["setpay"] ="application/set-payment-initiation"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["setreg"] ="application/set-registration-initiation"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["xla"] ="application/vnd.ms-excel"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["xlc"] ="application/vnd.ms-excel"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["xlm"] ="application/vnd.ms-excel"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["xls"] ="application/vnd.ms-excel"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["xlt"] ="application/vnd.ms-excel"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["xlw"] ="application/vnd.ms-excel"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["sst"] ="application/vnd.ms-pkicertstore"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["cat"] ="application/vnd.ms-pkiseccat"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["stl"] ="application/vnd.ms-pkistl"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["pot,"] ="application/vnd.ms-powerpoint"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["pps"] ="application/vnd.ms-powerpoint"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["ppt"] ="application/vnd.ms-powerpoint"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["mpp"] ="application/vnd.ms-project"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["wcm"] ="application/vnd.ms-works"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["wdb"] ="application/vnd.ms-works"; //$NON-NLS-1$   //$NON-NLS-2$    
    mimeMap["wks"] ="application/vnd.ms-works"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["wps"] ="application/vnd.ms-works"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["hlp"] ="application/winhlp"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["bcpio"] ="application/x-bcpio"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["cdf"] ="application/x-cdf"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["z"] ="application/x-compress"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["tgz"] ="application/x-compressed"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["cpio"] ="application/x-cpio"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["csh"] ="application/x-csh"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["dcr"] ="application/x-director"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["dir"] ="application/x-director"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["dxr"] ="application/x-director"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["dvi"] ="application/x-dvi"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["gtar"] ="application/x-gtar"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["gz"] ="application/x-gzip"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["hdf"] ="application/x-hdf"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["ins"] ="application/x-internet-signup"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["isp"] ="application/x-internet-signup"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["iii"] ="application/x-iphone"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["js"] ="application/x-javascript"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["latex"] ="application/x-latex"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["xml"] ="application/xml"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["mdb"] ="application/x-msaccess"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["crd"] ="application/x-mscardfile"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["clp"] ="application/x-msclip"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["dll"] ="application/x-msdownload"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["m13"] ="application/x-msmediaview"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["m14"] ="application/x-msmediaview"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["mvb"] ="application/x-msmediaview"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["wmf"] ="application/x-msmetafile"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["mny"] ="application/x-msmoney"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["pub"] ="application/x-mspublisher"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["scd"] ="application/x-msschedule"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["trm"] ="application/x-msterminal"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["wri"] ="application/x-mswrite"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["pma"] ="application/x-perfmon"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["pmc"] ="application/x-perfmon"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["pml"] ="application/x-perfmon"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["pmr"] ="application/x-perfmon"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["pmw"] ="application/x-perfmon"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["p12"] ="application/x-pkcs12"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["pfx"] ="application/x-pkcs12"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["p7b"] ="application/x-pkcs7-certificates"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["spc"] ="application/x-pkcs7-certificates"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["p7r"] ="application/x-pkcs7-certreqresp"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["p7c"] ="application/x-pkcs7-mime"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["p7m"] ="application/x-pkcs7-mime"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["p7s"] ="application/x-pkcs7-signature"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["sh"] ="application/x-sh"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["shar"] ="application/x-shar"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["swf"] ="application/x-shockwave-flash"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["sit"] ="application/x-stuffit"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["sv4cpio"] ="application/x-sv4cpio"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["sv4crc"] ="application/x-sv4crc"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["tar"] ="application/x-tar"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["tcl"] ="application/x-tcl"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["tex"] ="application/x-tex"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["texi"] ="application/x-texinfo"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["texinfo"] ="application/x-texinfo"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["obj"] ="application/x-tgif"; //$NON-NLS-1$   //$NON-NLS-2$
    mimeMap["roff"] ="application/x-troff"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["t"] ="application/x-troff"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["tr"] ="application/x-troff"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["man"] ="application/x-troff-man"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["me"] ="application/x-troff-me"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["ms"] ="application/x-troff-ms"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["avi"] ="application/x-troff-msvideo"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["ustar"] ="application/x-ustar"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["src"] ="application/x-wais-source"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["cer"] ="application/x-x509-ca-cert"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["crt"] ="application/x-x509-ca-cert"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["der"] ="application/x-x509-ca-cert"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["pko"] ="application/ynd.ms-pkipko"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["zip"] ="application/zip"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["au"] ="audio/basic"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["snd"] ="audio/basic"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["mid"] ="audio/mid"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["rmi"] ="audio/mid"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["mp3"] ="audio/mpeg"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["aif"] ="audio/x-aiff"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["aifc"] ="audio/x-aiff"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["aiff"] ="audio/x-aiff"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["m3u"] ="audio/x-mpegurl"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["ra"] ="audio/x-pn-realaudio"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["ram"] ="audio/x-pn-realaudio"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["wav"] ="audio/x-wav"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["wma"] ="audio/x-ms-wma"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["aac"] ="audio/aac"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["flac"] ="audio/x-flac"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["gsm"] ="audio/x-gsm"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["bmp"] ="image/bmp"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["cod"] ="image/cis-cod"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["gif"] ="image/gif"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["ief"] ="image/ief"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["jp2"] ="image/jp2"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["jpe"] ="image/jpeg"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["jpeg"] ="image/jpeg"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["jpg"] ="image/jpeg"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["jfif"] ="image/pipeg"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["svg"] ="image/svg+xml"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["tif"] ="image/tiff"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["tiff"] ="image/tiff"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["ras"] ="image/x-cmu-raster"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["cmx"] ="image/x-cmx"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["ico"] ="image/x-icon"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["pnm"] ="image/x-portable-anymap"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["pbm"] ="image/x-portable-bitmap"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["pgm"] ="image/x-portable-graymap"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["ppm"] ="image/x-portable-pixmap"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["rgb"] ="image/x-rgb"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["xbm"] ="image/x-xbitmap"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["xpm"] ="image/x-xpixmap"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["xwd"] ="image/x-xwindowdump"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["mht"] ="message/rfc822"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["mhtml"] ="message/rfc822"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["nws"] ="message/rfc822"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["css"] ="text/css"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["csv"] ="text/csv"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["323"] ="text/h323"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["htm"] ="text/html"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["html"] ="text/html"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["stm"] ="text/html"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["uls"] ="text/iuls"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["bas"] ="text/plain"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["c"] ="text/plain"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["h"] ="text/plain"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["txt"] ="text/plain"; //$NON-NLS-1$   //$NON-NLS-2$
    mimeMap["rtx"] ="text/richtext"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["sct"] ="text/scriptlet"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["tsv"] ="text/tab-separated-values"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["htt"] ="text/webviewhtml"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["htc"] ="text/x-component"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["xml"] ="text/xml"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["etx"] ="text/x-setext"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["vcf"] ="text/x-vcard"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["mp2"] ="video/mpeg"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["mpa"] ="video/mpeg"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["mpe"] ="video/mpeg"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["mpeg"] ="video/mpeg"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["mpg"] ="video/mpeg"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["mpv2"] ="video/mpeg"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["mov"] ="video/quicktime"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["qt"] ="video/quicktime"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["lsf"] ="video/x-la-asf"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["lsx"] ="video/x-la-asf"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["asf"] ="video/x-ms-asf"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["asr"] ="video/x-ms-asf"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["asx"] ="video/x-ms-asf"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["avi"] ="video/x-msvideo"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["movie"] ="video/x-sgi-movie"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["flr"] ="x-world/x-vrml"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["vrml"] ="x-world/x-vrml"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["wrl"] ="x-world/x-vrml"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["wrz"] ="x-world/x-vrml"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["xaf"] ="x-world/x-vrml"; //$NON-NLS-1$   //$NON-NLS-2$   
    mimeMap["xof"] ="x-world/x-vrml"; //$NON-NLS-1$   //$NON-NLS-2$ 
    mimeMap["png"] ="image/png"; //$NON-NLS-1$   //$NON-NLS-2$
    mimeMap["mts"] ="video/avchd"; //$NON-NLS-1$   //$NON-NLS-2$
    mimeMap["mod"] ="video/mpeg"; //$NON-NLS-1$   //$NON-NLS-2$
    mimeMap["ptm"] ="application/x-ptm";
	
//}

function FindCategory(fileExt) {
	if(mimeMap[fileExt]==null)
		return "Other";
	else
		fileExt = mimeMap[fileExt];
	
    if (fileExt.indexOf("image/")!=-1) {
        return "Image";
    } else if (fileExt.indexOf("video/")!=-1) {
        return "Video";
    } else if (fileExt.indexOf("audio/")!=-1) {
        return "Audio";
    } else if (fileExt.indexOf("x-tgif")!=-1) {
        return "3D";
    } else if (fileExt.indexOf("text/")!=-1) {
        return "Document";
    } else if (fileExt.indexOf("pdf")!=-1) {
        return "Document";
    } else if (fileExt.indexOf("word")!=-1) {
        return "Document";
    } else if (fileExt.indexOf("powerpoint")!=-1) {
        return "Document";
    } else if (fileExt.indexOf("excel")!=-1) {
        return "Document";
    } else {
        return "Other";
    }
}

//FindCategory.prototype = new SetMimeMap;
package edu.illinois.ncsa.mmdb.web.server;

import java.net.FileNameMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.TripleWriter;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.Triple;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.util.Tuple;

public class MimeMap implements FileNameMap {
    private static Log                log            = LogFactory.getLog(MimeMap.class);

    public static final String        UNKNOWN_TYPE   = "application/octet-stream";      //$NON-NLS-1$
    public static final String        UNKNOWN_EXT    = "unk";                           //$NON-NLS-1$

    // mime tags
    public static final Resource      MIME_CLASS     = Cet.cet("Mime");                 //$NON-NLS-1$
    public static final Resource      MIME_MAJOR     = Cet.cet("/mime/major");          //$NON-NLS-1$
    public static final Resource      MIME_MINOR     = Cet.cet("/mime/minor");          //$NON-NLS-1$
    public static final Resource      MIME_FILE_EXT  = Cet.cet("/mime/extension");      //$NON-NLS-1$
    public static final Resource      MIME_FILE_DFLT = Cet.cet("/mime/default");        //$NON-NLS-1$

    // maps
    private final Map<String, String> type;
    private final Map<String, String> ext;

    private final Context             context;

    public MimeMap() {
        this(null);
    }

    public MimeMap(Context context) {
        this.context = context;

        type = new HashMap<String, String>();
        ext = new HashMap<String, String>();

        initializeMaps();

        if (context != null) {
            Unifier uf = new Unifier();
            uf.addPattern("mime", Rdf.TYPE, MIME_CLASS); //$NON-NLS-1$
            uf.addPattern("mime", Dc.FORMAT, "format"); //$NON-NLS-2$
            uf.addPattern("mime", MIME_FILE_EXT, "ext");
            uf.addPattern("mime", MIME_FILE_DFLT, "dflt", true);
            uf.setColumnNames("mime", "format", "ext", "dflt");
            try {
                context.perform(uf);
            } catch (OperatorException e) {
                log.error("Could not load map from context.", e);
            }
            for (Tuple<Resource> row : uf.getResult() ) {
                String type = row.get(1).getString().toLowerCase();
                String ext = row.get(2).getString().toLowerCase();
                String dflt = (row.get(3) != null) ? row.get(3).getString().toLowerCase() : ""; //$NON-NLS-1$
                addMimeTypeMap(ext, type, ext.equals(dflt));
            }
        }
    }

    /**
     * Are new context types added persistant?
     * 
     * @return
     */
    public boolean isPersistant() {
        return context != null;
    }

    /**
     * Gets the MIME type for the specified file name.
     * 
     * @param fileName
     *            the specified file name
     * @return a <code>String</code> indicating the MIME
     *         type for the specified file name.
     */
    public String getContentTypeFor(String fileName) {
        int end = fileName.lastIndexOf('#');
        if (end == -1) {
            end = fileName.length();
        }
        int start = fileName.lastIndexOf('.', end);
        if (start == -1) {
            start = 0;
        }

        String mimeType = type.get(fileName.substring(start, end));
        if (mimeType != null) {
            mimeType = UNKNOWN_TYPE;
        }
        return mimeType;
    }

    /**
     * Given the MIME type return the best extension.
     * 
     * @param type
     * @return
     */
    public String getFileExtention(String mimeType) {
        if (ext.get(mimeType) != null) {
            return "." + ext.get(mimeType);
        }
        for (Entry<String, String> entry : type.entrySet() ) {
            if (entry.getValue().equals(mimeType)) {
                return "." + entry.getKey();
            }
        }
        return "." + UNKNOWN_EXT;
    }

    public void removeMimeType(String extension) {
        String mimeType = type.remove(extension);
        if (mimeType != null) {
            if (ext.get(mimeType).equals(extension)) {
                ext.remove(mimeType);
            }
        }
    }

    public void addMimeType(String extension, String mimeType) {
        addMimeType(extension, mimeType, false);
    }

    /**
     * Add a new MIME type and extension. This is persisted to the context (if
     * one
     * exists).
     * 
     * @param ext
     * @param type
     * @param isDefault
     */
    public void addMimeType(String extension, String mimeType, boolean isDefault) {
        mimeType = mimeType.toLowerCase();
        extension = extension.toLowerCase();
        addMimeTypeMap(extension, mimeType, isDefault);

        // write to context
        TripleWriter tw = new TripleWriter();
        Resource r = Cet.cet("mime/type#" + mimeType);
        tw.add(Triple.create(r, Rdf.TYPE, MIME_CLASS));
        tw.add(Triple.create(r, Dc.FORMAT, mimeType));
        tw.add(Triple.create(r, MIME_FILE_EXT, extension));
        if (isDefault) {
            tw.add(Triple.create(r, MIME_FILE_DFLT, extension));
        }
        String[] parts = mimeType.split("[/ ]"); //$NON-NLS-1$
        if (parts.length > 1) {
            tw.add(Triple.create(r, MIME_MAJOR, parts[0]));
            tw.add(Triple.create(r, MIME_MINOR, parts[1]));
        }
    }

    /**
     * Add a new entry to the map.
     * 
     * @param extension
     * @param mimeType
     * @param isDefault
     */
    private void addMimeTypeMap(String extension, String mimeType, boolean isDefault) {
        // add to map
        String old = type.put(extension, mimeType);
        if (old != null) {
            log.info(String.format("Replaced exisitng mimetype (%s) for ext %s.", old, extension));
        }

        // store default ext
        if (isDefault) {
            this.ext.put(mimeType, extension);
        }
    }

    /**
     * Large list of default mime-types
     */
    private void initializeMaps() {
        addMimeTypeMap(UNKNOWN_EXT, UNKNOWN_TYPE, false);

        // add some common mime types
        addMimeTypeMap("evy", "application/envoy", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("fif", "application/fractals", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("spl", "application/futuresplash", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("hta", "application/hta", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("acx", "application/internet-property-stream", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("hqx", "application/mac-binhex40", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("doc", "application/msword", true); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("dot", "application/msword", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("bin", "application/octet-stream", true); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("class", "application/octet-stream", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("dms", "application/octet-stream", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("exe", "application/octet-stream", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("lha", "application/octet-stream", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("lzh", "application/octet-stream", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("oda", "application/oda", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("axs", "application/olescript", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("pdf", "application/pdf", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("prf", "application/pics-rules", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("p10", "application/pkcs10", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("crl", "application/pkix-crl", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("ai", "application/postscript", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("eps", "application/postscript", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("ps", "application/postscript", true); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("rtf", "application/rtf", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("setpay", "application/set-payment-initiation", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("setreg", "application/set-registration-initiation", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("xla", "application/vnd.ms-excel", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("xlc", "application/vnd.ms-excel", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("xlm", "application/vnd.ms-excel", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("xls", "application/vnd.ms-excel", true); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("xlt", "application/vnd.ms-excel", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("xlw", "application/vnd.ms-excel", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("sst", "application/vnd.ms-pkicertstore", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("cat", "application/vnd.ms-pkiseccat", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("stl", "application/vnd.ms-pkistl", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("pot,", "application/vnd.ms-powerpoint", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("pps", "application/vnd.ms-powerpoint", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("ppt", "application/vnd.ms-powerpoint", true); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("mpp", "application/vnd.ms-project", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("wcm", "application/vnd.ms-works", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("wdb", "application/vnd.ms-works", false); //$NON-NLS-1$   //$NON-NLS-2$    
        addMimeTypeMap("wks", "application/vnd.ms-works", true); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("wps", "application/vnd.ms-works", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("hlp", "application/winhlp", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("bcpio", "application/x-bcpio", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("cdf", "application/x-cdf", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("z", "application/x-compress", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("tgz", "application/x-compressed", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("cpio", "application/x-cpio", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("csh", "application/x-csh", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("dcr", "application/x-director", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("dir", "application/x-director", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("dxr", "application/x-director", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("dvi", "application/x-dvi", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("gtar", "application/x-gtar", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("gz", "application/x-gzip", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("hdf", "application/x-hdf", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("ins", "application/x-internet-signup", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("isp", "application/x-internet-signup", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("iii", "application/x-iphone", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("js", "application/x-javascript", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("latex", "application/x-latex", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("xml", "application/xml", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("mdb", "application/x-msaccess", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("crd", "application/x-mscardfile", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("clp", "application/x-msclip", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("dll", "application/x-msdownload", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("m13", "application/x-msmediaview", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("m14", "application/x-msmediaview", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("mvb", "application/x-msmediaview", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("wmf", "application/x-msmetafile", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("mny", "application/x-msmoney", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("pub", "application/x-mspublisher", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("scd", "application/x-msschedule", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("trm", "application/x-msterminal", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("wri", "application/x-mswrite", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("pma", "application/x-perfmon", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("pmc", "application/x-perfmon", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("pml", "application/x-perfmon", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("pmr", "application/x-perfmon", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("pmw", "application/x-perfmon", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("p12", "application/x-pkcs12", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("pfx", "application/x-pkcs12", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("p7b", "application/x-pkcs7-certificates", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("spc", "application/x-pkcs7-certificates", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("p7r", "application/x-pkcs7-certreqresp", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("p7c", "application/x-pkcs7-mime", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("p7m", "application/x-pkcs7-mime", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("p7s", "application/x-pkcs7-signature", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("sh", "application/x-sh", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("shar", "application/x-shar", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("swf", "application/x-shockwave-flash", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("sit", "application/x-stuffit", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("sv4cpio", "application/x-sv4cpio", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("sv4crc", "application/x-sv4crc", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("tar", "application/x-tar", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("tcl", "application/x-tcl", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("tex", "application/x-tex", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("texi", "application/x-texinfo", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("texinfo", "application/x-texinfo", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("roff", "application/x-troff", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("t", "application/x-troff", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("tr", "application/x-troff", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("man", "application/x-troff-man", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("me", "application/x-troff-me", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("ms", "application/x-troff-ms", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("ustar", "application/x-ustar", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("src", "application/x-wais-source", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("cer", "application/x-x509-ca-cert", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("crt", "application/x-x509-ca-cert", true); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("der", "application/x-x509-ca-cert", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("pko", "application/ynd.ms-pkipko", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("zip", "application/zip", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("au", "audio/basic", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("snd", "audio/basic", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("mid", "audio/mid", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("rmi", "audio/mid", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("mp3", "audio/mpeg", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("aif", "audio/x-aiff", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("aifc", "audio/x-aiff", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("aiff", "audio/x-aiff", true); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("m3u", "audio/x-mpegurl", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("ra", "audio/x-pn-realaudio", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("ram", "audio/x-pn-realaudio", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("wav", "audio/x-wav", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("bmp", "image/bmp", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("cod", "image/cis-cod", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("gif", "image/gif", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("ief", "image/ief", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("jp2", "image/jp2", true); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("jpe", "image/jpeg", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("jpeg", "image/jpeg", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("jpg", "image/jpeg", true); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("jfif", "image/pipeg", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("svg", "image/svg+xml", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("tif", "image/tiff", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("tiff", "image/tiff", true); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("ras", "image/x-cmu-raster", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("cmx", "image/x-cmx", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("ico", "image/x-icon", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("pnm", "image/x-portable-anymap", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("pbm", "image/x-portable-bitmap", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("pgm", "image/x-portable-graymap", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("ppm", "image/x-portable-pixmap", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("rgb", "image/x-rgb", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("xbm", "image/x-xbitmap", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("xpm", "image/x-xpixmap", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("xwd", "image/x-xwindowdump", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("mht", "message/rfc822", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("mhtml", "message/rfc822", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("nws", "message/rfc822", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("css", "text/css", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("csv", "text/csv", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("323", "text/h323", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("htm", "text/html", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("html", "text/html", true); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("stm", "text/html", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("uls", "text/iuls", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("bas", "text/plain", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("c", "text/plain", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("h", "text/plain", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("txt", "text/plain", true); //$NON-NLS-1$   //$NON-NLS-2$
        addMimeTypeMap("rtx", "text/richtext", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("sct", "text/scriptlet", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("tsv", "text/tab-separated-values", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("htt", "text/webviewhtml", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("htc", "text/x-component", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("xml", "text/xml", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("etx", "text/x-setext", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("vcf", "text/x-vcard", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("mp2", "video/mpeg", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("mpa", "video/mpeg", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("mpe", "video/mpeg", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("mpeg", "video/mpeg", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("mpg", "video/mpeg", true); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("mpv2", "video/mpeg", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("mov", "video/quicktime", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("qt", "video/quicktime", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("lsf", "video/x-la-asf", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("lsx", "video/x-la-asf", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("asf", "video/x-ms-asf", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("asr", "video/x-ms-asf", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("asx", "video/x-ms-asf", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("avi", "video/x-msvideo", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("movie", "video/x-sgi-movie", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("flr", "x-world/x-vrml", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("vrml", "x-world/x-vrml", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("wrl", "x-world/x-vrml", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("wrz", "x-world/x-vrml", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("xaf", "x-world/x-vrml", false); //$NON-NLS-1$   //$NON-NLS-2$   
        addMimeTypeMap("xof", "x-world/x-vrml", false); //$NON-NLS-1$   //$NON-NLS-2$   
    }
}

package edu.illinois.ncsa.mmdb.web.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ExtractorJob implements Runnable {

    /** Commons logging **/
    private static Log log  = LogFactory.getLog(ExtractorJob.class);

    String             uri;
    String             server;
    String             stringContext;
    boolean            rerun;
    Properties         mongoProps;
    static Set<String> uris = new HashSet<String>();

    public ExtractorJob(String uri, boolean rerun, String server, String stringContext, Properties mongoProps) {
        this.uri = uri;
        this.rerun = rerun;
        this.server = server;
        this.mongoProps = mongoProps;
        this.stringContext = stringContext;
        uris.add(uri);
    }

    public static boolean isPending(String uri) {
        return uris.contains(uri);
    }

    @Override
    public void run() {
        OutputStreamWriter wr = null;
        BufferedReader rd = null;
        try {
            StringBuilder sb = new StringBuilder();

            // create the body of the message
            sb.append("context="); //$NON-NLS-1$
            sb.append(stringContext);
            sb.append("&dataset="); //$NON-NLS-1$
            sb.append(URLEncoder.encode(uri, "UTF-8")); //$NON-NLS-1$

            if (mongoProps != null) {
                sb.append("&mongo=");
                StringWriter writer = new StringWriter();
                mongoProps.store(writer, "MongoDB Properties");
                sb.append(URLEncoder.encode(writer.toString(), "UTF-8"));
                writer.close();
            }

            if (rerun) {
                sb.append("&removeOld=true"); //$NON-NLS-1$
            }

            // launch the job
            if (!server.endsWith("/")) { //$NON-NLS-1$
                server += "/"; //$NON-NLS-1$
            }

            server += "extractor/extract"; //$NON-NLS-1$

            URL url = new URL(server);
            URLConnection conn = url.openConnection();

            conn.setReadTimeout(5000);
            if (conn.getReadTimeout() != 5000) {
                log.info("Could not set read timeout! (set to " + conn.getReadTimeout() + ").");
            }

            // send post
            conn.setDoOutput(true);
            wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(sb.toString());
            wr.flush();
            wr.close();

            // Get the response
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            sb = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                log.debug(line);
                sb.append(line);
                sb.append("\n"); //$NON-NLS-1$
            }
            rd.close();

        } catch (Exception e) {
            log.error(String.format("Extraction service ( %s ) unavailable", server) + ": " + e.getMessage());
        } finally {
            if (rd != null) {
                try {
                    rd.close();
                } catch (IOException e) {
                    log.error("Could not close readstream.", e);
                }
            }
            if (wr != null) {
                try {
                    wr.close();
                } catch (IOException e) {
                    log.error("Could not close writestream.", e);
                }
            }
            uris.remove(uri);
        }

        log.debug("EXTRACT PREVIEWS " + uri + " DONE");
    }
}

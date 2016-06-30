package org.nds.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.jena.riot.RDFDataMgr;
import org.tupeloproject.rdf.Literal;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.UriRef;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class IDMediator {

    public static void main(String[] args) {

        InputStream is;
        try {
            is = getMetadataStream(new URL(
                    "http://doi.org/10.13012/J8MW2F2Q"));
            File f = new File("temp.ttl");
            writeFile(f, is);
            Map<String, Object> result = new LinkedHashMap<String, Object>();
            getMetadata(
                    "http://doi.org/10.13012/J8MW2F2Q", result);
            System.out.println(result.toString());
            InputStream is2 = getDataStream(new URL(
                    "http://doi.org/10.13012/J8MW2F2Q"));
            File f2 = new File("data.bin");
            writeFile(f2, is2);
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static void getMetadata(String uri, Map<String, Object> result) {
        // Create a model and read into it from file
        // "data.ttl" assumed to be Turtle.

        Model model = RDFDataMgr.loadModel(uri);//.loadModel("temp.ttl");
        StmtIterator i = model.listStatements();
        while (i.hasNext()) {
            Statement s = i.nextStatement();
            UriRef sub = new UriRef(s.getSubject().getURI());
            UriRef pred = new UriRef(s.getPredicate().getURI());
            Resource obj = null;
            if (s.getObject().isLiteral()) {
                obj = new Literal(s.getObject().toString());
            } else if (s.getObject().isURIResource()) {
                obj = new UriRef(s.getObject().toString());
            }
            result.put(pred.toString(), obj.toString());

        }
    }

    public InputStream getData(String uri) {
        try {
            return getDataStream(new URL(uri));
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    private static void writeFile(File f, InputStream is) {
        OutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(f);

            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = is.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }

            System.out.println(f.getName() + " written");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    // outputStream.flush();
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    public static InputStream getDataStream(URL url) throws IOException {

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000);
        conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");

        boolean redirect = false;

        // normally, 3xx is redirect
        int status = conn.getResponseCode();
        if (status != HttpURLConnection.HTTP_OK) {
            if (status == HttpURLConnection.HTTP_MOVED_TEMP
                    || status == HttpURLConnection.HTTP_MOVED_PERM
                    || status == HttpURLConnection.HTTP_SEE_OTHER) {
                redirect = true;
            }
        }

        if (redirect) {

            // get redirect url from "location" header field
            String newUrl = conn.getHeaderField("Location");

            // If the first site has set a cookie, pass it on in the redirect
            String cookies = conn.getHeaderField("Set-Cookie");

            // open the new connection
            conn = (HttpURLConnection) new URL(newUrl).openConnection();
            conn.setRequestProperty("Cookie", cookies);
            conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
            status = conn.getResponseCode();

        }
        if (status == HttpURLConnection.HTTP_OK) {
            return conn.getInputStream();
        } else {
            throw new IOException("Remote server response for "
                    + conn.getURL().toExternalForm() + " : " + status);
        }
    }

    private static InputStream getMetadataStream(URL url) throws IOException {

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000);
        conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
        conn.addRequestProperty("Accept", "text/turtle");

        boolean redirect = false;

        // normally, 3xx is redirect
        int status = conn.getResponseCode();
        if (status != HttpURLConnection.HTTP_OK) {
            if (status == HttpURLConnection.HTTP_MOVED_TEMP
                    || status == HttpURLConnection.HTTP_MOVED_PERM
                    || status == HttpURLConnection.HTTP_SEE_OTHER) {
                redirect = true;
            }
        }

        if (redirect) {

            // get redirect url from "location" header field
            String newUrl = conn.getHeaderField("Location");

            // If the first site has set a cookie, pass it on in the redirect
            String cookies = conn.getHeaderField("Set-Cookie");

            // open the new connection
            conn = (HttpURLConnection) new URL(newUrl).openConnection();
            conn.setRequestProperty("Cookie", cookies);
            conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
            status = conn.getResponseCode();

        }
        if (status == HttpURLConnection.HTTP_OK) {
            return conn.getInputStream();
        } else {
            throw new IOException("Remote server response for "
                    + conn.getURL().toExternalForm() + " : " + status);
        }
    }

}

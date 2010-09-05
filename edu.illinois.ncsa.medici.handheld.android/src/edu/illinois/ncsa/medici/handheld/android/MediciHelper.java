package edu.illinois.ncsa.medici.handheld.android;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.util.EncodingUtil;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class MediciHelper {
    public static void uploadData(Activity activity, Uri uri, String title) {
        // make sure there is an image
        if (uri == null) {
            Toast.makeText(activity, "No data selected.", Toast.LENGTH_SHORT).show();
            return;
        }

        // what's the caption?
        if ((title == null) || title.equals("")) {
            title = uri.getLastPathSegment();
        }

        // get medici url, username, password from preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        String server = prefs.getString("server", null);
        String username = prefs.getString("username", null);
        String password = prefs.getString("password", null);
        if ((server == null) || server.equals("") || (username == null) || username.equals("") || (password == null) || password.equals("")) {
            Intent i = new Intent(activity, Preferences.class);
            activity.startActivity(i);
            Toast.makeText(activity, "Invalid preferences.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!server.endsWith("/")) {
            server = server + "/";
        }

        // setup connection
        URL url;
        try {
            url = new URL(server);
        } catch (MalformedURLException e) {
            Toast.makeText(activity, "Invalid server URL.", Toast.LENGTH_SHORT).show();
            return;
        }
        String host = url.getHost();
        int port = url.getPort();
        Credentials upc = new UsernamePasswordCredentials(username, password);

        // create the photo part
        Part photo = new UriPart(activity, "data", title, uri);

        // create the post
        PostMethod post = new PostMethod(server + "UploadBlob");
        post.setRequestEntity(new MultipartRequestEntity(new Part[] { photo }, post.getParams()));

        // upload data
        HttpClient client = new HttpClient();
        client.getParams().setAuthenticationPreemptive(true);
        client.getState().setCredentials(new AuthScope(host, port, AuthScope.ANY_REALM), upc);
        try {
            int i = client.executeMethod(post);
            Toast.makeText(activity, "post returned " + i, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.w(Medici.TAG, "Post", e);
        }
    }

    static class UriPart extends Part {
        private final Uri      uri;
        private final String   title;
        private final String   name;
        private final Activity activity;

        public UriPart(Activity activity, String name, String title, Uri uri) {
            this.activity = activity;
            this.name = name;
            this.title = title;
            this.uri = uri;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getCharSet() {
            return "ISO-8859-1";
        }

        @Override
        public String getTransferEncoding() {
            return "binary";
        }

        /**
         * Write the disposition header to the output stream
         * 
         * @param out
         *            The output stream
         * @throws IOException
         *             If an IO problem occurs
         * @see Part#sendDispositionHeader(OutputStream)
         */
        protected void sendDispositionHeader(OutputStream out) throws IOException {
            super.sendDispositionHeader(out);
            if (title != null) {
                out.write(EncodingUtil.getAsciiBytes("; filename="));
                out.write(QUOTE_BYTES);
                out.write(EncodingUtil.getAsciiBytes(title));
                out.write(QUOTE_BYTES);
            }
        }

        @Override
        public String getContentType() {
            return activity.getContentResolver().getType(uri);
        }

        @Override
        protected long lengthOfData() throws IOException {
            AssetFileDescriptor fd = activity.getContentResolver().openAssetFileDescriptor(uri, "r");
            long length = fd.getLength();
            fd.close();
            return length;
        }

        @Override
        protected void sendData(OutputStream os) throws IOException {
            byte[] buf = new byte[8192];
            int len = 0;
            AssetFileDescriptor fd = activity.getContentResolver().openAssetFileDescriptor(uri, "r");
            InputStream is = fd.createInputStream();
            while ((len = is.read(buf)) > 0) {
                os.write(buf, 0, len);
            }
            is.close();
            fd.close();
        }
    }

}

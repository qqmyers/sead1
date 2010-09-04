package edu.illinois.ncsa.medici.handheld.android;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.util.EncodingUtil;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class Medici extends Activity {
    public static final String TAG            = "Medici";

    private static final int   TAKE_PICTURE   = 1;
    private static final int   SELECT_PICTURE = 2;

    private Uri                selectedImageUri;

    private Uri                imageUri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.medici);

        ((Button) findViewById(R.id.btnUpload)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                upload();
            }
        });
    }

    // ----------------------------------------------------------------------
    // ACTIVITY RESULTS
    // ----------------------------------------------------------------------
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == TAKE_PICTURE) {
                selectedImageUri = imageUri;
            }
            if (requestCode == SELECT_PICTURE) {
                selectedImageUri = data.getData();
            }

            Toast.makeText(Medici.this, "Selected image " + selectedImageUri, Toast.LENGTH_LONG).show();
            if (selectedImageUri != null) {
                ((ImageView) findViewById(R.id.imgPreview)).setImageURI(selectedImageUri);
                ((EditText) findViewById(R.id.title)).setText(selectedImageUri.getLastPathSegment());
            }
        }
    }

    // ----------------------------------------------------------------------
    // OPTIONS MENU
    // ----------------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        switch (item.getItemId()) {
            case R.id.mnuTakePicture:
                //define the file-name to save photo taken by Camera activity
                String fileName = new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss.'jpg'").format(new Date());
                //create parameters for Intent with filename
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, fileName);
                values.put(MediaStore.Images.Media.CONTENT_TYPE, "JPEG");
                values.put(MediaStore.Images.Media.DESCRIPTION, "Medici Capture");
                //imageUri is the current activity attribute, define and save it for later usage (also in onSaveInstanceState)
                imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                //create new Intent
                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                startActivityForResult(intent, TAKE_PICTURE);
                return true;

            case R.id.mnuGallery:
                intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
                return true;

            case R.id.mnuPreferences:
                intent = new Intent(Medici.this, Preferences.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // ----------------------------------------------------------------------
    // ACTUAL UPLOAD CODE
    // ----------------------------------------------------------------------
    private void upload() {
        // make sure there is an image
        if (selectedImageUri == null) {
            Toast.makeText(Medici.this, "No image selected.", Toast.LENGTH_SHORT).show();
            return;
        }

        // what's the caption?
        String title = ((EditText) findViewById(R.id.title)).getText().toString();
        if ((title == null) || title.equals("")) {
            title = selectedImageUri.getLastPathSegment();
        }

        // get medici url, username, password from preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String server = prefs.getString("server", null);
        String username = prefs.getString("username", null);
        String password = prefs.getString("password", null);
        if ((server == null) || (username == null) || (password == null)) {
            Intent i = new Intent(Medici.this, Preferences.class);
            startActivity(i);
            Toast.makeText(Medici.this, "Invalid preferences.", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(Medici.this, "Invalid server URL.", Toast.LENGTH_SHORT).show();
            return;
        }
        String host = url.getHost();
        int port = url.getPort();
        Credentials upc = new UsernamePasswordCredentials(username, password) {
            @Override
            public String getUserName() {
                Log.i(TAG, "username = " + super.getUserName());
                return super.getUserName();
            }
        };

        // create the photo part
        Part photo = new UriPart("data", title, selectedImageUri);

        // create the post
        PostMethod post = new PostMethod(server + "UploadBlob");
        post.setRequestEntity(new MultipartRequestEntity(new Part[] { photo }, post.getParams()));

        // upload data
        HttpClient client = new HttpClient();
        client.getParams().setAuthenticationPreemptive(true);
        client.getState().setCredentials(new AuthScope(host, port, AuthScope.ANY_REALM), upc);
        try {
            int i = client.executeMethod(post);
            Toast.makeText(Medici.this, "post returned " + i, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.w(TAG, "Post", e);
        }
    }

    class UriPart extends Part {
        private final Uri    uri;
        private final String title;
        private final String name;

        public UriPart(String name, String title, Uri uri) {
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
            return getContentResolver().getType(uri);
        }

        @Override
        protected long lengthOfData() throws IOException {
            AssetFileDescriptor fd = getContentResolver().openAssetFileDescriptor(uri, "r");
            long length = fd.getLength();
            fd.close();
            return length;
        }

        @Override
        protected void sendData(OutputStream os) throws IOException {
            byte[] buf = new byte[8192];
            int len = 0;
            AssetFileDescriptor fd = getContentResolver().openAssetFileDescriptor(uri, "r");
            InputStream is = fd.createInputStream();
            while ((len = is.read(buf)) > 0) {
                os.write(buf, 0, len);
            }
            is.close();
            fd.close();
        }
    }
}
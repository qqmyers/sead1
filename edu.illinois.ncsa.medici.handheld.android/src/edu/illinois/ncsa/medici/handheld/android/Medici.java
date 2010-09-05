package edu.illinois.ncsa.medici.handheld.android;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

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
                String title = ((EditText) findViewById(R.id.title)).getText().toString();
                MediciHelper.uploadData(Medici.this, selectedImageUri, title);
            }
        });

        // check to see if this is in response to sendto
        if (Intent.ACTION_SEND.equals(getIntent().getAction())) {
            if (getIntent().getExtras().get(Intent.EXTRA_STREAM) != null) {
                showPicture((Uri) getIntent().getExtras().get(Intent.EXTRA_STREAM));
            } else if (getIntent().getData() != null) {
                showPicture(getIntent().getData());
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
                Date now = new Date();
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss").format(now));
                values.put(MediaStore.Images.Media.DESCRIPTION, "Medici Capture");
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.Images.Media.DATE_ADDED, now.getTime());
                values.put(MediaStore.Images.Media.DATE_TAKEN, now.getTime());
                imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
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
    // ACTIVITY RESULTS
    // ----------------------------------------------------------------------
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == TAKE_PICTURE) {
                showPicture(imageUri);
            } else if (requestCode == SELECT_PICTURE) {
                showPicture(data.getData());
            }
        }
    }

    private void showPicture(Uri uri) {
        selectedImageUri = uri;
        if (selectedImageUri != null) {
            ((ImageView) findViewById(R.id.imgPreview)).setImageURI(selectedImageUri);
            ((EditText) findViewById(R.id.title)).setText(selectedImageUri.getLastPathSegment());
        }
    }
}
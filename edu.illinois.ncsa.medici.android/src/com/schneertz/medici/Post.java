package com.schneertz.medici;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.methods.multipart.PartSource;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.schneertz.medici.R;

public class Post extends Activity {
	Uri imageUri;

	void updateProgress(final int what) {
		runOnUiThread(new Runnable() {
			public void run() {
				Button postButton = (Button) findViewById(R.id.postButton);
				ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
				TextView statusMessage = (TextView) findViewById(R.id.statusMessage);
				if (what == -1) { // error
					statusMessage.setText("Error uploading");
					statusMessage.invalidate();
				} else if (what == 0) { // reset
					postButton.setEnabled(true);
					postButton.invalidate();
					progressBar.setVisibility(View.INVISIBLE);
					progressBar.setProgress(0);
					progressBar.invalidate();
					statusMessage.setText("");
					statusMessage.invalidate();
				} else { // % progress
					postButton.setEnabled(false);
					postButton.invalidate();
					progressBar.setVisibility(View.VISIBLE);
					progressBar.setProgress(what);
					progressBar.invalidate();
					statusMessage.setText("Uploading ... " + what
							+ "% complete");
					statusMessage.invalidate();
				}
			}
		});
	}

	void asyncPost(final Uri stream) {
		updateProgress(1);
		// what's the caption?
		EditText captionText = (EditText) findViewById(R.id.captionText);
		final String captionString = captionText.getText().toString();

		// get medici url, username, password from preferences
		String medici = PreferenceManager.getDefaultSharedPreferences(
				Post.this).getString("medici", null);
		String username = PreferenceManager.getDefaultSharedPreferences(
				Post.this).getString("username", null);
		String password = PreferenceManager.getDefaultSharedPreferences(
				Post.this).getString("password", null);

		updateProgress(2);

		PhotoPost post = new PhotoPost();
		post.setCreds(username,password);
		post.setText(captionString);
		post.setMedici(medici);
		post.setContentType(getContentResolver().getType(stream));
		try {
			ParcelFileDescriptor fd = getContentResolver().openFileDescriptor(
					stream, "r");
			final long length = fd.getStatSize();
			fd.close();
			updateProgress(3);
			post.setSource(new PartSource() {
				public InputStream createInputStream() throws IOException {
					return getContentResolver().openInputStream(stream);
				}
				public String getFileName() {
					return captionString;
				}
				public long getLength() {
					return length;
				}
			});
			post.asyncPost(new ProgressHandler() {
				public void onProgress(int k) {
					updateProgress(k);
				}
				// FIXME handle errors too
			});
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
		}
	}

	void handleIntent(Intent intent) {
		// the picture to post
		imageUri = (Uri) getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
		if (imageUri == null) {
			imageUri = getIntent().getData();
		}
		runOnUiThread(new Runnable() {
			public void run() {
				ImageView thumbnail = (ImageView) findViewById(R.id.thumbnail);
				thumbnail.setImageURI(imageUri);
				thumbnail.invalidate();
			}
		});
	}

	@Override
	protected void onNewIntent(Intent intent) {
		handleIntent(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.post);

		handleIntent(getIntent());

		Button postButton = (Button) findViewById(R.id.postButton);
		postButton.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				asyncPost(imageUri);
			}
		});

		updateProgress(0); // not doing anything; hide the progress bar
	}
}

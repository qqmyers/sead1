package com.schneertz.medici;

import com.schneertz.medici.R;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Main extends Activity {
	final int CHOOSE_REQUEST_CODE = 0;
	final int PREFERENCES_REQUEST_CODE = 1;
	
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_menu, menu);
	    return true;
	}

	
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.mm_prefs) {
			startActivityForResult(new Intent(this, Preferences.class), PREFERENCES_REQUEST_CODE);
		} else if(item.getItemId() == R.id.mm_quit) {
			finish();
		} else if(item.getItemId() == R.id.mm_choose) {
			chooseImage();
		}
		return true;
	}
    
    void chooseImage() {
		Intent i = new Intent(Intent.ACTION_GET_CONTENT);
		i.setType("image/*");
		startActivityForResult(i,CHOOSE_REQUEST_CODE);
    }
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if(requestCode == CHOOSE_REQUEST_CODE) {
    		Intent i = new Intent(Main.this, Post.class);
    		i.setData(Uri.parse(data.getDataString()));
    		startActivity(i);
    	}
	}

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Button button = (Button) findViewById(R.id.preferencesButton);
        button.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				startActivityForResult(new Intent(Main.this, Preferences.class), PREFERENCES_REQUEST_CODE);
			}
        });
        button = (Button) findViewById(R.id.mainPostButton);
        button.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				chooseImage();
			}
        });
    }
}
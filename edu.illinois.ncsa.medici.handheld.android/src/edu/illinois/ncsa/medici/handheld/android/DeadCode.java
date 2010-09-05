package edu.illinois.ncsa.medici.handheld.android;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.EditText;

public class DeadCode extends Activity {

    public static File convertImageUriToFile(Uri imageUri, Activity activity) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID, MediaStore.Images.ImageColumns.ORIENTATION };
            cursor = activity.managedQuery(imageUri,
                    proj, // Which columns to return
                    null, // WHERE clause; which rows to return (all rows)
                    null, // WHERE clause selection arguments (none)
                    null); // Order-by clause (ascending by name)
            int file_ColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            int orientation_ColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION);
            if (cursor.moveToFirst()) {
                String orientation = cursor.getString(orientation_ColumnIndex);
                return new File(cursor.getString(file_ColumnIndex));
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private void findLocation() {
        // Acquire a reference to the system Location Manager
        final LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // find best one
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        final String best = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(best);

        if ((System.currentTimeMillis() - location.getTime()) > 2 * 60000) {
            // Define a listener that responds to location updates
            LocationListener locationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    locationManager.removeUpdates(this);

                    // Called when a new location is found by the network location provider.
                    StringBuilder sb = new StringBuilder();
                    for (String s : locationManager.getAllProviders() ) {
                        sb.append(s);
                        sb.append("\n");
                    }
                    sb.append("--\n");
                    sb.append(best);
                    sb.append(" (refresh)\n--\n");
                    sb.append(System.currentTimeMillis() - location.getTime());
                    sb.append("\n--\n");
                    sb.append(location);

                    ((EditText) findViewById(R.id.debug)).setText(sb.toString());
                }

                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                public void onProviderEnabled(String provider) {
                }

                public void onProviderDisabled(String provider) {
                }
            };

            // Register the listener with the Location Manager to receive location updates
            locationManager.requestLocationUpdates(best, 0, 0, locationListener);
        } else {
            StringBuilder sb = new StringBuilder();
            for (String s : locationManager.getAllProviders() ) {
                sb.append(s);
                sb.append("\n");
            }
            sb.append("--\n");
            sb.append(best);
            sb.append("\n--\n");
            sb.append(System.currentTimeMillis() - location.getTime());
            sb.append("\n--\n");
            sb.append(location);

            ((EditText) findViewById(R.id.debug)).setText(sb.toString());
        }

    }

}

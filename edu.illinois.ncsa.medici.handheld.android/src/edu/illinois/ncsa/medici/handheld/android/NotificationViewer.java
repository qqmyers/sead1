package edu.illinois.ncsa.medici.handheld.android;

import android.app.Activity;
import android.os.Bundle;

public class NotificationViewer extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_viewer);

        //NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //notificationManager.cancel(MediciUploadService.NOTIFICATION_ID);
    }
}
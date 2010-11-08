package edu.illinois.ncsa.medici.handheld.android;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.widget.RemoteViews;

public class MediciUploadService extends Service {
    private static final String TAG             = "MediciUploadService";
    private static final String BOUNDRY         = "==================================";

    public static final int     NOTIFICATION_ID = 1001;

    public static final String  ACTION_UPLOAD   = "medici.action.upload";
    public static final String  EXTRA_USERNAME  = "medici.extra.username";
    public static final String  EXTRA_PASSWORD  = "medici.extra.password";
    public static final String  EXTRA_CAPTION   = "medici.extra.caption";
    public static final String  EXTRA_SERVER    = "medici.extra.server";

    private NotificationManager notificationManager;
    private RemoteViews         contentView;
    private Notification        notification;
    private ServiceHandler      serviceHandler;
    private Looper              serviceLooper;
    private Handler             handler;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        // main UI handler
        handler = new Handler();

        // thread handling uploads
        HandlerThread thread = new HandlerThread("Medici Upload Service", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);

        // notification service
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // create custom view
        contentView = new RemoteViews(getPackageName(), R.layout.notification);

        // create clickhandler
        Intent notificationIntent = new Intent(this, NotificationViewer.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        // create the notification
        notification = new Notification(R.drawable.icon_medici, "Medici Upload Service", System.currentTimeMillis());
        notification.contentView = contentView;
        notification.contentIntent = contentIntent;
        notification.flags = Notification.FLAG_ONGOING_EVENT + Notification.FLAG_NO_CLEAR;
    }

    @Override
    public void onDestroy() {
        serviceLooper.quit();

        // Cancel the persistent notification.
        notificationManager.cancel(NOTIFICATION_ID);
    }

    @Override
    public void onStart(Intent intent, int startid) {
        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startid;
        msg.obj = intent;
        serviceHandler.sendMessage(msg);
        Log.i(TAG, "Sending message " + msg);
    }

    // actual worker
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Intent intent = (Intent) msg.obj;

            // get image to upload
            Uri uri = intent.getData();

            AssetFileDescriptor asset = null;
            HttpURLConnection conn = null;
            try {
                // find the file that will be uploaded
                asset = MediciUploadService.this.getContentResolver().openAssetFileDescriptor(uri, "r");
                String filename = uri.getLastPathSegment();

                // get extra information
                String username = intent.getStringExtra(EXTRA_USERNAME);
                String password = intent.getStringExtra(EXTRA_PASSWORD);
                String server = intent.getStringExtra(EXTRA_SERVER);
                final String caption = intent.getStringExtra(EXTRA_CAPTION);

                // get the length
                final long length = asset.getLength();

                // update and (maybe) show notification
                Log.i(TAG, "Uploading " + caption + " with size " + length);
                handler.post(new Runnable() {
                    public void run() {
                        contentView.setTextViewText(R.id.notification_text, "Uploading " + caption);
                        contentView.setProgressBar(R.id.notification_progress, (int) length, 0, (length == AssetFileDescriptor.UNKNOWN_LENGTH));
                        contentView.setTextViewText(R.id.notification_percentage, "0%");
                        notificationManager.notify(NOTIFICATION_ID, notification);
                    }
                });

                // Make a connect to the server
                URL url = new URL(server + "UploadBlob");
                conn = (HttpURLConnection) url.openConnection();

                // Put the authentication details in the request
                String up = username + ":" + password;

                String encodedUsernamePassword = Base64.encodeToString(up.getBytes(), Base64.NO_WRAP);
                conn.setRequestProperty("Authorization", "Basic " + encodedUsernamePassword);

                // make it a post request
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setUseCaches(false);
                conn.setRequestMethod("POST");

                // mark it as multipart
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDRY);

                // create output stream
                DataOutputStream dataOS = new DataOutputStream(conn.getOutputStream());

                // write data
                dataOS.writeBytes("--" + BOUNDRY + "\r\n");
                dataOS.writeBytes("Content-Disposition: form-data; name=\"" + caption + "\"; filename=\"" + filename + "\";\r\n");
                String mimetype = MediciUploadService.this.getContentResolver().getType(uri);
                if (mimetype != null) {
                    dataOS.writeBytes("Content-Type: " + mimetype + "\r\n");
                }
                dataOS.writeBytes("\r\n");

                // actual data to be written
                InputStream is = asset.createInputStream();
                byte[] buf = new byte[10240];
                int len = 0;
                int total = 0;
                int count = 0;
                while ((len = is.read(buf)) > 0) {
                    total += len;
                    count++;
                    if (count >= 10) {
                        count = 0;
                        final int totalbytes = total;
                        handler.post(new Runnable() {
                            public void run() {
                                if (length == -1) {
                                    contentView.setTextViewText(R.id.notification_percentage, String.format("%d", totalbytes));
                                } else {
                                    contentView.setProgressBar(R.id.notification_progress, (int) length, totalbytes, false);
                                    contentView.setTextViewText(R.id.notification_percentage, String.format("%d%%", 100 * totalbytes / length));
                                }
                                notificationManager.notify(NOTIFICATION_ID, notification);
                            }
                        });
                        // give UI thread a chance to update.
                        Thread.sleep(100);
                    }
                    dataOS.write(buf, 0, len);
                }
                is.close();

                // write final boundary and done
                dataOS.writeBytes("\r\n--" + BOUNDRY + "--");
                dataOS.flush();
                dataOS.close();

                // Ensure we got the HTTP 200 response code
                int responseCode = conn.getResponseCode();
                if (responseCode != 200) {
                    throw new Exception(String.format("Received the response code %d from the URL %s", responseCode, conn.getResponseMessage()));
                }
                Log.i(TAG, "All is OK");

                // Read the response
                is = conn.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] bytes = new byte[1024];
                int bytesRead;
                while ((bytesRead = is.read(bytes)) != -1) {
                    baos.write(bytes, 0, bytesRead);
                }
                byte[] bytesReceived = baos.toByteArray();
                baos.close();
                is.close();
                String response = new String(bytesReceived);
                Log.i(TAG, response);

            } catch (Exception e) {
                Log.w(TAG, "Could not send dataset.", e);
            } finally {
                if (asset != null) {
                    try {
                        asset.close();
                    } catch (IOException e) {
                        Log.w(TAG, "Could not close dataset.", e);
                    }
                }
                if (conn != null) {
                    conn.disconnect();
                }
            }

            // hide notification
            notificationManager.cancel(NOTIFICATION_ID);

            stopSelfResult(msg.arg1);
        }
    };
}
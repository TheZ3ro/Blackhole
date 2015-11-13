package org.thezero.blackhole.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import org.thezero.blackhole.R;
import org.thezero.blackhole.app.AWSActivity;
import org.thezero.blackhole.app.AppSettings;
import org.thezero.blackhole.webserver.WebServer;

public class HTTPService extends Service {
	public static final int NOTIFICATION_STARTED_ID = 1;
	private NotificationManager notifyManager = null;
	private WebServer server = null;
	
	@Override
	public void onCreate() {
		super.onCreate();
		notifyManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		server = new WebServer(this);
	}

	@Override
	public void onDestroy() {
		server.stopThread();
		notifyManager.cancel(NOTIFICATION_STARTED_ID);
		notifyManager = null;
        AppSettings.setServiceStarted(this, false);
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		server.startThread();
		showNotification();
		return START_STICKY;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	private void showNotification(){
		Intent startIntent = new Intent(this,AWSActivity.class);
		startIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent intent = PendingIntent.getActivity(this, 0, startIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent dismissIntent = new Intent(this,AWSActivity.class);
        dismissIntent.setAction(AWSActivity.ACTION_DISMISS);
        dismissIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent piDismiss = PendingIntent.getService(this, 0, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_notify)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.notification_started_text))
                        .setContentIntent(intent)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(getString(R.string.notification_started_text)))
                        .addAction(android.R.drawable.ic_menu_close_clear_cancel,getString(R.string.dismiss), piDismiss);
        Notification n = mBuilder.build();
        n.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
        notifyManager.notify(NOTIFICATION_STARTED_ID, n);
	}
}

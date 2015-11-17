package org.thezero.blackhole.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import org.thezero.blackhole.Utility;
import org.thezero.blackhole.app.AppSettings;
import org.thezero.blackhole.app.BlackNotification;
import org.thezero.blackhole.webserver.WebServer;

public class HTTPService extends Service {
	public static final int NOTIFICATION_STARTED_ID = 239;
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
        new BlackNotification(this, Utility.getHost());
	}
}

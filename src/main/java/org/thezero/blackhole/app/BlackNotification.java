package org.thezero.blackhole.app;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import org.thezero.blackhole.R;
import org.thezero.blackhole.service.HTTPService;

/**
 * Created by thezero on 17/11/15.
 */
public class BlackNotification extends Notification {

    private Context ctx;
    private NotificationManager mNotificationManager;
    public static final String ACTION_KEY = "DO";
    public static final String ACTION_N_OPEN = "Open";
    public static final String ACTION_N_STOP = "Stop";

    public BlackNotification(Context ctx, String text){
        super();
        this.ctx=ctx;
        String ns = Context.NOTIFICATION_SERVICE;
        mNotificationManager = (NotificationManager) ctx.getSystemService(ns);

        Intent open=new Intent(ctx, AWSActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pOpen = PendingIntent.getActivity(ctx, 0, open, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification noti = new NotificationCompat.Builder(ctx)
                .setContentTitle(ctx.getString(R.string.app_name))
                .setContentText(ctx.getString(R.string.notification_started_text) + " " + text)
                .setContentIntent(pOpen)
                .setSmallIcon(R.drawable.ic_notify)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(ctx.getString(R.string.notification_started_text) + " " + text))
                .build();

        noti.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
        mNotificationManager.notify(HTTPService.NOTIFICATION_STARTED_ID, noti);
    }

}
package com.xy.psn.boardcast_helper.managers;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;

import com.xy.psn.R;
import com.xy.psn.member.MemberMailboxActivity;

public class NotificationManager {
    public static final int NOTIFICATION_ID = -Integer.MAX_VALUE;

    private static NotificationManager INSTANCE = null;

    private NotificationManager() {
    }

    public synchronized static NotificationManager getInstance() {
        if (INSTANCE == null) {
            return new NotificationManager();
        }

        return INSTANCE;
    }

    public void generateNotification(Context context, Bitmap bitmap, String title, String message) {
        Notification notification = this.createNotification(context, bitmap, title, message);
        this.notifyNotification(context, notification);
    }

    /*public void generateNotification(Context context, String title, String message) {
        //Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        Bitmap bitmap = null;
        Notification notification = createNotification(context, bitmap, title, message);
        this.notifyNotification(context, notification);
    }*/

    private Notification createNotification(Context context, Bitmap bitmap, String title, String message) {
        //Intent intent = new Intent(context.getApplicationContext(), LoginActivity.class);
        Intent intent = new Intent(context.getApplicationContext(), MemberMailboxActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context.getApplicationContext(),
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        //APP的預設icon是R.mipmap.ic_launcher
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        NotificationCompat.Style notificationStyle = createNotificationStyle(message, bitmap);
        Notification notification = mBuilder
                .setSmallIcon(R.drawable.logo_ntub)//顯示在通知欄
                .setLargeIcon(bitmap)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentIntent(pendingIntent)
                .setTicker(message)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .build();
        notification.flags |= Intent.FLAG_ACTIVITY_SINGLE_TOP;
        notification.defaults |= Notification.DEFAULT_VIBRATE;

        return notification;
    }

    private NotificationCompat.Style createNotificationStyle(String message, Bitmap picBitmap) {
        NotificationCompat.Style style;

        if (picBitmap == null) {
            style = new NotificationCompat.BigTextStyle().bigText(message);
        } else {
            style = new NotificationCompat.BigPictureStyle().bigPicture(picBitmap).setSummaryText(message);
        }

        return style;
    }

    private void notifyNotification(Context context, Notification notification) {
        if (notification != null) {
            android.app.NotificationManager notificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_ID, notification);
        }
    }
}

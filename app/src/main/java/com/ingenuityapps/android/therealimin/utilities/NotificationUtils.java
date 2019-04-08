package com.ingenuityapps.android.therealimin.utilities;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.google.firebase.Timestamp;
import com.ingenuityapps.android.therealimin.AttendanceActivity;
import com.ingenuityapps.android.therealimin.CheckInActivity;
import com.ingenuityapps.android.therealimin.R;
import com.ingenuityapps.android.therealimin.data.CheckIn;

import java.text.SimpleDateFormat;

public class NotificationUtils {

    /*
     * This notification ID can be used to access our notification after we've displayed it. This
     * can be handy when we need to cancel the notification, or perhaps update it. This number is
     * arbitrary and can be set to whatever you like. 1138 is in no way significant.
     */
    private static final int AUTOCHECKOUT_REMINDER_NOTIFICATION_ID = 1138;
    /**
     * This pending intent id is used to uniquely reference the pending intent
     */
    private static final int AUTOCHECKOUT_REMINDER_PENDING_INTENT_ID = 3417;
    /**
     * This notification channel id is used to link notifications to this channel
     */
    private static final String AUTOCHECKOUT_REMINDER_NOTIFICATION_CHANNEL_ID = "reminder_notification_channel";


    public static void remindUserAutoCheckOut(Context context, String eventDescription, Timestamp checkOutTime) {

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    AUTOCHECKOUT_REMINDER_NOTIFICATION_CHANNEL_ID,
                    context.getString(R.string.main_notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }

        SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context,AUTOCHECKOUT_REMINDER_NOTIFICATION_CHANNEL_ID)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setSmallIcon(R.drawable.imin_logo)
                .setLargeIcon(largeIcon(context))
                .setContentTitle(context.getString(R.string.autocheckout_reminder_notification_title))
                .setContentText(String.format(context.getString(R.string.autocheckout_reminder_notification_body), eventDescription, timeFormatter.format(checkOutTime.toDate())))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(String.format(context.getString(R.string.autocheckout_reminder_notification_body), eventDescription, timeFormatter.format(checkOutTime.toDate()))))
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentIntent(contentIntent(context))
                .setAutoCancel(true);


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }


        notificationManager.notify(AUTOCHECKOUT_REMINDER_NOTIFICATION_ID, notificationBuilder.build());
    }


    private static PendingIntent contentIntent(Context context) {

        Intent attendanceActivityIntent = new Intent(context, AttendanceActivity.class);

        return PendingIntent.getActivity(
                context,
                AUTOCHECKOUT_REMINDER_PENDING_INTENT_ID,
                attendanceActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }


    private static Bitmap largeIcon(Context context) {

        Resources res = context.getResources();
        return BitmapFactory.decodeResource(res, R.mipmap.ic_launcher_foreground);
    }

}

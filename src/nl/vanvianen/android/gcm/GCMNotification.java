/**
 * GCMNotification
 */
package nl.vanvianen.android.gcm;

import android.support.v4.app.NotificationCompat;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.util.TiRHelper;

import java.util.Map;
import java.util.HashMap;

public class GCMNotification {
    private static final String LCAT = "GCMNotification";

    public String pkg = null;
    public String title = null;
    public String message = null;
    public String ticker = null;
    public PendingIntent pendingIntent = null;
    public Context context = null;
    public int smallIcon = 0;
    public Bitmap bitmap = null;
    public String group = null;
    public boolean localOnly = true;
    public int priority = 0;
    public HashMap<String, Object> data = null;
    public String sound = null;
    public boolean vibrate = false;
    public boolean insistent = false;
    public boolean silent = false;
    public int notificationId = 1;
    public Integer ledOn = null;
    public Integer ledOff = null;

    public GCMNotification(Context context, String title, String message, String ticker, PendingIntent pendingIntent,
            int smallIcon, Bitmap bitmap, String pkg, HashMap<String, Object> data) {

        this.title = title;
        this.message = message;
        this.ticker = ticker;
        this.pendingIntent = pendingIntent;
        this.context = context;
        this.smallIcon = smallIcon;
        this.data = data;
        this.pkg = pkg;
    }

    private int getResource(String type, String name) {
        int icon = 0;
        if (name != null) {
            /* Remove extension from icon */
            int index = name.lastIndexOf(".");
            if (index > 0) {
                name = name.substring(0, index);
            }
            try {
                icon = TiRHelper.getApplicationResource(type + "." + name);
            } catch (TiRHelper.ResourceNotFoundException ex) {
                Log.e(LCAT, type + "." + name + " not found; make sure it's in platform/android/res/" + type);
            }
        }

        return icon;
    }

    public NotificationCompat.Builder buildNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
            .setContentTitle(title)
            .setContentText(message)
            .setTicker(ticker)
            .setContentIntent(pendingIntent)
            .setSmallIcon(smallIcon)
            .setLargeIcon(bitmap);
        
        if (data.get("group") != null) group = (String) data.get("group");
        Log.i(LCAT, "Group: " + group);

        /* Whether notification should be for this device only or bridged to other devices, can also be set in the push notification payload */
        if (data.get("localOnly") != null) localOnly = Boolean.valueOf((String) data.get("localOnly"));
        Log.i(LCAT, "LocalOnly: " + localOnly);
        builder.setLocalOnly(localOnly);

        if (group != null) builder.setGroup(group);

        if (data.get("priority") != null) priority = Integer.parseInt((String) data.get("priority"));
        if (priority >= NotificationCompat.PRIORITY_MIN && priority <= NotificationCompat.PRIORITY_MAX) {
            builder.setPriority(priority);
            Log.i(LCAT, "Priority: " + priority);
        } else {
            Log.e(LCAT, "Ignored invalid priority " + priority);
        }

        return builder;
    }

    public Notification notification() {
        Notification notification = buildNotification().build();

        /*Silent, can also be set in the push notification payload */
        if (data.get("silent") != null && "false".equals(data.get("silent"))) {

            /* Sound, can also be set in the push notification payload */
            if (data.get("sound") != null && "default".equals(sound)) {
                Log.i(LCAT, "Sound: default sound");
                notification.defaults |= Notification.DEFAULT_SOUND;
            } else if (sound != null) {
                sound = (String) data.get("sound");
                Log.i(LCAT, "Sound " + sound);
                notification.sound = Uri.parse("android.resource://" + pkg + "/" + getResource("raw", sound));
            }

            /* Vibrate, can also be set in the push notification payload */
            if (data.get("vibrate") != null) vibrate = Boolean.valueOf((String) data.get("vibrate"));
            if (vibrate) notification.defaults |= Notification.DEFAULT_VIBRATE;
            Log.i(LCAT, "Vibrate: " + vibrate);

            /* Insistent, can also be set in the push notification payload */
            if ("true".equals(data.get("insistent"))) insistent = true;
            if (insistent) notification.flags |= Notification.FLAG_INSISTENT;
            Log.i(LCAT, "Insistent: " + insistent);
        }

        /* notificationId, set in push payload to specify multiple notifications should be shown. If not specified, subsequent notifications 
        "override / overwrite" the older ones */
        if (data.get("notificationId") != null && data.get("notificationId") instanceof Integer) {
            notificationId = (Integer) data.get("notificationId");
        } else if (data.get("notificationId") != null && data.get("notificationId") instanceof String) {
            try {
                notificationId = Integer.parseInt((String) data.get("notificationId"));
            } catch (NumberFormatException ex) {
                Log.e(LCAT, "Invalid setting notificationId, should be Integer");
            }
        } else {
            Log.e(LCAT, "Invalid setting notificationId, should be Integer");
        }
        notification.number = notificationId;
        Log.i(LCAT, "Notification ID: " + notificationId);

        /* Specify LED flashing */
        if (ledOn != null || ledOff != null) {
            notification.flags |= Notification.FLAG_SHOW_LIGHTS;
            if (ledOn != null)  notification.ledOnMS = ledOn;
            if (ledOff != null) notification.ledOffMS = ledOff;
        } else {
            notification.defaults |= Notification.DEFAULT_LIGHTS;
        }

        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        return notification;
    }
}
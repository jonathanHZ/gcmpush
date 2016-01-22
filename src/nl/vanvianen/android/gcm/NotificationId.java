package nl.vanvianen.android.gcm;

import java.util.concurrent.atomic.AtomicInteger;

public class NotificationId {

    private static NotificationId instance = null;
    private static final AtomicInteger id = new AtomicInteger(0);
    
    /**
     * Returns the instance of NotificationId. This should be used to get
     * a unique Id for all new notifications.
     *
     * @see GCMIntentService#onMessage(Context, Intent)
     */
    public static NotificationId getInstance() {
        if (instance == null) {
            instance = new NotificationId();
        }     
        return instance;
    }
    
    /**
     * Returns the current id and increments the atomic value to gurantee
     * unique values on all threads and even after app state changes.
     *
     * @return int A unique id to use in the notify method on Notification.
     */
    public int getUniqueId() {
       return id.getAndAdd(1);
    }

}

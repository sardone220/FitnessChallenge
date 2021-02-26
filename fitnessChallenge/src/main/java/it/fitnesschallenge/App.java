/**
 * Questa classe crea il NotificationChannel che serve per mostrare la notifica nella barra delle
 * notifiche, da notare che crea questo canale solo se la versione del S.O. è superiore ad Oreo.
 */
package it.fitnesschallenge;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class App extends Application {

    public static final String CHANNEL_ID = "timerServiceChannel";

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID, "Timer", NotificationManager.IMPORTANCE_HIGH
            );

            serviceChannel.setSound(null, null);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(serviceChannel);
        }
    }
}

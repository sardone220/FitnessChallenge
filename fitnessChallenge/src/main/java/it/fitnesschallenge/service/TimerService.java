/**
 * Questa classe è il Service in se, contiene il timer e i metodi per gestire la notifica di foreground
 * qui si crea il collegamento con il fragment e di conseguenza con l'handler che gestisce la UI.
 */
package it.fitnesschallenge.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import it.fitnesschallenge.HomeActivity;
import it.fitnesschallenge.R;
import it.fitnesschallenge.model.room.entity.PersonalExercise;

import static it.fitnesschallenge.App.CHANNEL_ID;
import static it.fitnesschallenge.model.SharedConstance.TIME_FOR_TIMER;

public class TimerService extends Service {

    private static final String TAG = "TimerService";
    private static final int NOTIFICATION_ID = 1;
    private static final String ACTION_STOP_SERVICE = "actionStopService";

    private long mTimeLeftInMillis;
    private final IBinder binder = new RunServiceBinder();
    private NotificationManagerCompat mNotificationCompactManager;
    private NotificationCompat.Builder mNotificationCompactBuilder;
    private Notification mNotification;
    private PendingIntent mPendingIntent;
    private CountDownTimer mCountDownTimer;
    private Intent thisService;
    private Ringtone mRingtone;
    private boolean mTimerIsFinish;
    private boolean mTimerIsStopped;
    private boolean mTimerIsRunning;

    @Override
    public void onCreate() {
        super.onCreate();
        mTimerIsFinish = false;
        mTimerIsStopped = false;
        mTimerIsRunning = false;
        Log.d(TAG, "Creo il service");
    }

    /**
     * onBind ritorna il collegamento tra l'intent creato per richiamare il service ed il service stesso.
     *
     * @param intent l'intent che chiama il service.
     * @return il collegamento tra il service e l'intent.
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Bind tra activity e service");
        return binder;
    }

    /**
     * Questa funzione viene richiamata dopo aver instanziato il service ed averlo collegato
     * @param intent che chiama il service
     * @param flags non utilizzato
     * @param startId id del service
     * @return ritorna un flag che comunica al SO se è necessario ricreare il servizio se questo
     * viene killato per mancanza di memoria.
     */
    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        if (ACTION_STOP_SERVICE.equals(intent.getAction()))
            cancelTimer();
        Intent notificationIntent = new Intent(this, HomeActivity.class);
        mPendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        mTimeLeftInMillis = intent.getLongExtra(TIME_FOR_TIMER, 0);
        thisService = intent;
        return START_NOT_STICKY;
    }

    /**
     * Questo metodo avvia il timer usando una classe standard del SO che gestisce i timer.
     * mTimeLeftInMillis: contiente il valore del timer in millisecondi.
     * 1000: indica la durata di un tick, in questo caso 1s.
     */
    public void startTimer() {
        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimerIsRunning = true;
                mTimeLeftInMillis = millisUntilFinished;
                //NotificationCompactBuilder permette di modificare la notifica senza distruggerla.
                mNotificationCompactBuilder.setContentText(PersonalExercise.getCoolDownString(mTimeLeftInMillis));
                mNotification = mNotificationCompactBuilder.build();
                /*
                 * Il metodo notify indica al SO il cambiamento nella notifica, e quindi la necessità di
                 * un aggiornamento nella UI
                 */
                mNotificationCompactManager.notify(NOTIFICATION_ID, mNotification);
            }

            /**
             * Quando il timer termina viene richiamato questo metodo che accede alla Preferenze di
             * sitema preleva la sveglia standard e la fa suonare, mentre interrompe il service.
             */
            @Override
            public void onFinish() {
                Log.d(TAG, "Start ringtone");
                Uri defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                mRingtone = RingtoneManager.getRingtone(getApplicationContext(),
                        defaultUri);
                mRingtone.play();
                stopSelf();
                try {
                    cancelNotify();
                } catch (NullPointerException ex) {
                    ex.printStackTrace();
                }
                mTimerIsFinish = true;
                mTimerIsRunning = false;
            }
        }.start();
    }

    /**
     * Mette in pausa il timer e ritorna il tempo rimanente al timer.
     *
     * @return tempo rimanente al timer
     */
    public long pauseTimer() {
        if (mCountDownTimer != null)
            mCountDownTimer.cancel();
        mTimerIsRunning = false;
        return mTimeLeftInMillis;
    }

    /**
     * Cessa la riproduzione della sveglia.
     */
    public void stopRingtone() {
        if (mRingtone != null)
            mRingtone.stop();
    }

    /**
     * Cancella il timer.
     */
    public void cancelTimer() {
        stopRingtone();
        if (mCountDownTimer != null)
            mCountDownTimer.cancel();
        mTimerIsRunning = false;
        mTimerIsStopped = true;
        try {
            cancelNotify();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
        stopSelf();
    }

    public boolean isTimerFinish() {
        return mTimerIsFinish;
    }

    public boolean isTimerStopped() {
        return mTimerIsStopped;
    }

    public boolean isTimerRunning() {
        return mTimerIsRunning;
    }

    /**
     * Crea la notifica da mostrare sul canale standard, setta titolo, testo e bottoni di interazione
     * per ogni bottone di interazione viene creato un intent che gestise la chiamata broadcast generata
     * dal SO quando viene premuto il pulsante sulla notifica.
     */
    public void createNotify() {
        Intent stopIntent = new Intent(this, TimerService.class);
        stopIntent.setAction(ACTION_STOP_SERVICE);

        PendingIntent stopPendingIntent = PendingIntent.getService(this,
                0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mNotificationCompactManager = NotificationManagerCompat.from(this);

        mNotificationCompactBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_timer)
                .setContentTitle(getString(R.string.remaining_time))
                .setContentText(PersonalExercise.getCoolDownString(mTimeLeftInMillis))
                .addAction(R.drawable.ic_stop_24dp,
                        getString(R.string.stop_timer),
                        stopPendingIntent)
                .setContentIntent(mPendingIntent);
        mNotification = mNotificationCompactBuilder.build();

        mNotificationCompactManager.notify(NOTIFICATION_ID, mNotification);

        startForeground(NOTIFICATION_ID, mNotification);
    }


    /**
     * Cancella la notifica.
     */
    private void cancelNotify() throws NullPointerException {
        mNotificationCompactManager.cancel(NOTIFICATION_ID);
    }

    /**
     * Recupera il tempo rimanente, senza stoppare il timer.
     * @return tempo rimanente in millisecondi.
     */
    public long getRemainingTime() {
        return mTimeLeftInMillis;
    }

    /**
     * onDestroy forza la distruzione del service
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Chiamato onDestroy");
        getApplicationContext().stopService(thisService);
    }

    /**
     * Questa classe implementa il binder tra Service e Chiamante.
     */
    public class RunServiceBinder extends Binder {
        public TimerService getService() {
            return TimerService.this;
        }
    }
}

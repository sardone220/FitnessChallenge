/**
 * Questo ViewModel è necessario per gestire il cambio di impostazioni o il ripristino del Service
 * del Timer.
 */
package it.fitnesschallenge.model.view;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import it.fitnesschallenge.service.TimerService;

public class TimerViewModel extends AndroidViewModel {

    // Qui viene memorizzato il service creato
    private TimerService mTimerService;
    // Qui si memorizza se il fragment ha già un service collegato.
    private boolean mServiceBound;

    public TimerViewModel(@NonNull Application application) {
        super(application);
    }

    public TimerService getTimerService() {
        return mTimerService;
    }

    public void setTimerService(TimerService mTimerService) {
        this.mTimerService = mTimerService;
    }

    public boolean isServiceBound() {
        return mServiceBound;
    }

    public void setServiceBound(boolean mServiceBound) {
        this.mServiceBound = mServiceBound;
    }
}

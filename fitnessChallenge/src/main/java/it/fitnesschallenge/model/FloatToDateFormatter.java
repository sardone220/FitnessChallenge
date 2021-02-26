/**
 * Questa classe permette di formattare un valore in floating point in una data del tipo 2020-Mar-24,
 * partendo da un valore in floting point del tipo 84.2020 dove 2020 è l'anno e 84 è l'84esimo giorno
 * dell'anno ovvero il 24 marzo in un anno bisestile.
 */
package it.fitnesschallenge.model;

import android.util.Log;

import java.util.GregorianCalendar;

public class FloatToDateFormatter {

    private static final String TAG = "FloatToDateFormatter";
    // Questo array contiene le abbreviazioni dei mesi per la visualizzazione;
    private static final String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    // Quseto array contine i giorni dei vari mesi in ordine.
    private static final int[] MONTHS_DAY = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    private static final float THOUSANDTH_TO_INT = 10000.00F;

    private int mYear;
    private int mDayOfMonth;
    private int mMonth;
    private int mDayOfYear;

    public FloatToDateFormatter(float floatingDate) {
        /*
         * Questo comando effettua il troncamento della valore passato, quindi nel caso di 2020.084,
         * mYear pernderà il valore di 2020;
         */
        mDayOfYear = (int) floatingDate;
        Log.d(TAG, "Data in floating point: " + floatingDate);
        mYear = Math.round((floatingDate - mDayOfYear) * THOUSANDTH_TO_INT);
        Log.d(TAG, "Anno estrapolato: " + mYear + " l'anno è bisestile: " + isLeap());
        /*
         * Dopo aver verificato se l'anno è bisestile passo il giorno dell'anno per calcolare il mese
         * a cui esso appartine, quindi trasformo la parte millesimale in intera, nell esempio avremo:
         * 2020.084 - 2020 = 0.084, dopo di che 0.084 * 1000 = 84, e quindi l'84esimo giorno dell'anno.
         */
        monthOfTheYear(mDayOfYear);
        Log.d(TAG, "Mese calcolato: " + mMonth + " giorno calcolato: " + mDayOfMonth);
    }

    /**
     * Questo metodo restituisce una string formattata del tipo 2020-Mar-24 nel caso d'esempio.
     *
     * @return la stringa formattata contenente la data YYYY-MMM-DD.
     */
    public String formatDate() {
        return mYear + "-" + MONTHS[mMonth] + "-" + mDayOfMonth;
    }

    /**
     * Questo metodo partendo dal giorno dell'anno calcolato ottiene il mese e il giorno di quel mese
     * il calcolo scorre l'array contente la durata dei mesi in giorni, se il giorno dell'anno è maggiore
     * del giorno del mese selezionato, sottrae al girono dell'anno la durata del mese selezionato,
     * continua in questa maniera finchè non viene selezionato un mese dell'anno che ha più giorni dei
     * giorni dell'anno rimasti, a quel punto so di aver selezionato il mese corretto e i giorni dell'
     * anno rimanenti rappresentano esattamente il giorno nel mese selezionato:
     * 84 > 31 // gennaio => true => 84 - 31 = 53 e i++;
     * 53 > 29 // febbraio => true => 53 - 29 = 24 e i++;
     * 24 > 31 // marzo => false => mMonth = 2 e mDayOfMonth = 24;
     * Partendo nel conteggio dei mesi da 0, il mese 2 = Marzo.
     *
     * @param dayOfTheYear contiene il giorno dell'anno calcolato se il valore di dayOfTheYear <= 0
     *                     c'è un errore nel paramentro e quindi mMonth = -1 mDayOfTheYear = -1.
     */
    private void monthOfTheYear(int dayOfTheYear) {
        mMonth = -1;
        mDayOfMonth = -1;
        boolean monthFound = false;
        if (dayOfTheYear > 0) {
            int i;
            for (i = 0; i < MONTHS.length && !monthFound; i++) {
                if (dayOfTheYear > MONTHS_DAY[i])
                    dayOfTheYear -= MONTHS_DAY[i];
                else {
                    mMonth = i;
                    monthFound = true;
                }
            }
            mDayOfMonth = dayOfTheYear;
        }
    }

    /**
     * Questo metodo verifica se l'anno inserito è bisestile, nel caso in cui lo sia, modifica il valore
     * del mese di febbraio impostando la durata a 29 giorni.
     *
     * @return ritorna true se l'anno è bisestile.
     */
    private boolean isLeap() {
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        boolean isLeap = false;
        if (gregorianCalendar.isLeapYear(mYear)) {
            MONTHS_DAY[1] = 29;
            isLeap = true;
        }
        return isLeap;
    }
}

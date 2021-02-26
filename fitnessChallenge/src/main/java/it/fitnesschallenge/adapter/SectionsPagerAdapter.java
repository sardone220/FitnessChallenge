/**
 * Classe auto generata che serve a fare lo switch tra i fragment.
 */
package it.fitnesschallenge.adapter;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import it.fitnesschallenge.R;
import it.fitnesschallenge.Rooms;
import it.fitnesschallenge.Statistics;
import it.fitnesschallenge.model.User;

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.statistics_tab_title, R.string.room_tab_title};
    private final Context mContext;
    private final User mUser;

    /**
     * Il costuttore è autogenerato, imposta il fragment manager dell'activity e il contesto.
     */
    public SectionsPagerAdapter(Context context, FragmentManager fm, User user) {
        super(fm);
        mContext = context;
        mUser = user;
    }

    /**
     * Questo metodo restituisce il fragment fa caricare nel ViewPage, che dovrebbe essere simile al
     * FrameLayout.
     *
     * @param position indica il tab selezionato {0,1}
     *                 0: Statistics tab
     *                 1: Room tab
     * @return restituisce il fragment selezionato.
     */
    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new Statistics();
        } else {
            return new Rooms();
        }
    }

    /**
     * Questo metodo preleva un titlolo dall'array statico.
     *
     * @param position il tab di riferimento
     * @return il titolo
     */
    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    /**
     * Questo metodo restituisce il numero di tab presenti
     *
     * @return numero di tab presenti.
     */
    @Override
    public int getCount() {
        return TAB_TITLES.length;
    }
}
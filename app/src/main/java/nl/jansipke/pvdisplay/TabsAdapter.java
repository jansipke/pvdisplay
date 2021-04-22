package nl.jansipke.pvdisplay;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import nl.jansipke.pvdisplay.fragments.DailyFragment;
import nl.jansipke.pvdisplay.fragments.LiveFragment;
import nl.jansipke.pvdisplay.fragments.MonthlyFragment;
import nl.jansipke.pvdisplay.fragments.SystemFragment;
import nl.jansipke.pvdisplay.fragments.YearlyFragment;

class TabsAdapter extends FragmentPagerAdapter {

    private final static String TAG = TabsAdapter.class.getSimpleName();
    private final static String[] tabTitles = {"Live", "Daily", "Monthly", "Yearly", "System"};

    private final LiveFragment liveFragment;
    private final DailyFragment dailyFragment;
    private final MonthlyFragment monthlyFragment;
    private final YearlyFragment yearlyFragment;
    private final SystemFragment systemFragment;

    TabsAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);

        liveFragment = new LiveFragment();
        dailyFragment = new DailyFragment();
        monthlyFragment = new MonthlyFragment();
        yearlyFragment = new YearlyFragment();
        systemFragment = new SystemFragment();
    }

    @Override
    public int getCount() {
        return tabTitles.length;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        Log.d(TAG, "Returning fragment for tab " + position);

        switch (position) {
            case 0:
                return liveFragment;
            case 1:
                return dailyFragment;
            case 2:
                return monthlyFragment;
            case 3:
                return yearlyFragment;
            case 4:
                return systemFragment;
        }
        return liveFragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }
}

package nl.jansipke.pvdisplay;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

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

    TabsAdapter(FragmentManager fragmentManager, Context context) {
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
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }
}

package nl.jansipke.pvdisplay;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import nl.jansipke.pvdisplay.fragments.DayFragment;
import nl.jansipke.pvdisplay.fragments.LiveFragment;
import nl.jansipke.pvdisplay.fragments.MonthFragment;
import nl.jansipke.pvdisplay.fragments.SystemFragment;
import nl.jansipke.pvdisplay.fragments.YearFragment;

class TabsAdapter extends FragmentPagerAdapter {

    private final static String TAG = TabsAdapter.class.getSimpleName();
    private final static String[] tabTitles = {"Live", "Day", "Month", "Year", "System"};

    private final LiveFragment liveFragment;
    private final DayFragment dayFragment;
    private final MonthFragment monthFragment;
    private final YearFragment yearFragment;
    private final SystemFragment systemFragment;

    TabsAdapter(FragmentManager fragmentManager, Context context) {
        super(fragmentManager);

        liveFragment = new LiveFragment();
        dayFragment = new DayFragment();
        monthFragment = new MonthFragment();
        yearFragment = new YearFragment();
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
                return dayFragment;
            case 2:
                return monthFragment;
            case 3:
                return yearFragment;
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

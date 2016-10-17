package nl.jansipke.pvdisplay;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TabsAdapter extends FragmentPagerAdapter {

    private final static String[] tabTitles = {"Live", "Day", "Week", "Month", "Year", "System"};
    private final Context context;

    public TabsAdapter(FragmentManager fragmentManager, Context context) {
        super(fragmentManager);
        this.context = context;
    }

    @Override
    public int getCount() {
        return tabTitles.length;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0: return new LiveFragment();
            case 1: return new DayFragment();
            case 2: return new WeekFragment();
            case 3: return new MonthFragment();
            case 4: return new YearFragment();
            case 5: return new SystemFragment();
            default: return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }
}

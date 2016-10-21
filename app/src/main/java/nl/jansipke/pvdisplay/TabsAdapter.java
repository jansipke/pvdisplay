package nl.jansipke.pvdisplay;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

import nl.jansipke.pvdisplay.fragments.DayFragment;
import nl.jansipke.pvdisplay.fragments.LiveFragment;
import nl.jansipke.pvdisplay.fragments.MonthFragment;
import nl.jansipke.pvdisplay.fragments.SystemFragment;
import nl.jansipke.pvdisplay.fragments.WeekFragment;
import nl.jansipke.pvdisplay.fragments.YearFragment;

public class TabsAdapter extends FragmentPagerAdapter implements ViewPager.OnPageChangeListener {

    private final static String TAG = TabsAdapter.class.getSimpleName();
    private final static String[] tabTitles = {"Live", "Day", "Week", "Month", "Year", "System"};

    private final LiveFragment liveFragment;
    private final DayFragment dayFragment;
    private final WeekFragment weekFragment;
    private final MonthFragment monthFragment;
    private final YearFragment yearFragment;
    private final SystemFragment systemFragment;

    public TabsAdapter(FragmentManager fragmentManager, Context context) {
        super(fragmentManager);
        liveFragment = new LiveFragment();
        dayFragment = new DayFragment();
        weekFragment = new WeekFragment();
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
                return weekFragment;
            case 3:
                return monthFragment;
            case 4:
                return yearFragment;
            case 5:
                return systemFragment;
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (positionOffset == 0) {
            switch (position) {
                case 0:
                    liveFragment.onFragmentSelected();
                    break;
                case 1:
                    dayFragment.onFragmentSelected();
                    break;
                case 2:
                    weekFragment.onFragmentSelected();
                    break;
                case 3:
                    monthFragment.onFragmentSelected();
                    break;
                case 4:
                    yearFragment.onFragmentSelected();
                    break;
                case 5:
                    systemFragment.onFragmentSelected();
                    break;
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        // Pass
    }

    @Override
    public void onPageSelected(int position) {
        // Pass
    }
}

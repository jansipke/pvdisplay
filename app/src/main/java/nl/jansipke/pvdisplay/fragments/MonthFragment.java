package nl.jansipke.pvdisplay.fragments;

import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MonthFragment extends Fragment {

    private final static String TAG = MonthFragment.class.getSimpleName();

    public void onFragmentSelected() {
        Log.d(TAG, "Fragment selected");
        setTitle();
    }

    private void setTitle() {
        AppCompatActivity appCompatActivity = ((AppCompatActivity) getActivity());
        if (appCompatActivity != null) {
            ActionBar supportActionBar = appCompatActivity.getSupportActionBar();
            if (supportActionBar != null) {
                supportActionBar.setTitle("Month");
            }
        }
    }
}

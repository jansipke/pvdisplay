package nl.jansipke.pvdisplay.fragments;

import android.support.v4.app.Fragment;
import android.util.Log;

public class WeekFragment extends Fragment {

    private final static String TAG = WeekFragment.class.getSimpleName();

    public void onFragmentSelected() {
        Log.d(TAG, "Fragment selected");
    }
}

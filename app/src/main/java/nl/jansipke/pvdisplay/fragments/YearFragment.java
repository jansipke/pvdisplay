package nl.jansipke.pvdisplay.fragments;

import android.support.v4.app.Fragment;
import android.util.Log;

public class YearFragment extends Fragment {

    private final static String TAG = YearFragment.class.getSimpleName();

    public void onFragmentSelected() {
        Log.d(TAG, "Fragment selected");
    }
}

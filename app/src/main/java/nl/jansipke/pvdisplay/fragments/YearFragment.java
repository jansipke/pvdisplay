package nl.jansipke.pvdisplay.fragments;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class YearFragment extends Fragment {

    private final static String TAG = YearFragment.class.getSimpleName();

    public void onFragmentSelected() {
        Log.d(TAG, "Fragment selected");
        setTitle();
    }

    private void setTitle() {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Year");
    }
}

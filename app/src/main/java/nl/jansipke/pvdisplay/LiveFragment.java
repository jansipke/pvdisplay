package nl.jansipke.pvdisplay;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import nl.jansipke.pvdisplay.database.PvDataOperations;

public class LiveFragment extends Fragment {

    public final static String TAG = LiveFragment.class.getSimpleName();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_live, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_previous:
                Log.i(TAG, "Clicked previous");
                Toast.makeText(getActivity(), "Not implemented yet", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_next:
                Log.i(TAG, "Clicked next");
                Toast.makeText(getActivity(), "Not implemented yet", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_refresh:
                Log.i(TAG, "Clicked refresh");
                Toast.makeText(getActivity(), "Not implemented yet", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_date:
                Log.i(TAG, "Clicked date");
                Toast.makeText(getActivity(), "Not implemented yet", Toast.LENGTH_SHORT).show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}

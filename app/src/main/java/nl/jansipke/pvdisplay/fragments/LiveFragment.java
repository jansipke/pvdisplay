package nl.jansipke.pvdisplay.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;
import nl.jansipke.pvdisplay.PvDataService;
import nl.jansipke.pvdisplay.R;
import nl.jansipke.pvdisplay.data.LivePvDatum;
import nl.jansipke.pvdisplay.database.PvDataOperations;
import nl.jansipke.pvdisplay.utils.DateTimeUtils;
import nl.jansipke.pvdisplay.utils.FormatUtils;

import static nl.jansipke.pvdisplay.R.id.graph;

public class LiveFragment extends Fragment {

    private final static String TAG = LiveFragment.class.getSimpleName();

    private final static String STATE_KEY_YEAR = "year";
    private final static String STATE_KEY_MONTH = "month";
    private final static String STATE_KEY_DAY = "day";

    private static DatePickerListener datePickerListener;
    private static DateTimeUtils.YearMonthDay picked;

    private View fragmentView;
    private LayoutInflater layoutInflater;
    private PvDataOperations pvDataOperations;

    public static class DatePickerFragment extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new DatePickerDialog(getContext(), datePickerListener,
                    picked.year, picked.month - 1, picked.day);
        }
    }

    private class DatePickerListener implements DatePickerDialog.OnDateSetListener {

        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            picked.year = year;
            picked.month = month + 1;
            picked.day = day;
            LiveFragment.this.updateScreen(false);
        }
    }

    private List<LivePvDatum> createFullDay(int year, int month, int day,
                                            List<LivePvDatum> livePvData) {
        List<LivePvDatum> fullDay = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            for (int minute = 0; minute < 60; minute += 5) {
                fullDay.add(new LivePvDatum(year, month, day, hour, minute, 0, 0));
            }
        }
        for (LivePvDatum livePvDatum : livePvData) {
            int fullDayIndex = livePvDatum.getHour() * 12 + livePvDatum.getMinute() / 5;
            fullDay.set(fullDayIndex, livePvDatum);
        }
        return fullDay;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        datePickerListener = new DatePickerListener();

        pvDataOperations = new PvDataOperations(getContext());

        if (savedInstanceState != null) {
            Log.d(TAG, "Loading fragment state");
            picked = new DateTimeUtils.YearMonthDay();
            picked.year = savedInstanceState.getInt(STATE_KEY_YEAR);
            picked.month = savedInstanceState.getInt(STATE_KEY_MONTH);
            picked.day = savedInstanceState.getInt(STATE_KEY_DAY);
        } else {
            picked = DateTimeUtils.getTodaysYearMonthDay();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_live, menu);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        layoutInflater = inflater;
        fragmentView = inflater.inflate(R.layout.fragment_live, container, false);
        updateScreen(false);
        return fragmentView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_previous:
                Log.d(TAG, "Clicked previous");
                picked = DateTimeUtils.addDays(picked, -1);
                updateScreen(false);
                Answers.getInstance().logContentView(new ContentViewEvent()
                        .putContentName("Activity")
                        .putContentType("Live")
                        .putContentId("Previous"));
                break;
            case R.id.action_next:
                Log.d(TAG, "Clicked next");
                picked = DateTimeUtils.addDays(picked, 1);
                updateScreen(false);
                Answers.getInstance().logContentView(new ContentViewEvent()
                        .putContentName("Activity")
                        .putContentType("Live")
                        .putContentId("Next"));
                break;
            case R.id.action_refresh:
                Log.d(TAG, "Clicked refresh");
                updateScreen(true);
                Answers.getInstance().logContentView(new ContentViewEvent()
                        .putContentName("Activity")
                        .putContentType("Live")
                        .putContentId("Refresh"));
                break;
            case R.id.action_date:
                Log.d(TAG, "Clicked date");
                DialogFragment dialogFragment = new DatePickerFragment();
                dialogFragment.show(getFragmentManager(), "datePicker");
                Answers.getInstance().logContentView(new ContentViewEvent()
                        .putContentName("Activity")
                        .putContentType("Live")
                        .putContentId("Date"));
                break;
            case R.id.action_today:
                Log.d(TAG, "Clicked today");
                picked = DateTimeUtils.getTodaysYearMonthDay();
                updateScreen(false);
                Answers.getInstance().logContentView(new ContentViewEvent()
                        .putContentName("Activity")
                        .putContentType("Live")
                        .putContentId("Today"));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Log.d(TAG, "Saving fragment state");

        outState.putInt(STATE_KEY_YEAR, picked.year);
        outState.putInt(STATE_KEY_MONTH, picked.month);
        outState.putInt(STATE_KEY_DAY, picked.day);
    }

    private void updateGraph(List<LivePvDatum> livePvData) {
        LinearLayout graphLinearLayout = (LinearLayout) fragmentView.findViewById(graph);
        graphLinearLayout.removeAllViews();

        final Context context = getContext();
        if (context != null) {
            LineChartView lineChartView = new LineChartView(context);
            graphLinearLayout.addView(lineChartView);

            List<PointValue> powerPointValues = new ArrayList<>();
            for (int i = 0; i < livePvData.size(); i++) {
                LivePvDatum livePvDatum = livePvData.get(i);

                float x = (float) i;
                float y = (float) livePvDatum.getPowerGeneration();
                powerPointValues.add(new PointValue(x, y));
            }
            List<Line> lines = new ArrayList<>();
            Line powerLine = new Line(powerPointValues)
                    .setColor(ChartUtils.COLORS[0])
                    .setHasPoints(false)
                    .setCubic(true)
                    .setFilled(true);
            lines.add(powerLine);
            LineChartData lineChartData = new LineChartData();
            lineChartData.setLines(lines);

            Axis yAxis = Axis
                    .generateAxisFromRange(0, 1250, 250) // TODO Use real maximum value
                    .setMaxLabelChars(6)
                    .setTextColor(Color.GRAY)
                    .setHasLines(true);
            yAxis.setName(getResources().getString(R.string.graph_legend_power));
            lineChartData.setAxisYLeft(yAxis);

            lineChartView.setLineChartData(lineChartData);

            lineChartView.setViewportCalculationEnabled(false);
            final Viewport viewport = new Viewport(-1, 1375, livePvData.size(), 0); // TODO Use real maximum value
            lineChartView.setMaximumViewport(viewport);
            lineChartView.setCurrentViewport(viewport);
        }
    }

    private void updateTable(List<LivePvDatum> livePvData) {
        LinearLayout linearLayout = (LinearLayout) fragmentView.findViewById(R.id.table);
        linearLayout.removeAllViews();

        for (int i = livePvData.size() - 1; i >= 0; i--) {
            LivePvDatum livePvDatum = livePvData.get(i);
            View row = layoutInflater.inflate(R.layout.row_live, null);
            ((TextView) row.findViewById(R.id.time)).setText(DateTimeUtils.formatTime(
                    livePvDatum.getHour(),
                    livePvDatum.getMinute(),
                    true));
            ((TextView) row.findViewById(R.id.power)).setText(
                    FormatUtils.POWER_FORMAT.format(livePvDatum.getPowerGeneration()));
            ((TextView) row.findViewById(R.id.energy)).setText(
                    FormatUtils.ENERGY_FORMAT.format(livePvDatum.getEnergyGeneration() / 1000.0));
            linearLayout.addView(row);
        }
    }

    private void updateTitle(int year, int month, int day) {
        TextView textView = (TextView) fragmentView.findViewById(R.id.title);
        textView.setText(DateTimeUtils.formatYearMonthDay(year, month, day, true));
    }

    public void updateScreen(boolean refreshData) {
        Log.d(TAG, "Updating screen");

        List<LivePvDatum> livePvData = pvDataOperations.loadLive(
                picked.year, picked.month, picked.day);

        if (refreshData || livePvData.size() == 0) {
            if (refreshData) {
                Log.d(TAG, "Refreshing live PV data for " + DateTimeUtils.formatYearMonthDay(
                        picked.year, picked.month, picked.day, true));
            } else {
                Log.d(TAG, "No live PV data for " + DateTimeUtils.formatYearMonthDay(
                        picked.year, picked.month, picked.day, true));
            }

            BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getBooleanExtra("success", true)) {
                        updateScreen(false);
                    } else {
                        Toast.makeText(context, intent.getStringExtra("message"),
                                Toast.LENGTH_LONG).show();
                    }
                }
            };
            IntentFilter intentFilter = new IntentFilter(PvDataService.class.getName());
            LocalBroadcastManager.getInstance(getContext())
                    .registerReceiver(broadcastReceiver, intentFilter);

            PvDataService.callLive(getContext(), picked.year, picked.month, picked.day);
        }

        updateTitle(picked.year, picked.month, picked.day);
        updateGraph(createFullDay(picked.year, picked.month, picked.day, livePvData));
        updateTable(livePvData);
    }
}

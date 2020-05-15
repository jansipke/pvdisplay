package nl.jansipke.pvdisplay.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;
import nl.jansipke.pvdisplay.PvDataService;
import nl.jansipke.pvdisplay.R;
import nl.jansipke.pvdisplay.data.AxisLabelValues;
import nl.jansipke.pvdisplay.data.LivePvDatum;
import nl.jansipke.pvdisplay.data.RecordPvDatum;
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
            DateTimeUtils.YearMonthDay today = DateTimeUtils.getTodaysYearMonthDay();
            if (picked.isLaterThan(today)) {
                picked = today;
            }
            LiveFragment.this.updateScreen();
        }
    }

    private void callPvDataService() {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
                if (intent.getBooleanExtra("success", true)) {
                    updateScreen();
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

    private Map<String, List<Integer>> createColors(List<LivePvDatum> livePvDataPicked, List<LivePvDatum> livePvDataPrevious) {
        Map<String, List<Integer>> colors = new HashMap<>();
        colors.put("power", new ArrayList<Integer>());
        colors.put("energy", new ArrayList<Integer>());
        List<LivePvDatum> previousFullDay = createFullDay(new DateTimeUtils.YearMonthDay(), 0, 24, livePvDataPrevious);
        for (LivePvDatum livePvDatum : livePvDataPicked) {
            int fullDayIndex = livePvDatum.getHour() * 12 + livePvDatum.getMinute() / 5;

            final double powerPicked = livePvDatum.getPowerGeneration();
            final double powerPrevious = previousFullDay.get(fullDayIndex).getPowerGeneration();
            if (powerPicked == powerPrevious) {
                colors.get("power").add(Color.rgb(61, 61, 61));
            } else if (powerPicked > powerPrevious) {
                colors.get("power").add(Color.rgb(61, 153, 61));
            } else {
                colors.get("power").add(Color.rgb(153, 61, 61));
            }

            final double energyPicked = livePvDatum.getEnergyGeneration();
            final double energyPrevious = previousFullDay.get(fullDayIndex).getEnergyGeneration();
            if (energyPicked == energyPrevious) {
                colors.get("energy").add(Color.rgb(61, 61, 61));
            } else if (energyPicked > energyPrevious) {
                colors.get("energy").add(Color.rgb(61, 153, 61));
            } else {
                colors.get("energy").add(Color.rgb(153, 61, 61));
            }
        }
        return colors;
    }

    private List<LivePvDatum> createFullDay(DateTimeUtils.YearMonthDay date,
                                            int startHour, int endHour,
                                            List<LivePvDatum> livePvData) {
        List<LivePvDatum> fullDay = new ArrayList<>();
        for (int hour = startHour; hour < endHour; hour++) {
            for (int minute = 0; minute < 60; minute += 5) {
                fullDay.add(new LivePvDatum(date.year, date.month, date.day, hour, minute, 0, 0));
            }
        }
        for (LivePvDatum livePvDatum : livePvData) {
            if (livePvDatum.getHour() >= startHour && livePvDatum.getHour() < endHour) {
                int fullDayIndex = (livePvDatum.getHour() - startHour) * 12 + livePvDatum.getMinute() / 5;
                fullDay.set(fullDayIndex, livePvDatum);
            }
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
        updateScreen();
        return fragmentView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_previous:
                Log.d(TAG, "Clicked previous");
                picked = DateTimeUtils.addDays(picked, -1, true);
                updateScreen();
                break;
            case R.id.action_next:
                Log.d(TAG, "Clicked next");
                picked = DateTimeUtils.addDays(picked, 1, false);
                updateScreen();
                break;
            case R.id.action_refresh:
                Log.d(TAG, "Clicked refresh");
                callPvDataService();
                break;
            case R.id.action_date:
                Log.d(TAG, "Clicked date");
                DialogFragment dialogFragment = new DatePickerFragment();
                dialogFragment.show(getFragmentManager(), "datePicker");
                break;
            case R.id.action_today:
                Log.d(TAG, "Clicked today");
                picked = DateTimeUtils.getTodaysYearMonthDay();
                updateScreen();
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

    private void updateGraph(List<LivePvDatum> livePvDataPicked, List<LivePvDatum> livePvDataPrevious, boolean showPrevious) {
        LinearLayout graphLinearLayout = (LinearLayout) fragmentView.findViewById(graph);
        graphLinearLayout.removeAllViews();

        final Context context = getContext();
        if (context != null) {
            LineChartView lineChartView = new LineChartView(context);
            graphLinearLayout.addView(lineChartView);

            List<Line> lines = new ArrayList<>();

            List<PointValue> powerPointValuesPicked = new ArrayList<>();
            for (int i = 0; i < livePvDataPicked.size(); i++) {
                LivePvDatum livePvDatum = livePvDataPicked.get(i);

                float x = (float) i;
                float y = (float) livePvDatum.getPowerGeneration();
                powerPointValuesPicked.add(new PointValue(x, y));
            }
            Line powerLinePicked = new Line(powerPointValuesPicked)
                    .setColor(ChartUtils.COLORS[0])
                    .setHasPoints(false)
                    .setCubic(true)
                    .setFilled(true);

            if (showPrevious) {
                List<PointValue> powerPointValuesPrevious = new ArrayList<>();
                for (int i = 0; i < livePvDataPrevious.size(); i++) {
                    LivePvDatum livePvDatum = livePvDataPrevious.get(i);

                    float x = (float) i;
                    float y = (float) livePvDatum.getPowerGeneration();
                    powerPointValuesPrevious.add(new PointValue(x, y));
                }
                Line powerLinePrevious = new Line(powerPointValuesPrevious)
                        .setColor(Color.rgb(223, 223, 223))
                        .setHasPoints(false)
                        .setCubic(true)
                        .setFilled(false);
                lines.add(powerLinePrevious);
            }

            lines.add(powerLinePicked);
            LineChartData lineChartData = new LineChartData();
            lineChartData.setLines(lines);

            RecordPvDatum recordPvDatum = pvDataOperations.loadRecord();
            double yAxisMax = Math.max(recordPvDatum.getLivePowerGeneration(), 1.0);
            AxisLabelValues axisLabelValues = FormatUtils.getAxisLabelValues(yAxisMax);
            Axis yAxis = Axis
                    .generateAxisFromRange(0, axisLabelValues.getMax(), axisLabelValues.getStep())
                    .setMaxLabelChars(6)
                    .setTextColor(Color.GRAY)
                    .setHasLines(true);
            yAxis.setName(getResources().getString(R.string.graph_legend_power));
            lineChartData.setAxisYLeft(yAxis);

            lineChartView.setLineChartData(lineChartData);

            lineChartView.setViewportCalculationEnabled(false);
            Viewport viewport = new Viewport(-1, axisLabelValues.getView(), livePvDataPicked.size(), 0);
            lineChartView.setMaximumViewport(viewport);
            lineChartView.setCurrentViewport(viewport);
        }
    }

    private void updateTable(List<LivePvDatum> livePvData, Map<String, List<Integer>> colors, boolean showPrevious) {
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
            if (showPrevious) {
                ((TextView) row.findViewById(R.id.power)).setTextColor(colors.get("power").get(i));
            }
            ((TextView) row.findViewById(R.id.energy)).setText(
                    FormatUtils.ENERGY_FORMAT.format(livePvDatum.getEnergyGeneration() / 1000.0));
            if (showPrevious) {
                ((TextView) row.findViewById(R.id.energy)).setTextColor(colors.get("energy").get(i));
            }
            linearLayout.addView(row);
        }
    }

    private void updateTitle(int year, int month, int day) {
        String title = DateTimeUtils.getDayOfWeek(year, month, day) + "  " +
                DateTimeUtils.formatYearMonthDay(year, month, day, true);
        TextView textView = (TextView) fragmentView.findViewById(R.id.title);
        textView.setText(title);
    }

    public void updateScreen() {
        Log.d(TAG, "Updating screen with live PV data");

        List<LivePvDatum> livePvDataPicked = pvDataOperations.loadLive(
                picked.year, picked.month, picked.day);
        if (livePvDataPicked.size() == 0) {
            Log.d(TAG, "No live PV data for " + DateTimeUtils.formatYearMonthDay(
                    picked.year, picked.month, picked.day, true));
            callPvDataService();
        }

        DateTimeUtils.YearMonthDay previous = DateTimeUtils.addDays(picked, -1, false);
        List<LivePvDatum> livePvDataPrevious = pvDataOperations.loadLive(
                previous.year, previous.month, previous.day);

        if (isAdded() && getActivity() != null) {
            final SharedPreferences sharedPreferences = PreferenceManager.
                    getDefaultSharedPreferences(getContext());
            int startHour;
            try {
                startHour = Integer.parseInt(sharedPreferences.getString(getResources().
                        getString(R.string.preferences_key_graph_hour_start), "0"));
            } catch (Exception e) {
                startHour = 0;
            }
            int endHour;
            try {
                endHour = Integer.parseInt(sharedPreferences.getString(getResources().
                        getString(R.string.preferences_key_graph_hour_end), "24"));
            } catch (Exception e) {
                endHour = 24;
            }
            boolean showPrevious = sharedPreferences.getBoolean(getResources().
                    getString(R.string.preferences_key_show_previous), true);

            updateTitle(picked.year, picked.month, picked.day);
            updateGraph(createFullDay(picked, startHour, endHour, livePvDataPicked),
                        createFullDay(previous, startHour, endHour, livePvDataPrevious),
                        showPrevious);
            updateTable(livePvDataPicked,
                        createColors(livePvDataPicked, livePvDataPrevious),
                        showPrevious);
        }
    }
}

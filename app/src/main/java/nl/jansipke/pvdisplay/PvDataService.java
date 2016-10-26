package nl.jansipke.pvdisplay;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.jansipke.pvdisplay.data.HistoricalPvDatum;
import nl.jansipke.pvdisplay.data.LivePvDatum;
import nl.jansipke.pvdisplay.data.StatisticPvDatum;
import nl.jansipke.pvdisplay.data.SystemPvDatum;
import nl.jansipke.pvdisplay.database.PvDataOperations;
import nl.jansipke.pvdisplay.utils.DateTimeUtils;
import nl.jansipke.pvdisplay.utils.NetworkUtils;

public class PvDataService extends Service {

    private final static String TAG = PvDataService.class.getSimpleName();
    private final static String API_KEY = "4054f46dd2c8e71855122e964c8e099cb9394d69";
    private final static String SYSTEM_ID = "23329";
    private final static String URL_BASE = "http://pvoutput.org/service/r2/";

    public static void callHistorical(Context context,
                                      int fromYear, int fromMonth, int fromDay,
                                      int toYear, int toMonth, int toDay) {
        Intent intent = new Intent(context, PvDataService.class);
        intent.putExtra("type", "historical");
        intent.putExtra("fromYear", fromYear);
        intent.putExtra("fromMonth", fromMonth);
        intent.putExtra("fromDay", fromDay);
        intent.putExtra("toYear", toYear);
        intent.putExtra("toMonth", toMonth);
        intent.putExtra("toDay", toDay);
        context.startService(intent);
    }

    public static void callLive(Context context, int year, int month, int day) {
        Intent intent = new Intent(context, PvDataService.class);
        intent.putExtra("type", "live");
        intent.putExtra("year", year);
        intent.putExtra("month", month);
        intent.putExtra("day", day);
        context.startService(intent);
    }

    public static void callStatistic(Context context) {
        Intent intent = new Intent(context, PvDataService.class);
        intent.putExtra("type", "statistic");
        context.startService(intent);
    }

    public static void callSystem(Context context) {
        Intent intent = new Intent(context, PvDataService.class);
        intent.putExtra("type", "system");
        context.startService(intent);
    }

    private void downloadHistorical(final int fromYear, final int fromMonth, final int fromDay,
                                    final int toYear, final int toMonth, final int toDay) {
        Log.d(TAG, "Downloading historical PV data for " +
                DateTimeUtils.formatDate(fromYear, fromMonth, fromDay, true) +
                " to " +
                DateTimeUtils.formatDate(toYear, toMonth, toDay, true));
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Download data
                    Map<String, String> headers = new HashMap<>();
                    headers.put("X-Pvoutput-Apikey", API_KEY);
                    headers.put("X-Pvoutput-SystemId", SYSTEM_ID);
                    String fromDate = DateTimeUtils.formatDate(fromYear, fromMonth, fromDay, false);
                    String toDate = DateTimeUtils.formatDate(toYear, toMonth, toDay, false);
                    String url = URL_BASE + "getoutput.jsp?df=" + fromDate + "&dt=" + toDate;
                    String result = NetworkUtils.httpGet(url, headers);

                    // Parse and save data
                    List<HistoricalPvDatum> historicalPvData = new PvOutputParser().parseHistorical(result);
                    new PvDataOperations(getApplicationContext()).saveHistorical(historicalPvData);

                    reportStatus(true, "Downloaded " + historicalPvData.size() + " data points");
                } catch (IOException e) {
                    reportStatus(false, "Could not download historical PV data: " + e.getMessage());
                } catch (ParseException e) {
                    reportStatus(false, "Could not parse historical PV data: " + e.getMessage());
                }
            }
        }).start();
    }

    private void downloadLive(final int year, final int month, final int day) {
        Log.d(TAG, "Downloading live PV data for " + DateTimeUtils.formatDate(year, month, day, true));
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Download data
                    Map<String, String> headers = new HashMap<>();
                    headers.put("X-Pvoutput-Apikey", API_KEY);
                    headers.put("X-Pvoutput-SystemId", SYSTEM_ID);
                    String date = DateTimeUtils.formatDate(year, month, day, false);
                    String url = URL_BASE + "getstatus.jsp?d=" + date + "&h=1&limit=288&asc=1";
                    String result = NetworkUtils.httpGet(url, headers);

                    // Parse and save data
                    List<LivePvDatum> livePvData = new PvOutputParser().parseLive(result);
                    new PvDataOperations(getApplicationContext()).saveLive(livePvData);

                    reportStatus(true, "Downloaded " + livePvData.size() + " data points");
                } catch (IOException e) {
                    reportStatus(false, "Could not download live PV data: " + e.getMessage());
                } catch (ParseException e) {
                    reportStatus(false, "Could not parse live PV data: " + e.getMessage());
                }
            }
        }).start();
    }

    private void downloadStatistic() {
        Log.d(TAG, "Downloading statistic PV data");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Download data
                    Map<String, String> headers = new HashMap<>();
                    headers.put("X-Pvoutput-Apikey", API_KEY);
                    headers.put("X-Pvoutput-SystemId", SYSTEM_ID);
                    String url = URL_BASE + "getstatistic.jsp";
                    String result = NetworkUtils.httpGet(url, headers);

                    // Parse and save data
                    StatisticPvDatum statisticPvDatum = new PvOutputParser().parseStatistic(result);
                    new PvDataOperations(getApplicationContext()).saveStatistic(statisticPvDatum);

                    reportStatus(true, "Downloaded statistic PV data");
                } catch (IOException e) {
                    reportStatus(false, "Could not download statistic PV data: " + e.getMessage());
                } catch (ParseException e) {
                    reportStatus(false, "Could not parse statistic PV data: " + e.getMessage());
                }
            }
        }).start();
    }

    private void downloadSystem() {
        Log.d(TAG, "Downloading system PV data");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Download data
                    Map<String, String> headers = new HashMap<>();
                    headers.put("X-Pvoutput-Apikey", API_KEY);
                    headers.put("X-Pvoutput-SystemId", SYSTEM_ID);
                    String url = URL_BASE + "getsystem.jsp";
                    String result = NetworkUtils.httpGet(url, headers);

                    // Parse and save data
                    SystemPvDatum systemPvDatum = new PvOutputParser().parseSystem(result);
                    new PvDataOperations(getApplicationContext()).saveSystem(systemPvDatum);

                    reportStatus(true, "Downloaded system PV data");
                } catch (IOException e) {
                    reportStatus(false, "Could not download system PV data: " + e.getMessage());
                } catch (ParseException e) {
                    reportStatus(false, "Could not parse system PV data: " + e.getMessage());
                }
            }
        }).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (NetworkUtils.networkConnected(getApplicationContext())) {
            if (intent.getStringExtra("type").equals("historical")) {
                int fromYear = intent.getIntExtra("fromYear", 0);
                int fromMonth = intent.getIntExtra("fromMonth", 0);
                int fromDay = intent.getIntExtra("fromDay", 0);
                int toYear = intent.getIntExtra("toYear", 0);
                int toMonth = intent.getIntExtra("toMonth", 0);
                int toDay = intent.getIntExtra("toDay", 0);
                downloadHistorical(fromYear, fromMonth, fromDay, toYear, toMonth, toDay);
            } else if (intent.getStringExtra("type").equals("live")) {
                int year = intent.getIntExtra("year", 0);
                int month = intent.getIntExtra("month", 0);
                int day = intent.getIntExtra("day", 0);
                downloadLive(year, month, day);
            } else if (intent.getStringExtra("type").equals("statistic")) {
                downloadStatistic();
            } else if (intent.getStringExtra("type").equals("system")) {
                downloadSystem();
            }
        } else {
            Log.w(TAG, "Could not download PV data because network is unavailable");
        }
        return Service.START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void reportStatus(boolean success, String message) {
        if (success) {
            Log.d(TAG, message);
        } else {
            Log.w(TAG, message);
        }
        Intent intent = new Intent(PvDataService.class.getName());
        intent.putExtra("success", success);
        intent.putExtra("message", message);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }
}

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

import nl.jansipke.pvdisplay.data.LivePvDatum;
import nl.jansipke.pvdisplay.data.StatisticPvDatum;
import nl.jansipke.pvdisplay.data.SystemPvDatum;
import nl.jansipke.pvdisplay.database.PvDataOperations;
import nl.jansipke.pvdisplay.parsers.PvOutputParser;
import nl.jansipke.pvdisplay.utils.DateTimeUtils;
import nl.jansipke.pvdisplay.utils.NetworkUtils;

public class PvDataService extends Service {

    private final static String TAG = PvDataService.class.getSimpleName();
    private final static String API_KEY = "4054f46dd2c8e71855122e964c8e099cb9394d69";
    private final static String SYSTEM_ID = "23329";
    private final static String URL_BASE = "http://pvoutput.org/service/r2/";
//    private final static String URL = "http://pvoutput.org/service/r2/getoutput.jsp?df=20160101&dt=20160801&limit=50";


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

    private void downloadLive(final int year, final int month, final int day) {
        Log.i(TAG, "Downloading live PV data for " + DateTimeUtils.formatDate(year, month, day, true));
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

                    // Parse data
                    List<LivePvDatum> livePvData = new PvOutputParser().parseLive(result);
                    Log.i(TAG, "Downloaded " + livePvData.size() + " data points");

                    // Save data
                    new PvDataOperations(getApplicationContext()).saveLive(livePvData);

                    // Notify that data has been saved
                    Intent intent = new Intent(PvDataService.class.getName());
                    intent.putExtra("type", "live");
                    intent.putExtra("date", date);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                } catch (IOException e) {
                    Log.w(TAG, "Could not download live PV data");
                } catch (ParseException e) {
                    Log.w(TAG, "Could not parse live PV data");
                }
            }
        }).start();
    }

    private void downloadStatistic() {
        Log.i(TAG, "Downloading statistic PV data");
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

                    // Parse data
                    StatisticPvDatum statisticPvDatum = new PvOutputParser().parseStatistic(result);
                    Log.i(TAG, "Downloaded statistic PV data");

                    // Save data
                    new PvDataOperations(getApplicationContext()).saveStatistic(statisticPvDatum);

                    // Notify that data has been saved
                    Intent intent = new Intent(PvDataService.class.getName());
                    intent.putExtra("type", "statistic");
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                } catch (IOException e) {
                    Log.w(TAG, "Could not download statistic PV data");
                } catch (ParseException e) {
                    Log.w(TAG, "Could not parse statistic PV data");
                }
            }
        }).start();
    }

    private void downloadSystem() {
        Log.i(TAG, "Downloading system PV data");
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

                    // Parse data
                    SystemPvDatum systemPvDatum = new PvOutputParser().parseSystem(result);
                    Log.i(TAG, "Downloaded system PV data");

                    // Save data
                    new PvDataOperations(getApplicationContext()).saveSystem(systemPvDatum);

                    // Notify that data has been saved
                    Intent intent = new Intent(PvDataService.class.getName());
                    intent.putExtra("type", "system");
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                } catch (IOException e) {
                    Log.w(TAG, "Could not download system PV data");
                } catch (ParseException e) {
                    Log.w(TAG, "Could not parse system PV data");
                }
            }
        }).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (NetworkUtils.networkConnected(getApplicationContext())) {
            if (intent.getStringExtra("type").equals("live")) {
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
            Log.w(TAG, "Can not download PV data because network is not available");
        }
        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

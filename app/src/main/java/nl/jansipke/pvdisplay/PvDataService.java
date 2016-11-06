package nl.jansipke.pvdisplay;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.jansipke.pvdisplay.data.DailyPvDatum;
import nl.jansipke.pvdisplay.data.LivePvDatum;
import nl.jansipke.pvdisplay.data.MonthlyPvDatum;
import nl.jansipke.pvdisplay.data.StatisticPvDatum;
import nl.jansipke.pvdisplay.data.SystemPvDatum;
import nl.jansipke.pvdisplay.data.YearlyPvDatum;
import nl.jansipke.pvdisplay.database.PvDataOperations;
import nl.jansipke.pvdisplay.utils.DateTimeUtils;
import nl.jansipke.pvdisplay.utils.NetworkUtils;

public class PvDataService extends Service {

    private final static String TAG = PvDataService.class.getSimpleName();
    private final static String URL_BASE = "http://pvoutput.org/service/r2/";

    public static void callDay(Context context, int year, int month) {
        Intent intent = new Intent(context, PvDataService.class);
        intent.putExtra("type", "day");
        intent.putExtra("year", year);
        intent.putExtra("month", month);
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

    public static void callMonth(Context context, int year) {
        Intent intent = new Intent(context, PvDataService.class);
        intent.putExtra("type", "month");
        intent.putExtra("year", year);
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

    public static void callYear(Context context) {
        Intent intent = new Intent(context, PvDataService.class);
        intent.putExtra("type", "year");
        context.startService(intent);
    }

    private String download(String urlPath) throws IOException {
        Map<String, String> headers = new HashMap<>();
        SharedPreferences sharedPreferences = PreferenceManager.
                getDefaultSharedPreferences(getApplicationContext());
        String apiKey = sharedPreferences.getString(getResources().
                getString(R.string.preferences_key_pvoutput_api_key), null);
        String systemId = sharedPreferences.getString(getResources().
                getString(R.string.preferences_key_pvoutput_system_id), null);
        if (apiKey == null || apiKey.equals("") || systemId == null || systemId.equals("")) {
            throw new IOException("API Key and System Id need to be set");
        }
        headers.put("X-Pvoutput-Apikey", apiKey);
        headers.put("X-Pvoutput-SystemId", systemId);
        String url = URL_BASE + urlPath;
        try {
            return NetworkUtils.httpGet(url, headers);
        } catch (Exception e) {
            switch (e.getMessage()) {
                case "Bad request 400: No status found":
                    throw new IOException("No PV data found\n(please select other time range)");
                case "Unauthorized 401: Invalid System ID":
                    throw new IOException("Invalid System Id\n(please edit in settings)");
                case "Unauthorized 401: Invalid API Key":
                    throw new IOException("Invalid API Key\n(please edit in settings)");
                case "Unauthorized 401: Disabled API Key":
                    throw new IOException("Disabled API Key\n(please enable on PVOutput website)");
                case "Forbidden 403: Exceeded 60 requests per hour":
                    throw new IOException("Exceeded 60 requests per hour\n(please do not refresh too often)");
                case "Forbidden 403: Exceeded 300 requests per hour":
                    throw new IOException("Exceeded 300 requests per hour\n(please do not refresh too often)");
            }
            Crashlytics.logException(e);
            throw e;
        }
    }

    private void downloadDay(final int year, final int month) {
        Log.d(TAG, "Downloading daily PV data for " +
                DateTimeUtils.formatYearMonth(year, month, true));
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Download data
                    String fromDate = DateTimeUtils.formatYearMonthDay(year, month, 1, false);
                    String toDate = DateTimeUtils.formatYearMonthDay(year, month, 31, false);
                    String urlPath = "getoutput.jsp?df=" + fromDate + "&dt=" + toDate;
                    String result = download(urlPath);

                    // Parse and save data
                    List<DailyPvDatum> dayPvData = new PvOutputParser().parseDaily(result);
                    new PvDataOperations(getApplicationContext()).saveDaily(dayPvData);

                    reportStatus(true, "Downloaded " + dayPvData.size() + " data points");
                } catch (IOException e) {
                    reportStatus(false, "Could not download daily PV data:\n" + e.getMessage());
                } catch (ParseException e) {
                    Crashlytics.logException(e);
                    reportStatus(false, "Could not parse daily PV data:\n" + e.getMessage());
                }
            }
        }).start();
    }

    private void downloadLive(final int year, final int month, final int day) {
        Log.d(TAG, "Downloading live PV data for " + DateTimeUtils.formatYearMonthDay(year, month, day, true));
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Download data
                    String date = DateTimeUtils.formatYearMonthDay(year, month, day, false);
                    String urlPath = "getstatus.jsp?d=" + date + "&h=1&limit=288&asc=1";
                    String result = download(urlPath);

                    // Parse and save data
                    List<LivePvDatum> livePvData = new PvOutputParser().parseLive(result);
                    new PvDataOperations(getApplicationContext()).saveLive(livePvData);

                    reportStatus(true, "Downloaded " + livePvData.size() + " data points");
                } catch (IOException e) {
                    reportStatus(false, "Could not download live PV data:\n" + e.getMessage());
                } catch (ParseException e) {
                    Crashlytics.logException(e);
                    reportStatus(false, "Could not parse live PV data:\n" + e.getMessage());
                }
            }
        }).start();
    }

    private void downloadMonth(final int year) {
        Log.d(TAG, "Downloading monthly PV data for " + year);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Download data
                    String fromDate = DateTimeUtils.formatYearMonthDay(year, 1, 1, false);
                    String toDate = DateTimeUtils.formatYearMonthDay(year, 12, 31, false);
                    String urlPath = "getoutput.jsp?a=m&df=" + fromDate + "&dt=" + toDate;
                    String result = download(urlPath);

                    // Parse and save data
                    List<MonthlyPvDatum> monthPvData = new PvOutputParser().parseMonthly(result);
                    new PvDataOperations(getApplicationContext()).saveMonthly(monthPvData);

                    reportStatus(true, "Downloaded " + monthPvData.size() + " data points");
                } catch (IOException e) {
                    reportStatus(false, "Could not download monthly PV data:\n" + e.getMessage());
                } catch (ParseException e) {
                    Crashlytics.logException(e);
                    reportStatus(false, "Could not parse monthly PV data:\n" + e.getMessage());
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
                    String urlPath = "getstatistic.jsp";
                    String result = download(urlPath);

                    // Parse and save data
                    StatisticPvDatum statisticPvDatum = new PvOutputParser().parseStatistic(result);
                    new PvDataOperations(getApplicationContext()).saveStatistic(statisticPvDatum);

                    reportStatus(true, "Downloaded statistic PV data");
                } catch (IOException e) {
                    reportStatus(false, "Could not download statistic PV data:\n" + e.getMessage());
                } catch (ParseException e) {
                    Crashlytics.logException(e);
                    reportStatus(false, "Could not parse statistic PV data:\n" + e.getMessage());
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
                    String urlPath = "getsystem.jsp";
                    String result = download(urlPath);

                    // Parse and save data
                    SystemPvDatum systemPvDatum = new PvOutputParser().parseSystem(result);
                    new PvDataOperations(getApplicationContext()).saveSystem(systemPvDatum);

                    reportStatus(true, "Downloaded system PV data");
                } catch (IOException e) {
                    reportStatus(false, "Could not download system PV data:\n" + e.getMessage());
                } catch (ParseException e) {
                    Crashlytics.logException(e);
                    reportStatus(false, "Could not parse system PV data:\n" + e.getMessage());
                }
            }
        }).start();
    }

    private void downloadYear() {
        Log.d(TAG, "Downloading yearly PV data");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Download data
                    String urlPath = "getoutput.jsp?a=y";
                    String result = download(urlPath);

                    // Parse and save data
                    List<YearlyPvDatum> yearPvData = new PvOutputParser().parseYearly(result);
                    new PvDataOperations(getApplicationContext()).saveYearly(yearPvData);

                    reportStatus(true, "Downloaded " + yearPvData.size() + " data points");
                } catch (IOException e) {
                    reportStatus(false, "Could not download yearly PV data:\n" + e.getMessage());
                } catch (ParseException e) {
                    Crashlytics.logException(e);
                    reportStatus(false, "Could not parse yearly PV data:\n" + e.getMessage());
                }
            }
        }).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (NetworkUtils.isNetworkConnected(getApplicationContext())) {
            switch(intent.getStringExtra("type")) {
                case "day":
                    int year = intent.getIntExtra("year", 0);
                    int month = intent.getIntExtra("month", 0);
                    downloadDay(year, month);
                    break;
                case "live":
                    year = intent.getIntExtra("year", 0);
                    month = intent.getIntExtra("month", 0);
                    int day = intent.getIntExtra("day", 0);
                    downloadLive(year, month, day);
                    break;
                case "month":
                    year = intent.getIntExtra("year", 0);
                    downloadMonth(year);
                    break;
                case "statistic":
                    downloadStatistic();
                    break;
                case "system":
                    downloadSystem();
                    break;
                case "year":
                    downloadYear();
                    break;
            }
        } else {
            Log.w(TAG, "Could not download PV data because\nnetwork is unavailable");
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

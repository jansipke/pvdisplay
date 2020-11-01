package nl.jansipke.pvdisplay;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    protected final static String URL_BASE = "https://pvoutput.org/service/r2/";

    public static void callAll(Context context, int year, int month, int day) {
        Intent intent = new Intent(context, PvDataService.class);
        intent.putExtra("type", "all");
        intent.putExtra("year", year);
        intent.putExtra("month", month);
        intent.putExtra("day", day);
        context.startService(intent);
    }

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
            switch (Objects.requireNonNull(e.getMessage())) {
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
            throw e;
        }
    }

    private void downloadDay(final int year, final int month) {
        Log.d(TAG, "Downloading daily PV data for " + new DateTimeUtils.YearMonth(year, month));
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Download data
                    String fromDate = new DateTimeUtils.YearMonthDay(year, month, 1).asString(false);
                    String toDate = new DateTimeUtils.YearMonthDay(year, month, 31).asString(false);
                    String urlPath = "getoutput.jsp?df=" + fromDate + "&dt=" + toDate;
                    String result = download(urlPath);

                    // Parse and save data
                    List<DailyPvDatum> dailyPvData = new PvOutputParser().parseDaily(result);
                    new PvDataOperations(getApplicationContext()).saveDaily(dailyPvData);

                    reportStatus(true, "day", "Downloaded " + dailyPvData.size() + " data points");
                } catch (IOException e) {
                    reportStatus(false, "day", "Could not download daily PV data for " +
                            new DateTimeUtils.YearMonth(year, month) + ": " + e.getMessage());
                } catch (ParseException e) {
                    reportStatus(false, "day", "Could not parse daily PV data for " +
                            new DateTimeUtils.YearMonth(year, month) + ": " + e.getMessage());
                }
            }
        }).start();
    }

    private void downloadLive(final int year, final int month, final int day) {
        Log.d(TAG, "Downloading live PV data for " + new DateTimeUtils.YearMonthDay(year, month, day));
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Download data
                    String date = new DateTimeUtils.YearMonthDay(year, month, day).asString(false);
                    String urlPath = "getstatus.jsp?d=" + date + "&h=1&limit=288&asc=1";
                    String result = download(urlPath);

                    // Parse and save data
                    List<LivePvDatum> livePvData = new PvOutputParser().parseLive(result);
                    new PvDataOperations(getApplicationContext()).saveLive(livePvData);

                    reportStatus(true, "live", "Downloaded " + livePvData.size() + " data points");
                } catch (IOException e) {
                    reportStatus(false, "live", "Could not download live PV data for " +
                            new DateTimeUtils.YearMonthDay(year, month, day) + ": " + e.getMessage());
                } catch (ParseException e) {
                    reportStatus(false, "live", "Could not parse live PV data for " +
                            new DateTimeUtils.YearMonthDay(year, month, day) + ": " + e.getMessage());
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
                    String fromDate = new DateTimeUtils.YearMonthDay(year, 1, 1).asString(false);
                    String toDate = new DateTimeUtils.YearMonthDay(year, 12, 31).asString(false);
                    String urlPath = "getoutput.jsp?a=m&df=" + fromDate + "&dt=" + toDate;
                    String result = download(urlPath);

                    // Parse and save data
                    List<MonthlyPvDatum> monthlyPvData = new PvOutputParser().parseMonthly(result);
                    new PvDataOperations(getApplicationContext()).saveMonthly(monthlyPvData);

                    reportStatus(true, "month", "Downloaded " + monthlyPvData.size() + " data points");
                } catch (IOException e) {
                    reportStatus(false, "month", "Could not download monthly PV data for " +
                            year + ": " + e.getMessage());
                } catch (ParseException e) {
                    reportStatus(false, "month", "Could not parse monthly PV data for " +
                            year + ": " + e.getMessage());
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

                    reportStatus(true, "statistic", "Downloaded statistic PV data");
                } catch (IOException e) {
                    reportStatus(false, "statistic", "Could not download statistic PV data: " +
                            e.getMessage());
                } catch (ParseException e) {
                    reportStatus(false, "statistic", "Could not parse statistic PV data: " +
                            e.getMessage());
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

                    reportStatus(true, "system", "Downloaded system PV data");
                } catch (IOException e) {
                    reportStatus(false, "system", "Could not download system PV data: " +
                            e.getMessage());
                } catch (ParseException e) {
                    reportStatus(false, "system", "Could not parse system PV data: " +
                            e.getMessage());
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
                    List<YearlyPvDatum> yearlyPvData = new PvOutputParser().parseYearly(result);
                    new PvDataOperations(getApplicationContext()).saveYearly(yearlyPvData);

                    reportStatus(true, "year", "Downloaded " + yearlyPvData.size() + " data points");
                } catch (IOException e) {
                    reportStatus(false, "year", "Could not download yearly PV data: " +
                            e.getMessage());
                } catch (ParseException e) {
                    reportStatus(false, "year", "Could not parse yearly PV data: " +
                            e.getMessage());
                }
            }
        }).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String type = intent.getStringExtra("type");
        if (NetworkUtils.isNetworkConnected(getApplicationContext())) {
            int year, month, day;
            assert type != null;
            switch(type) {
                case "all":
                    year = intent.getIntExtra("year", 0);
                    month = intent.getIntExtra("month", 0);
                    day = intent.getIntExtra("day", 0);
                    downloadLive(year, month, day);
                    downloadDay(year, month);
                    downloadMonth(year);
                    downloadYear();
                    downloadSystem();
                    downloadStatistic();
                    break;
                case "day":
                    year = intent.getIntExtra("year", 0);
                    month = intent.getIntExtra("month", 0);
                    downloadDay(year, month);
                    break;
                case "live":
                    year = intent.getIntExtra("year", 0);
                    month = intent.getIntExtra("month", 0);
                    day = intent.getIntExtra("day", 0);
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
            reportStatus(false, type, "Could not download PV data because\nnetwork is unavailable");
        }
        return Service.START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void reportStatus(boolean success, String type, String message) {
        if (success) {
            Log.d(TAG, message);
        } else {
            Log.w(TAG, message);
        }
        Intent intent = new Intent(PvDataService.class.getName());
        intent.putExtra("success", success);
        intent.putExtra("type", type);
        intent.putExtra("message", message);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }
}

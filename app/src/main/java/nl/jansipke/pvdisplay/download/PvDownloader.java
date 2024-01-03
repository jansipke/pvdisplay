package nl.jansipke.pvdisplay.download;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import nl.jansipke.pvdisplay.R;
import nl.jansipke.pvdisplay.data.DailyPvDatum;
import nl.jansipke.pvdisplay.data.LivePvDatum;
import nl.jansipke.pvdisplay.data.MonthlyPvDatum;
import nl.jansipke.pvdisplay.data.StatisticPvDatum;
import nl.jansipke.pvdisplay.data.SystemPvDatum;
import nl.jansipke.pvdisplay.data.YearlyPvDatum;
import nl.jansipke.pvdisplay.database.PvDatabase;
import nl.jansipke.pvdisplay.utils.DateTimeUtils;
import nl.jansipke.pvdisplay.utils.NetworkUtils;

public class PvDownloader {

    private final Context context;
    private final AtomicInteger atomicDownloadTotalCount;
    private final AtomicInteger atomicDownloadSuccessCount;
    private final MutableLiveData<Integer> downloadSuccessCount;
    private final MutableLiveData<String> errorMessage;

    public PvDownloader(Context context) {
        this.context = context;
        atomicDownloadTotalCount = new AtomicInteger(0);
        atomicDownloadSuccessCount = new AtomicInteger(0);
        downloadSuccessCount = new MutableLiveData<>();
        errorMessage = new MutableLiveData<>();
    }

    private final static String TAG = PvDownloader.class.getSimpleName();
    public final static String URL_BASE = "https://pvoutput.org/service/r2/";

    private String download(String urlPath) throws IOException {
        Map<String, String> headers = new HashMap<>();
        SharedPreferences sharedPreferences = PreferenceManager.
                getDefaultSharedPreferences(context);
        String apiKey = sharedPreferences.getString(context.getResources().
                getString(R.string.preferences_key_pvoutput_api_key), null);
        String systemId = sharedPreferences.getString(context.getResources().
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

    public void downloadDaily(final DateTimeUtils.YearMonth ym) {
        Log.d(TAG, "Downloading daily PV data for " + ym);
        new Thread(() -> {
            try {
                String fromDate = new DateTimeUtils.YearMonthDay(ym.year, ym.month, 1).asString(false);
                String toDate = new DateTimeUtils.YearMonthDay(ym.year, ym.month, 31).asString(false);
                String urlPath = "getoutput.jsp?df=" + fromDate + "&dt=" + toDate;
                String result = download(urlPath);

                List<DailyPvDatum> dailyPvData = new PvOutputParser().parseDaily(result);
                new PvDatabase(context).saveDaily(dailyPvData);

                reportStatus(true, "Downloaded daily PV data for " + ym + ": " + dailyPvData.size() + " data points");
            } catch (IOException e) {
                reportStatus(false, "Could not download daily PV data for " + ym + ": " + e.getMessage());
            } catch (ParseException e) {
                reportStatus(false, "Could not parse daily PV data for " + ym + ": " + e.getMessage());
            }
        }).start();
    }

    public void downloadLive(final DateTimeUtils.YearMonthDay ymd) {
        Log.d(TAG, "Downloading live PV data for " + ymd);
        new Thread(() -> {
            try {
                String date = ymd.asString(false);
                String urlPath = "getstatus.jsp?d=" + date + "&h=1&limit=288&asc=1";
                String result = download(urlPath);

                List<LivePvDatum> livePvData = new PvOutputParser().parseLive(result);
                new PvDatabase(context).saveLive(livePvData);

                reportStatus(true, "Downloaded live PV data for " + ymd + ": " + livePvData.size() + " data points");
            } catch (IOException e) {
                reportStatus(false, "Could not download live PV data for " + ymd + ": " + e.getMessage());
            } catch (ParseException e) {
                reportStatus(false, "Could not parse live PV data for " + ymd + ": " + e.getMessage());
            }
        }).start();
    }

    public void downloadMonthly(final DateTimeUtils.Year y) {
        Log.d(TAG, "Downloading monthly PV data for " + y);
        new Thread(() -> {
            try {
                String fromDate = new DateTimeUtils.YearMonthDay(y.year, 1, 1).asString(false);
                String toDate = new DateTimeUtils.YearMonthDay(y.year, 12, 31).asString(false);
                String urlPath = "getoutput.jsp?a=m&df=" + fromDate + "&dt=" + toDate;
                String result = download(urlPath);

                List<MonthlyPvDatum> monthlyPvData = new PvOutputParser().parseMonthly(result);
                new PvDatabase(context).saveMonthly(monthlyPvData);

                reportStatus(true, "Downloaded monthly PV data for " + y + ": " + monthlyPvData.size() + " data points");
            } catch (IOException e) {
                reportStatus(false, "Could not download monthly PV data for " + y + ": " + e.getMessage());
            } catch (ParseException e) {
                reportStatus(false, "Could not parse monthly PV data for " + y + ": " + e.getMessage());
            }
        }).start();
    }

    public void downloadStatistic() {
        Log.d(TAG, "Downloading statistic PV data");
        new Thread(() -> {
            try {
                String urlPath = "getstatistic.jsp";
                String result = download(urlPath);

                StatisticPvDatum statisticPvDatum = new PvOutputParser().parseStatistic(result);
                new PvDatabase(context).saveStatistic(statisticPvDatum);

                reportStatus(true, "Downloaded statistic PV data");
            } catch (IOException e) {
                reportStatus(false, "Could not download statistic PV data: " + e.getMessage());
            } catch (ParseException e) {
                reportStatus(false, "Could not parse statistic PV data: " + e.getMessage());
            }
        }).start();
    }

    public void downloadSystem() {
        Log.d(TAG, "Downloading system PV data");
        new Thread(() -> {
            try {
                String urlPath = "getsystem.jsp";
                String result = download(urlPath);

                SystemPvDatum systemPvDatum = new PvOutputParser().parseSystem(result);
                new PvDatabase(context).saveSystem(systemPvDatum);

                reportStatus(true, "Downloaded system PV data");
            } catch (IOException e) {
                reportStatus(false, "Could not download system PV data: " + e.getMessage());
            } catch (ParseException e) {
                reportStatus(false, "Could not parse system PV data: " + e.getMessage());
            }
        }).start();
    }

    public void downloadYearly() {
        Log.d(TAG, "Downloading yearly PV data");
        new Thread(() -> {
            try {
                String urlPath = "getoutput.jsp?a=y";
                String result = download(urlPath);

                List<YearlyPvDatum> yearlyPvData = new PvOutputParser().parseYearly(result);
                new PvDatabase(context).saveYearly(yearlyPvData);

                reportStatus(true, "Downloaded yearly PV data: " + yearlyPvData.size() + " data points");
            } catch (IOException e) {
                reportStatus(false, "Could not download yearly PV data: " + e.getMessage());
            } catch (ParseException e) {
                reportStatus(false, "Could not parse yearly PV data: " + e.getMessage());
            }
        }).start();
    }

    public int getDownloadTotalCount() {
        return atomicDownloadTotalCount.get();
    }

    public LiveData<Integer> getDownloadSuccessCount() {
        return downloadSuccessCount;
    }

    private void reportStatus(boolean success, String message) {
        atomicDownloadTotalCount.addAndGet(1);
        if (success) {
            Log.d(TAG, message);
            atomicDownloadSuccessCount.addAndGet(1);
            downloadSuccessCount.postValue(atomicDownloadSuccessCount.intValue());
        } else {
            Log.w(TAG, message);
            errorMessage.postValue(message);
        }
    }
}

package nl.jansipke.pvdisplay;

import android.app.Service;
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
import nl.jansipke.pvdisplay.database.PvDataOperations;
import nl.jansipke.pvdisplay.parsers.PvOutputParser;
import nl.jansipke.pvdisplay.utils.DateTimeUtils;
import nl.jansipke.pvdisplay.utils.NetworkUtils;

public class PvDataService extends Service {

    private final static String TAG = "PvDataService";

    private final static String API_KEY = "4054f46dd2c8e71855122e964c8e099cb9394d69";
    private final static String SYSTEM_ID = "23329";
    private final static String URL_BASE = "http://pvoutput.org/service/r2/";
//    private final static String URL = "http://pvoutput.org/service/r2/getoutput.jsp?df=20160101&dt=20160801&limit=50";
//    private final static String URL = "http://pvoutput.org/service/r2/getstatistic.jsp";


    private void downloadLivePvData(final int year, final int month, final int day) {
        Log.i(TAG, "Downloading PV data for year=" + year + ", month=" + month + ", day=" + day);
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
                    intent.putExtra("date", date);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                } catch (IOException e) {
                    Log.w(TAG, "Could not download PV data");
                } catch (ParseException e) {
                    Log.w(TAG, "Could not parse PV data");
                }
            }
        }).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getStringExtra("type").equals("live")) {
            int year = intent.getIntExtra("year", 0);
            int month = intent.getIntExtra("month", 0);
            int day = intent.getIntExtra("day", 0);
            if (NetworkUtils.networkConnected(getApplicationContext())) {
                downloadLivePvData(year, month, day);
            }
        }
        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

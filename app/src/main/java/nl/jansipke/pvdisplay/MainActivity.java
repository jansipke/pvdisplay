package nl.jansipke.pvdisplay;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.jansipke.pvdisplay.data.LivePvDatum;
import nl.jansipke.pvdisplay.database.PvDataOperations;
import nl.jansipke.pvdisplay.parsers.PvOutputParser;

public class MainActivity extends AppCompatActivity {

    private final static String API_KEY = "4054f46dd2c8e71855122e964c8e099cb9394d69";
    private final static String SYSTEM_ID = "23329";
    private final static String URL = "http://pvoutput.org/service/r2/getstatus.jsp?d=20160724&h=1&limit=288&asc=1";
//    private final static String URL = "http://pvoutput.org/service/r2/getoutput.jsp?df=20160101&dt=20160801&limit=50";
//    private final static String URL = "http://pvoutput.org/service/r2/getstatistic.jsp";

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void fetch(View view) {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadTask().execute();
        } else {
            Toast.makeText(MainActivity.this, "No network connectivity", Toast.LENGTH_SHORT).show();
        }
    }

    public void showLiveGraph(View view) {
        Intent intent = new Intent(MainActivity.this, LiveActivity.class);
        startActivity(intent);
    }

    private class DownloadTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... empty) {
            try {
                Map<String, String> headers = new HashMap<>();
                headers.put("X-Pvoutput-Apikey", API_KEY);
                headers.put("X-Pvoutput-SystemId", SYSTEM_ID);
                return httpGet(URL, headers);
            } catch (IOException e) {
                Toast.makeText(MainActivity.this, "Error fetching data from pvoutput.org", Toast.LENGTH_LONG).show();
                return "ERROR: Unable to retrieve URL " + URL;
            }
        }
        @Override
        protected void onPostExecute(String result) {
            PvDataOperations pvDataOperations = new PvDataOperations(getApplicationContext());
            PvOutputParser pvOutputParser = new PvOutputParser();

            try {
                List<LivePvDatum> livePvData = pvOutputParser.parseLive(result);
                for (LivePvDatum livePvDatum : livePvData) {
                    pvDataOperations.saveLive(livePvDatum);
                }
            } catch (ParseException e) {
                Toast.makeText(MainActivity.this, "Error parsing data from pvoutput.org", Toast.LENGTH_LONG).show();
            }


//            LivePvDatum livePvDatum = pvDataOperations.loadLive("20160724").get(50);
//            textView.setText(livePvDatum.toString());

//            List<HistoricalPvDatum> historicalPvData = pvOutputParser.parseHistorical(result);
//            for (HistoricalPvDatum historicalPvDatum : historicalPvData) {
//                pvDataOperations.saveHistorical(historicalPvDatum);
//            }

//            HistoricalPvDatum historicalPvDatum = pvDataOperations.loadHistorical("20160724");
//            textView.setText(historicalPvDatum.toString());

//            StatisticPvDatum statisticPvDatum = pvOutputParser.parseStatistic(result);
//            textView.setText(statisticPvDatum.toString());
        }
    }

    private String httpGet(String urlString, Map<String, String> headers) throws IOException {
        InputStream is = null;

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                conn.addRequestProperty(entry.getKey(), entry.getValue());
            }
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            is = conn.getInputStream();
            return inputStreamToString(is);

        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    private String inputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        return sb.toString();
    }
}

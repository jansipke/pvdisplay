package nl.jansipke.pvdisplay.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import nl.jansipke.pvdisplay.R;
import nl.jansipke.pvdisplay.data.HistoricalPvDatum;
import nl.jansipke.pvdisplay.data.LivePvDatum;
import nl.jansipke.pvdisplay.data.StatisticPvDatum;
import nl.jansipke.pvdisplay.data.SystemPvDatum;
import nl.jansipke.pvdisplay.utils.DateTimeUtils;

public class PvDataOperations {

    private final static String TAG = PvDataOperations.class.getSimpleName();

    private final Context context;
    private final PvDataHelper pvDataHelper;

    public PvDataOperations(Context context) {
        this.context = context;
        this.pvDataHelper = new PvDataHelper(context);
    }

    public HistoricalPvDatum loadHistorical(int year, int month, int day) {
        Log.i(TAG, "Loading historical PV data for " + DateTimeUtils.formatDate(year, month, day, true));
        SQLiteDatabase db = pvDataHelper.getReadableDatabase();

        String[] projection = {
                PvDataContract.HistoricalPvData.COLUMN_NAME_YEAR,
                PvDataContract.HistoricalPvData.COLUMN_NAME_MONTH,
                PvDataContract.HistoricalPvData.COLUMN_NAME_DAY,
                PvDataContract.HistoricalPvData.COLUMN_NAME_ENERGY_GENERATED
        };
        String sortOrder =
                PvDataContract.HistoricalPvData.COLUMN_NAME_YEAR + " ASC," +
                PvDataContract.HistoricalPvData.COLUMN_NAME_MONTH + " ASC," +
                PvDataContract.HistoricalPvData.COLUMN_NAME_DAY + " ASC";
        String selection =
                PvDataContract.HistoricalPvData.COLUMN_NAME_YEAR + "=? AND " +
                PvDataContract.HistoricalPvData.COLUMN_NAME_MONTH + "=? AND " +
                PvDataContract.HistoricalPvData.COLUMN_NAME_DAY + "=?";
        String[] selectionArgs = {
                "" + year,
                "" + month,
                "" + day
        };

        HistoricalPvDatum historicalPvDatum = new HistoricalPvDatum(year, month, day, 0);
        Cursor cursor = db.query(PvDataContract.HistoricalPvData.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                historicalPvDatum = new HistoricalPvDatum(
                        year,
                        month,
                        day,
                        cursor.getDouble(cursor.getColumnIndex(PvDataContract.HistoricalPvData.COLUMN_NAME_ENERGY_GENERATED)));
            }
            cursor.close();
            Log.i(TAG, "Loaded 1 row");
        } else {
            Log.i(TAG, "Loaded 0 rows");
        }
        db.close();

        return historicalPvDatum;
    }

    public List<LivePvDatum> loadLive(int year, int month, int day) {
        Log.i(TAG, "Loading live PV data for " + DateTimeUtils.formatDate(year, month, day, true));
        List<LivePvDatum> livePvData = new ArrayList<>();
        SQLiteDatabase db = pvDataHelper.getReadableDatabase();

        String[] projection = {
                PvDataContract.LivePvData.COLUMN_NAME_YEAR,
                PvDataContract.LivePvData.COLUMN_NAME_MONTH,
                PvDataContract.LivePvData.COLUMN_NAME_DAY,
                PvDataContract.LivePvData.COLUMN_NAME_HOUR,
                PvDataContract.LivePvData.COLUMN_NAME_MINUTE,
                PvDataContract.LivePvData.COLUMN_NAME_ENERGY_GENERATION,
                PvDataContract.LivePvData.COLUMN_NAME_POWER_GENERATION
        };
        String sortOrder =
                PvDataContract.LivePvData.COLUMN_NAME_YEAR + " ASC," +
                PvDataContract.LivePvData.COLUMN_NAME_MONTH + " ASC," +
                PvDataContract.LivePvData.COLUMN_NAME_DAY + " ASC," +
                PvDataContract.LivePvData.COLUMN_NAME_HOUR + " ASC," +
                PvDataContract.LivePvData.COLUMN_NAME_MINUTE + " ASC";
        String selection =
                PvDataContract.LivePvData.COLUMN_NAME_YEAR + "=? AND " +
                PvDataContract.LivePvData.COLUMN_NAME_MONTH + "=? AND " +
                PvDataContract.LivePvData.COLUMN_NAME_DAY + "=?";
        String[] selectionArgs = {
                "" + year,
                "" + month,
                "" + day
        };

        Cursor cursor = db.query(PvDataContract.LivePvData.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    livePvData.add(new LivePvDatum(
                            year,
                            month,
                            day,
                            cursor.getInt(cursor.getColumnIndex(PvDataContract.LivePvData.COLUMN_NAME_HOUR)),
                            cursor.getInt(cursor.getColumnIndex(PvDataContract.LivePvData.COLUMN_NAME_MINUTE)),
                            cursor.getDouble(cursor.getColumnIndex(PvDataContract.LivePvData.COLUMN_NAME_ENERGY_GENERATION)),
                            cursor.getDouble(cursor.getColumnIndex(PvDataContract.LivePvData.COLUMN_NAME_POWER_GENERATION))));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();
        Log.i(TAG, "Loaded " + livePvData.size() + " rows");

        return livePvData;
    }

    public StatisticPvDatum loadStatistic() {
        Log.i(TAG, "Loading statistic PV data");

        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.preferences_pv_data_file),
                Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(context.getString(R.string.preferences_object_statistic), null);
        if (json != null) {
            Log.i(TAG, "Loaded from preferences");
            return new Gson().fromJson(json, StatisticPvDatum.class);
        } else {
            Log.i(TAG, "No statistic data found in preferences");
            return null;
        }
    }

    public SystemPvDatum loadSystem() {
        Log.i(TAG, "Loading system PV data");

        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.preferences_pv_data_file),
                Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(context.getString(R.string.preferences_object_system), null);
        if (json != null) {
            Log.i(TAG, "Loaded from preferences");
            return new Gson().fromJson(json, SystemPvDatum.class);
        } else {
            Log.i(TAG, "No system data found in preferences");
            return null;
        }
    }

    public void saveHistorical(HistoricalPvDatum historicalPvDatum) {
        // TODO Change to saving of list; use one transaction
        Log.i(TAG, "Saving historical PV data");
        SQLiteDatabase db = pvDataHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PvDataContract.HistoricalPvData.COLUMN_NAME_YEAR, historicalPvDatum.getYear());
        values.put(PvDataContract.HistoricalPvData.COLUMN_NAME_MONTH, historicalPvDatum.getMonth());
        values.put(PvDataContract.HistoricalPvData.COLUMN_NAME_DAY, historicalPvDatum.getDay());
        values.put(PvDataContract.HistoricalPvData.COLUMN_NAME_ENERGY_GENERATED, historicalPvDatum.getEnergyGenerated());

        db.replace(PvDataContract.HistoricalPvData.TABLE_NAME, null, values);
        db.close();

        Log.i(TAG, "Saved 1 row");
    }

    public void saveLive(List<LivePvDatum> livePvData) {
        Log.i(TAG, "Saving live PV data");
        SQLiteDatabase db = pvDataHelper.getWritableDatabase();

        db.beginTransaction();
        String sql = "REPLACE INTO " + PvDataContract.LivePvData.TABLE_NAME +
                "(" + PvDataContract.LivePvData.COLUMN_NAME_YEAR +
                "," + PvDataContract.LivePvData.COLUMN_NAME_MONTH +
                "," + PvDataContract.LivePvData.COLUMN_NAME_DAY +
                "," + PvDataContract.LivePvData.COLUMN_NAME_HOUR +
                "," + PvDataContract.LivePvData.COLUMN_NAME_MINUTE +
                "," + PvDataContract.LivePvData.COLUMN_NAME_ENERGY_GENERATION +
                "," + PvDataContract.LivePvData.COLUMN_NAME_POWER_GENERATION +
                ") VALUES (?,?,?,?,?,?,?);";
        SQLiteStatement statement = db.compileStatement(sql);
        for (LivePvDatum livePvDatum : livePvData) {
            statement.clearBindings();
            statement.bindLong(1, livePvDatum.getYear());
            statement.bindLong(2, livePvDatum.getMonth());
            statement.bindLong(3, livePvDatum.getDay());
            statement.bindLong(4, livePvDatum.getHour());
            statement.bindLong(5, livePvDatum.getMinute());
            statement.bindDouble(6, livePvDatum.getEnergyGeneration());
            statement.bindDouble(7, livePvDatum.getPowerGeneration());
            statement.execute();
        }
        db.setTransactionSuccessful();
        db.endTransaction();

        db.close();
        Log.i(TAG, "Saved " + livePvData.size() + " rows");
    }

    public void saveStatistic(StatisticPvDatum statisticPvDatum) {
        Log.i(TAG, "Saving statistic PV data");

        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.preferences_pv_data_file),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = new Gson().toJson(statisticPvDatum);
        editor.putString(context.getString(R.string.preferences_object_statistic), json);
        editor.apply();

        Log.i(TAG, "Saved to preferences");
    }

    public void saveSystem(SystemPvDatum systemPvDatum) {
        Log.i(TAG, "Saving system PV data");

        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.preferences_pv_data_file),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = new Gson().toJson(systemPvDatum);
        editor.putString(context.getString(R.string.preferences_object_system), json);
        editor.apply();

        Log.i(TAG, "Saved to preferences");
    }
}

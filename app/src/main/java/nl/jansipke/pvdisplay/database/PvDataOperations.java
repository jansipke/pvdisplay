package nl.jansipke.pvdisplay.database;

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
import nl.jansipke.pvdisplay.data.DayPvDatum;
import nl.jansipke.pvdisplay.data.LivePvDatum;
import nl.jansipke.pvdisplay.data.MonthPvDatum;
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

    public List<DayPvDatum> loadDay(int year, int month) {
        Log.d(TAG, "Loading day PV data for " + DateTimeUtils.formatYearMonth(year, month, true));
        SQLiteDatabase db = pvDataHelper.getReadableDatabase();

        String[] projection = {
                PvDataContract.DayPvData.COLUMN_NAME_DAY,
                PvDataContract.DayPvData.COLUMN_NAME_ENERGY_GENERATED,
                PvDataContract.DayPvData.COLUMN_NAME_PEAK_POWER,
                PvDataContract.DayPvData.COLUMN_NAME_CONDITION
        };
        String sortOrder =
                PvDataContract.DayPvData.COLUMN_NAME_DAY + " ASC";
        String selection =
                PvDataContract.DayPvData.COLUMN_NAME_YEAR + "=? AND " +
                PvDataContract.DayPvData.COLUMN_NAME_MONTH + "=?";
        String[] selectionArgs = {
                "" + year,
                "" + month
        };

        List<DayPvDatum> dayPvData = new ArrayList<>();
        Cursor cursor = db.query(PvDataContract.DayPvData.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    dayPvData.add(new DayPvDatum(
                            year,
                            month,
                            cursor.getInt(cursor.getColumnIndex(PvDataContract.DayPvData.COLUMN_NAME_DAY)),
                            cursor.getDouble(cursor.getColumnIndex(PvDataContract.DayPvData.COLUMN_NAME_ENERGY_GENERATED)),
                            cursor.getDouble(cursor.getColumnIndex(PvDataContract.DayPvData.COLUMN_NAME_PEAK_POWER)),
                            cursor.getString(cursor.getColumnIndex(PvDataContract.DayPvData.COLUMN_NAME_CONDITION))));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();
        Log.d(TAG, "Loaded " + dayPvData.size() + " rows");

        return dayPvData;
    }

    public List<LivePvDatum> loadLive(int year, int month, int day) {
        Log.d(TAG, "Loading live PV data for " + DateTimeUtils.formatYearMonthDay(year, month, day, true));
        List<LivePvDatum> livePvData = new ArrayList<>();
        SQLiteDatabase db = pvDataHelper.getReadableDatabase();

        String[] projection = {
                PvDataContract.LivePvData.COLUMN_NAME_HOUR,
                PvDataContract.LivePvData.COLUMN_NAME_MINUTE,
                PvDataContract.LivePvData.COLUMN_NAME_ENERGY_GENERATION,
                PvDataContract.LivePvData.COLUMN_NAME_POWER_GENERATION
        };
        String sortOrder =
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
        Log.d(TAG, "Loaded " + livePvData.size() + " rows");

        return livePvData;
    }

    public List<MonthPvDatum> loadMonth(int year) {
        Log.d(TAG, "Loading month PV data for " + year);
        SQLiteDatabase db = pvDataHelper.getReadableDatabase();

        String[] projection = {
                PvDataContract.MonthPvData.COLUMN_NAME_MONTH,
                PvDataContract.MonthPvData.COLUMN_NAME_ENERGY_GENERATED
        };
        String sortOrder =
                PvDataContract.MonthPvData.COLUMN_NAME_MONTH + " ASC";
        String selection =
                PvDataContract.MonthPvData.COLUMN_NAME_YEAR + "=?";
        String[] selectionArgs = {
                "" + year
        };

        List<MonthPvDatum> monthPvData = new ArrayList<>();
        Cursor cursor = db.query(PvDataContract.MonthPvData.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    monthPvData.add(new MonthPvDatum(
                            year,
                            cursor.getInt(cursor.getColumnIndex(PvDataContract.MonthPvData.COLUMN_NAME_MONTH)),
                            cursor.getDouble(cursor.getColumnIndex(PvDataContract.DayPvData.COLUMN_NAME_ENERGY_GENERATED))));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();
        Log.d(TAG, "Loaded " + monthPvData.size() + " rows");

        return monthPvData;
    }

    public StatisticPvDatum loadStatistic() {
        Log.d(TAG, "Loading statistic PV data");

        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.preferences_pv_data_file),
                Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(context.getString(R.string.preferences_object_statistic), null);
        if (json != null) {
            Log.d(TAG, "Loaded from preferences");
            return new Gson().fromJson(json, StatisticPvDatum.class);
        } else {
            Log.d(TAG, "No statistic data found in preferences");
            return null;
        }
    }

    public SystemPvDatum loadSystem() {
        Log.d(TAG, "Loading system PV data");

        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.preferences_pv_data_file),
                Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(context.getString(R.string.preferences_object_system), null);
        if (json != null) {
            Log.d(TAG, "Loaded from preferences");
            return new Gson().fromJson(json, SystemPvDatum.class);
        } else {
            Log.d(TAG, "No system data found in preferences");
            return null;
        }
    }

    public void saveDay(List<DayPvDatum> dayPvData) {
        Log.d(TAG, "Saving day PV data");
        SQLiteDatabase db = pvDataHelper.getWritableDatabase();

        db.beginTransaction();
        String sql = "REPLACE INTO " + PvDataContract.DayPvData.TABLE_NAME +
                "(" + PvDataContract.DayPvData.COLUMN_NAME_YEAR +
                "," + PvDataContract.DayPvData.COLUMN_NAME_MONTH +
                "," + PvDataContract.DayPvData.COLUMN_NAME_DAY +
                "," + PvDataContract.DayPvData.COLUMN_NAME_ENERGY_GENERATED +
                "," + PvDataContract.DayPvData.COLUMN_NAME_PEAK_POWER +
                "," + PvDataContract.DayPvData.COLUMN_NAME_CONDITION +
                ") VALUES (?,?,?,?,?,?);";
        SQLiteStatement statement = db.compileStatement(sql);
        for (DayPvDatum dayPvDatum : dayPvData) {
            statement.clearBindings();
            statement.bindLong(1, dayPvDatum.getYear());
            statement.bindLong(2, dayPvDatum.getMonth());
            statement.bindLong(3, dayPvDatum.getDay());
            statement.bindDouble(4, dayPvDatum.getEnergyGenerated());
            statement.bindDouble(5, dayPvDatum.getPeakPower());
            statement.bindString(6, dayPvDatum.getCondition());
            statement.execute();
        }
        db.setTransactionSuccessful();
        db.endTransaction();

        db.close();
        Log.d(TAG, "Saved " + dayPvData.size() + " rows");
    }

    public void saveLive(List<LivePvDatum> livePvData) {
        Log.d(TAG, "Saving live PV data");
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
        Log.d(TAG, "Saved " + livePvData.size() + " rows");
    }

    public void saveMonth(List<MonthPvDatum> monthPvData) {
        Log.d(TAG, "Saving month PV data");
        SQLiteDatabase db = pvDataHelper.getWritableDatabase();

        db.beginTransaction();
        String sql = "REPLACE INTO " + PvDataContract.MonthPvData.TABLE_NAME +
                "(" + PvDataContract.DayPvData.COLUMN_NAME_YEAR +
                "," + PvDataContract.DayPvData.COLUMN_NAME_MONTH +
                "," + PvDataContract.DayPvData.COLUMN_NAME_ENERGY_GENERATED +
                ") VALUES (?,?,?);";
        SQLiteStatement statement = db.compileStatement(sql);
        for (MonthPvDatum monthPvDatum : monthPvData) {
            statement.clearBindings();
            statement.bindLong(1, monthPvDatum.getYear());
            statement.bindLong(2, monthPvDatum.getMonth());
            statement.bindDouble(3, monthPvDatum.getEnergyGenerated());
            statement.execute();
        }
        db.setTransactionSuccessful();
        db.endTransaction();

        db.close();
        Log.d(TAG, "Saved " + monthPvData.size() + " rows");
    }

    public void saveStatistic(StatisticPvDatum statisticPvDatum) {
        Log.d(TAG, "Saving statistic PV data");

        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.preferences_pv_data_file),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = new Gson().toJson(statisticPvDatum);
        editor.putString(context.getString(R.string.preferences_object_statistic), json);
        editor.apply();

        Log.d(TAG, "Saved to preferences");
    }

    public void saveSystem(SystemPvDatum systemPvDatum) {
        Log.d(TAG, "Saving system PV data");

        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.preferences_pv_data_file),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = new Gson().toJson(systemPvDatum);
        editor.putString(context.getString(R.string.preferences_object_system), json);
        editor.apply();

        Log.d(TAG, "Saved to preferences");
    }
}

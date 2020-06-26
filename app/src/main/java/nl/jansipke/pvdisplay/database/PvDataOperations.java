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
import nl.jansipke.pvdisplay.data.DailyPvDatum;
import nl.jansipke.pvdisplay.data.LivePvDatum;
import nl.jansipke.pvdisplay.data.MonthlyPvDatum;
import nl.jansipke.pvdisplay.data.RecordPvDatum;
import nl.jansipke.pvdisplay.data.StatisticPvDatum;
import nl.jansipke.pvdisplay.data.SystemPvDatum;
import nl.jansipke.pvdisplay.data.YearlyPvDatum;
import nl.jansipke.pvdisplay.utils.DateTimeUtils;

public class PvDataOperations {

    private final static String TAG = PvDataOperations.class.getSimpleName();

    private final Context context;
    private final PvDataHelper pvDataHelper;

    public PvDataOperations(Context context) {
        this.context = context;
        this.pvDataHelper = new PvDataHelper(context);
    }

    public List<DailyPvDatum> loadDaily(DateTimeUtils.YearMonth ym) {
        SQLiteDatabase db = pvDataHelper.getReadableDatabase();

        String[] projection = {
                PvDataContract.DailyPvData.COLUMN_NAME_DAY,
                PvDataContract.DailyPvData.COLUMN_NAME_ENERGY_GENERATED,
                PvDataContract.DailyPvData.COLUMN_NAME_PEAK_POWER,
                PvDataContract.DailyPvData.COLUMN_NAME_CONDITION
        };
        String sortOrder =
                PvDataContract.DailyPvData.COLUMN_NAME_DAY + " ASC";
        String selection =
                PvDataContract.DailyPvData.COLUMN_NAME_YEAR + "=? AND " +
                PvDataContract.DailyPvData.COLUMN_NAME_MONTH + "=?";
        String[] selectionArgs = {
                "" + ym.year,
                "" + ym.month
        };

        List<DailyPvDatum> dailyPvData = new ArrayList<>();
        Cursor cursor = db.query(
                PvDataContract.DailyPvData.TABLE_NAME,
                projection, selection, selectionArgs,null, null, sortOrder);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    dailyPvData.add(new DailyPvDatum(
                            ym.year,
                            ym.month,
                            cursor.getInt(cursor.getColumnIndex(PvDataContract.DailyPvData.COLUMN_NAME_DAY)),
                            cursor.getDouble(cursor.getColumnIndex(PvDataContract.DailyPvData.COLUMN_NAME_ENERGY_GENERATED)),
                            cursor.getDouble(cursor.getColumnIndex(PvDataContract.DailyPvData.COLUMN_NAME_PEAK_POWER)),
                            cursor.getString(cursor.getColumnIndex(PvDataContract.DailyPvData.COLUMN_NAME_CONDITION))));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();
        Log.d(TAG, "Loaded " + dailyPvData.size() + " rows of daily PV data for " + ym + " from database");

        return dailyPvData;
    }

    public List<LivePvDatum> loadLive(DateTimeUtils.YearMonthDay ymd) {
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
                "" + ymd.year,
                "" + ymd.month,
                "" + ymd.day
        };

        Cursor cursor = db.query(
                PvDataContract.LivePvData.TABLE_NAME,
                projection, selection, selectionArgs, null, null, sortOrder);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    livePvData.add(new LivePvDatum(
                            ymd.year,
                            ymd.month,
                            ymd.day,
                            cursor.getInt(cursor.getColumnIndex(PvDataContract.LivePvData.COLUMN_NAME_HOUR)),
                            cursor.getInt(cursor.getColumnIndex(PvDataContract.LivePvData.COLUMN_NAME_MINUTE)),
                            cursor.getDouble(cursor.getColumnIndex(PvDataContract.LivePvData.COLUMN_NAME_ENERGY_GENERATION)),
                            cursor.getDouble(cursor.getColumnIndex(PvDataContract.LivePvData.COLUMN_NAME_POWER_GENERATION))));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();
        Log.d(TAG, "Loaded " + livePvData.size() + " rows of live PV data for " + ymd + " from database");

        return livePvData;
    }

    public List<MonthlyPvDatum> loadMonthly(DateTimeUtils.Year y) {
        SQLiteDatabase db = pvDataHelper.getReadableDatabase();

        String[] projection = {
                PvDataContract.MonthlyPvData.COLUMN_NAME_MONTH,
                PvDataContract.MonthlyPvData.COLUMN_NAME_ENERGY_GENERATED
        };
        String sortOrder =
                PvDataContract.MonthlyPvData.COLUMN_NAME_MONTH + " ASC";
        String selection =
                PvDataContract.MonthlyPvData.COLUMN_NAME_YEAR + "=?";
        String[] selectionArgs = {
                "" + y.year
        };

        List<MonthlyPvDatum> monthlyPvData = new ArrayList<>();
        Cursor cursor = db.query(
                PvDataContract.MonthlyPvData.TABLE_NAME,
                projection, selection, selectionArgs, null, null, sortOrder);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    monthlyPvData.add(new MonthlyPvDatum(
                            y.year,
                            cursor.getInt(cursor.getColumnIndex(PvDataContract.MonthlyPvData.COLUMN_NAME_MONTH)),
                            cursor.getDouble(cursor.getColumnIndex(PvDataContract.MonthlyPvData.COLUMN_NAME_ENERGY_GENERATED))));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();
        Log.d(TAG, "Loaded " + monthlyPvData.size() + " rows of monthly PV data for " + y + " from database");

        return monthlyPvData;
    }

    public RecordPvDatum loadRecord() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.preferences_pv_data_file),
                Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(context.getString(R.string.preferences_object_record), null);
        if (json != null) {
            Log.d(TAG, "Loaded record PV data from preferences");
            return new Gson().fromJson(json, RecordPvDatum.class);
        } else {
            Log.d(TAG, "No record PV data found in preferences");
            return new RecordPvDatum(0, 0, 0, 0);
        }
    }

    public StatisticPvDatum loadStatistic() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.preferences_pv_data_file),
                Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(context.getString(R.string.preferences_object_statistic), null);
        if (json != null) {
            Log.d(TAG, "Loaded statistic PV data from preferences");
            return new Gson().fromJson(json, StatisticPvDatum.class);
        } else {
            Log.d(TAG, "No statistic PV data found in preferences");
            return null;
        }
    }

    public SystemPvDatum loadSystem() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.preferences_pv_data_file),
                Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(context.getString(R.string.preferences_object_system), null);
        if (json != null) {
            Log.d(TAG, "Loaded system PV data from preferences");
            return new Gson().fromJson(json, SystemPvDatum.class);
        } else {
            Log.d(TAG, "No system PV data found in preferences");
            return null;
        }
    }

    public List<YearlyPvDatum> loadYearly() {
        SQLiteDatabase db = pvDataHelper.getReadableDatabase();

        String[] projection = {
                PvDataContract.YearlyPvData.COLUMN_NAME_YEAR,
                PvDataContract.YearlyPvData.COLUMN_NAME_ENERGY_GENERATED
        };
        String sortOrder =
                PvDataContract.YearlyPvData.COLUMN_NAME_YEAR + " ASC";

        List<YearlyPvDatum> yearlyPvData = new ArrayList<>();
        Cursor cursor = db.query(
                PvDataContract.YearlyPvData.TABLE_NAME,
                projection, null, null, null, null, sortOrder);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    yearlyPvData.add(new YearlyPvDatum(
                            cursor.getInt(cursor.getColumnIndex(PvDataContract.YearlyPvData.COLUMN_NAME_YEAR)),
                            cursor.getDouble(cursor.getColumnIndex(PvDataContract.YearlyPvData.COLUMN_NAME_ENERGY_GENERATED))));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();
        Log.d(TAG, "Loaded " + yearlyPvData.size() + " rows of yearly PV data from database");

        return yearlyPvData;
    }

    public void saveDaily(List<DailyPvDatum> dailyPvData) {
        SQLiteDatabase db = pvDataHelper.getWritableDatabase();

        db.beginTransaction();
        String sql = "REPLACE INTO " + PvDataContract.DailyPvData.TABLE_NAME +
                "(" + PvDataContract.DailyPvData.COLUMN_NAME_YEAR +
                "," + PvDataContract.DailyPvData.COLUMN_NAME_MONTH +
                "," + PvDataContract.DailyPvData.COLUMN_NAME_DAY +
                "," + PvDataContract.DailyPvData.COLUMN_NAME_ENERGY_GENERATED +
                "," + PvDataContract.DailyPvData.COLUMN_NAME_PEAK_POWER +
                "," + PvDataContract.DailyPvData.COLUMN_NAME_CONDITION +
                ") VALUES (?,?,?,?,?,?);";
        SQLiteStatement statement = db.compileStatement(sql);
        double maxEnergyGenerated = Double.MIN_VALUE;
        for (DailyPvDatum dailyPvDatum : dailyPvData) {
            statement.clearBindings();
            statement.bindLong(1, dailyPvDatum.getYear());
            statement.bindLong(2, dailyPvDatum.getMonth());
            statement.bindLong(3, dailyPvDatum.getDay());
            statement.bindDouble(4, dailyPvDatum.getEnergyGenerated());
            statement.bindDouble(5, dailyPvDatum.getPeakPower());
            statement.bindString(6, dailyPvDatum.getCondition());
            statement.execute();
            maxEnergyGenerated = Math.max(maxEnergyGenerated, dailyPvDatum.getEnergyGenerated());
        }
        db.setTransactionSuccessful();
        db.endTransaction();

        db.close();
        Log.d(TAG, "Saved " + dailyPvData.size() + " rows of daily PV data to database");

        RecordPvDatum recordPvDatum = loadRecord();
        if (maxEnergyGenerated > recordPvDatum.getDailyEnergyGenerated()) {
            recordPvDatum.setDailyEnergyGenerated(maxEnergyGenerated);
            saveRecord(recordPvDatum);
        }
    }

    public void saveLive(List<LivePvDatum> livePvData) {
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
        double maxPowerGeneration = Double.MIN_VALUE;
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
            maxPowerGeneration = Math.max(maxPowerGeneration, livePvDatum.getPowerGeneration());
        }
        db.setTransactionSuccessful();
        db.endTransaction();

        db.close();
        Log.d(TAG, "Saved " + livePvData.size() + " rows of live PV data to database");

        RecordPvDatum recordPvDatum = loadRecord();
        if (maxPowerGeneration > recordPvDatum.getLivePowerGeneration()) {
            recordPvDatum.setLivePowerGeneration(maxPowerGeneration);
            saveRecord(recordPvDatum);
        }
    }

    public void saveMonthly(List<MonthlyPvDatum> monthlyPvData) {
        SQLiteDatabase db = pvDataHelper.getWritableDatabase();

        db.beginTransaction();
        String sql = "REPLACE INTO " + PvDataContract.MonthlyPvData.TABLE_NAME +
                "(" + PvDataContract.MonthlyPvData.COLUMN_NAME_YEAR +
                "," + PvDataContract.MonthlyPvData.COLUMN_NAME_MONTH +
                "," + PvDataContract.MonthlyPvData.COLUMN_NAME_ENERGY_GENERATED +
                ") VALUES (?,?,?);";
        SQLiteStatement statement = db.compileStatement(sql);
        double maxEnergyGenerated = Double.MIN_VALUE;
        for (MonthlyPvDatum monthlyPvDatum : monthlyPvData) {
            statement.clearBindings();
            statement.bindLong(1, monthlyPvDatum.getYear());
            statement.bindLong(2, monthlyPvDatum.getMonth());
            statement.bindDouble(3, monthlyPvDatum.getEnergyGenerated());
            statement.execute();
            maxEnergyGenerated = Math.max(maxEnergyGenerated, monthlyPvDatum.getEnergyGenerated());
        }
        db.setTransactionSuccessful();
        db.endTransaction();

        db.close();
        Log.d(TAG, "Saved " + monthlyPvData.size() + " rows of monthly PV data to database");

        RecordPvDatum recordPvDatum = loadRecord();
        if (maxEnergyGenerated > recordPvDatum.getMonthlyEnergyGenerated()) {
            recordPvDatum.setMonthlyEnergyGenerated(maxEnergyGenerated);
            saveRecord(recordPvDatum);
        }
    }

    public void saveRecord(RecordPvDatum recordPvDatum) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.preferences_pv_data_file),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = new Gson().toJson(recordPvDatum);
        editor.putString(context.getString(R.string.preferences_object_record), json);
        editor.apply();

        Log.d(TAG, "Saved record PV data to preferences");
    }

    public void saveStatistic(StatisticPvDatum statisticPvDatum) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.preferences_pv_data_file),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = new Gson().toJson(statisticPvDatum);
        editor.putString(context.getString(R.string.preferences_object_statistic), json);
        editor.apply();

        Log.d(TAG, "Saved statistic PV data to preferences");
    }

    public void saveSystem(SystemPvDatum systemPvDatum) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.preferences_pv_data_file),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = new Gson().toJson(systemPvDatum);
        editor.putString(context.getString(R.string.preferences_object_system), json);
        editor.apply();

        Log.d(TAG, "Saved system PV data to preferences");
    }

    public void saveYearly(List<YearlyPvDatum> yearlyPvData) {
        SQLiteDatabase db = pvDataHelper.getWritableDatabase();

        db.beginTransaction();
        String sql = "REPLACE INTO " + PvDataContract.YearlyPvData.TABLE_NAME +
                "(" + PvDataContract.YearlyPvData.COLUMN_NAME_YEAR +
                "," + PvDataContract.YearlyPvData.COLUMN_NAME_ENERGY_GENERATED +
                ") VALUES (?,?);";
        SQLiteStatement statement = db.compileStatement(sql);
        double maxEnergyGenerated = Double.MIN_VALUE;
        for (YearlyPvDatum yearlyPvDatum : yearlyPvData) {
            statement.clearBindings();
            statement.bindLong(1, yearlyPvDatum.getYear());
            statement.bindDouble(2, yearlyPvDatum.getEnergyGenerated());
            statement.execute();
            maxEnergyGenerated = Math.max(maxEnergyGenerated, yearlyPvDatum.getEnergyGenerated());
        }
        db.setTransactionSuccessful();
        db.endTransaction();

        db.close();
        Log.d(TAG, "Saved " + yearlyPvData.size() + " rows of yearly PV data to database");

        RecordPvDatum recordPvDatum = loadRecord();
        if (maxEnergyGenerated > recordPvDatum.getYearlyEnergyGenerated()) {
            recordPvDatum.setYearlyEnergyGenerated(maxEnergyGenerated);
            saveRecord(recordPvDatum);
        }
    }
}

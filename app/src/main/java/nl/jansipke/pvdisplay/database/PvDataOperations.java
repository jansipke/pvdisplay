package nl.jansipke.pvdisplay.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import nl.jansipke.pvdisplay.data.HistoricalPvDatum;
import nl.jansipke.pvdisplay.data.LivePvDatum;
import nl.jansipke.pvdisplay.data.StatisticPvDatum;
import nl.jansipke.pvdisplay.utils.DateTimeUtils;

public class PvDataOperations {

    private final static String TAG = PvDataOperations.class.getSimpleName();

    private PvDataHelper pvDataHelper;

    public PvDataOperations(Context context) {
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
        SQLiteDatabase db = pvDataHelper.getReadableDatabase();

        String[] projection = {
                PvDataContract.StatisticPvData.COLUMN_NAME_ENERGY_GENERATED,
                PvDataContract.StatisticPvData.COLUMN_NAME_AVERAGE_GENERATION,
                PvDataContract.StatisticPvData.COLUMN_NAME_MINIMUM_GENERATION,
                PvDataContract.StatisticPvData.COLUMN_NAME_MAXIMUM_GENERATION,
                PvDataContract.StatisticPvData.COLUMN_NAME_OUTPUTS,
                PvDataContract.StatisticPvData.COLUMN_NAME_ACTUAL_DATE_FROM_YEAR,
                PvDataContract.StatisticPvData.COLUMN_NAME_ACTUAL_DATE_FROM_MONTH,
                PvDataContract.StatisticPvData.COLUMN_NAME_ACTUAL_DATE_FROM_DAY,
                PvDataContract.StatisticPvData.COLUMN_NAME_ACTUAL_DATE_TO_YEAR,
                PvDataContract.StatisticPvData.COLUMN_NAME_ACTUAL_DATE_TO_MONTH,
                PvDataContract.StatisticPvData.COLUMN_NAME_ACTUAL_DATE_TO_DAY,
                PvDataContract.StatisticPvData.COLUMN_NAME_RECORD_DATE_YEAR,
                PvDataContract.StatisticPvData.COLUMN_NAME_RECORD_DATE_MONTH,
                PvDataContract.StatisticPvData.COLUMN_NAME_RECORD_DATE_DAY
        };

        StatisticPvDatum statisticPvDatum = new StatisticPvDatum(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        Cursor cursor = db.query(PvDataContract.StatisticPvData.TABLE_NAME, projection, null, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                statisticPvDatum = new StatisticPvDatum(
                        cursor.getDouble(cursor.getColumnIndex(PvDataContract.StatisticPvData.COLUMN_NAME_ENERGY_GENERATED)),
                        cursor.getDouble(cursor.getColumnIndex(PvDataContract.StatisticPvData.COLUMN_NAME_AVERAGE_GENERATION)),
                        cursor.getDouble(cursor.getColumnIndex(PvDataContract.StatisticPvData.COLUMN_NAME_MINIMUM_GENERATION)),
                        cursor.getDouble(cursor.getColumnIndex(PvDataContract.StatisticPvData.COLUMN_NAME_MAXIMUM_GENERATION)),
                        cursor.getInt(cursor.getColumnIndex(PvDataContract.StatisticPvData.COLUMN_NAME_OUTPUTS)),
                        cursor.getInt(cursor.getColumnIndex(PvDataContract.StatisticPvData.COLUMN_NAME_ACTUAL_DATE_FROM_YEAR)),
                        cursor.getInt(cursor.getColumnIndex(PvDataContract.StatisticPvData.COLUMN_NAME_ACTUAL_DATE_FROM_MONTH)),
                        cursor.getInt(cursor.getColumnIndex(PvDataContract.StatisticPvData.COLUMN_NAME_ACTUAL_DATE_FROM_DAY)),
                        cursor.getInt(cursor.getColumnIndex(PvDataContract.StatisticPvData.COLUMN_NAME_ACTUAL_DATE_TO_YEAR)),
                        cursor.getInt(cursor.getColumnIndex(PvDataContract.StatisticPvData.COLUMN_NAME_ACTUAL_DATE_TO_MONTH)),
                        cursor.getInt(cursor.getColumnIndex(PvDataContract.StatisticPvData.COLUMN_NAME_ACTUAL_DATE_TO_DAY)),
                        cursor.getInt(cursor.getColumnIndex(PvDataContract.StatisticPvData.COLUMN_NAME_RECORD_DATE_YEAR)),
                        cursor.getInt(cursor.getColumnIndex(PvDataContract.StatisticPvData.COLUMN_NAME_RECORD_DATE_MONTH)),
                        cursor.getInt(cursor.getColumnIndex(PvDataContract.StatisticPvData.COLUMN_NAME_RECORD_DATE_DAY)));
            }
            cursor.close();
            Log.i(TAG, "Loaded 1 row");
        } else {
            Log.i(TAG, "Loaded 0 rows");
        }
        db.close();

        return statisticPvDatum;
    }

    public void saveHistorical(HistoricalPvDatum historicalPvDatum) {
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
        SQLiteDatabase db = pvDataHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PvDataContract.StatisticPvData.COLUMN_NAME_ENERGY_GENERATED, statisticPvDatum.getEnergyGenerated());
        values.put(PvDataContract.StatisticPvData.COLUMN_NAME_AVERAGE_GENERATION, statisticPvDatum.getAverageGeneration());
        values.put(PvDataContract.StatisticPvData.COLUMN_NAME_MINIMUM_GENERATION, statisticPvDatum.getMinimumGeneration());
        values.put(PvDataContract.StatisticPvData.COLUMN_NAME_MAXIMUM_GENERATION, statisticPvDatum.getMaximumGeneration());
        values.put(PvDataContract.StatisticPvData.COLUMN_NAME_OUTPUTS, statisticPvDatum.getOutputs());
        values.put(PvDataContract.StatisticPvData.COLUMN_NAME_ACTUAL_DATE_FROM_YEAR, statisticPvDatum.getActualDateFromYear());
        values.put(PvDataContract.StatisticPvData.COLUMN_NAME_ACTUAL_DATE_FROM_MONTH, statisticPvDatum.getActualDateFromMonth());
        values.put(PvDataContract.StatisticPvData.COLUMN_NAME_ACTUAL_DATE_FROM_DAY, statisticPvDatum.getActualDateFromDay());
        values.put(PvDataContract.StatisticPvData.COLUMN_NAME_ACTUAL_DATE_TO_YEAR, statisticPvDatum.getActualDateToYear());
        values.put(PvDataContract.StatisticPvData.COLUMN_NAME_ACTUAL_DATE_TO_MONTH, statisticPvDatum.getActualDateToMonth());
        values.put(PvDataContract.StatisticPvData.COLUMN_NAME_ACTUAL_DATE_TO_DAY, statisticPvDatum.getActualDateToDay());
        values.put(PvDataContract.StatisticPvData.COLUMN_NAME_RECORD_DATE_YEAR, statisticPvDatum.getRecordDateYear());
        values.put(PvDataContract.StatisticPvData.COLUMN_NAME_RECORD_DATE_MONTH, statisticPvDatum.getRecordDateMonth());
        values.put(PvDataContract.StatisticPvData.COLUMN_NAME_RECORD_DATE_DAY, statisticPvDatum.getRecordDateDay());

        db.replace(PvDataContract.StatisticPvData.TABLE_NAME, null, values);

        db.close();
        Log.i(TAG, "Saved 1 row");
    }
}

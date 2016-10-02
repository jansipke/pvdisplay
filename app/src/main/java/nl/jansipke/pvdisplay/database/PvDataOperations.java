package nl.jansipke.pvdisplay.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import nl.jansipke.pvdisplay.data.HistoricalPvDatum;
import nl.jansipke.pvdisplay.data.LivePvDatum;

public class PvDataOperations {

    private final static String TAG = "PvDataOperations";

    private PvDataHelper pvDataHelper;

    public PvDataOperations(Context context) {
        this.pvDataHelper = new PvDataHelper(context);
    }

    public HistoricalPvDatum loadHistorical(int year, int month, int day) {
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
                double energyGenerated = cursor.getDouble(cursor.getColumnIndex(PvDataContract.HistoricalPvData.COLUMN_NAME_ENERGY_GENERATED));
                historicalPvDatum = new HistoricalPvDatum(year, month, day, energyGenerated);
            }
        }
        db.close();

        return historicalPvDatum;
    }

    public List<LivePvDatum> loadLive(int year, int month, int day) {
        Log.i(TAG, "Loading live PV data for year=" + year + ", month=" + month + ", day=" + day);
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
                    int hour = cursor.getInt(cursor.getColumnIndex(PvDataContract.LivePvData.COLUMN_NAME_HOUR));
                    int minute = cursor.getInt(cursor.getColumnIndex(PvDataContract.LivePvData.COLUMN_NAME_MINUTE));
                    double energyGeneration = cursor.getDouble(cursor.getColumnIndex(PvDataContract.LivePvData.COLUMN_NAME_ENERGY_GENERATION));
                    double powerGeneration = cursor.getDouble(cursor.getColumnIndex(PvDataContract.LivePvData.COLUMN_NAME_POWER_GENERATION));
                    livePvData.add(new LivePvDatum(year, month, day, hour, minute, energyGeneration, powerGeneration));
                } while (cursor.moveToNext());
            }
        }
        db.close();
        Log.i(TAG, "Fetched " + livePvData.size() + " rows");

        return livePvData;
    }

    public void saveHistorical(HistoricalPvDatum historicalPvDatum) {
        SQLiteDatabase db = pvDataHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PvDataContract.HistoricalPvData.COLUMN_NAME_YEAR, historicalPvDatum.getYear());
        values.put(PvDataContract.HistoricalPvData.COLUMN_NAME_MONTH, historicalPvDatum.getMonth());
        values.put(PvDataContract.HistoricalPvData.COLUMN_NAME_DAY, historicalPvDatum.getDay());
        values.put(PvDataContract.HistoricalPvData.COLUMN_NAME_ENERGY_GENERATED, historicalPvDatum.getEnergyGenerated());

        db.replace(PvDataContract.HistoricalPvData.TABLE_NAME, null, values);
        db.close();
    }

    public void saveLive(List<LivePvDatum> livePvData) {
        Log.i(TAG, "Saving live PV data");
        SQLiteDatabase db = pvDataHelper.getWritableDatabase();

        int rowsInserted = 0;
        for (LivePvDatum livePvDatum : livePvData) {
            ContentValues values = new ContentValues();
            values.put(PvDataContract.LivePvData.COLUMN_NAME_YEAR, livePvDatum.getYear());
            values.put(PvDataContract.LivePvData.COLUMN_NAME_MONTH, livePvDatum.getMonth());
            values.put(PvDataContract.LivePvData.COLUMN_NAME_DAY, livePvDatum.getDay());
            values.put(PvDataContract.LivePvData.COLUMN_NAME_HOUR, livePvDatum.getHour());
            values.put(PvDataContract.LivePvData.COLUMN_NAME_MINUTE, livePvDatum.getMinute());
            values.put(PvDataContract.LivePvData.COLUMN_NAME_ENERGY_GENERATION, livePvDatum.getEnergyGeneration());
            values.put(PvDataContract.LivePvData.COLUMN_NAME_POWER_GENERATION, livePvDatum.getPowerGeneration());

            db.replace(PvDataContract.LivePvData.TABLE_NAME, null, values);
            rowsInserted += 1;
        }
        db.close();
        Log.i(TAG, "Inserted " + rowsInserted + " rows");
    }
}

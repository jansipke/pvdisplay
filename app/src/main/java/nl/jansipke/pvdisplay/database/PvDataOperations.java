package nl.jansipke.pvdisplay.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import nl.jansipke.pvdisplay.data.HistoricalPvDatum;
import nl.jansipke.pvdisplay.data.LivePvDatum;
import nl.jansipke.pvdisplay.data.YearMonthDay;

public class PvDataOperations {

    private PvDataHelper pvDataHelper;

    public PvDataOperations(Context context) {
        this.pvDataHelper = new PvDataHelper(context);
    }

    public HistoricalPvDatum loadHistorical(YearMonthDay yearMonthDay) {
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
                "" + yearMonthDay.getYear(),
                "" + yearMonthDay.getMonth(),
                "" + yearMonthDay.getDay()
        };

        HistoricalPvDatum historicalPvDatum = new HistoricalPvDatum(yearMonthDay, 0);
        Cursor cursor = db.query(PvDataContract.HistoricalPvData.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                double energyGenerated = cursor.getDouble(cursor.getColumnIndex(PvDataContract.HistoricalPvData.COLUMN_NAME_ENERGY_GENERATED));
                historicalPvDatum = new HistoricalPvDatum(yearMonthDay, energyGenerated);
            }
        }
        db.close();

        return historicalPvDatum;
    }

    public List<LivePvDatum> loadLive(YearMonthDay yearMonthDay) {
        List<LivePvDatum> livePvData = new ArrayList<>();
        SQLiteDatabase db = pvDataHelper.getReadableDatabase();

        String[] projection = {
                PvDataContract.LivePvData.COLUMN_NAME_TIMESTAMP,
                PvDataContract.LivePvData.COLUMN_NAME_ENERGY_GENERATION,
                PvDataContract.LivePvData.COLUMN_NAME_POWER_GENERATION
        };
        String sortOrder =
                PvDataContract.LivePvData.COLUMN_NAME_TIMESTAMP + " ASC";
        String selection =
                PvDataContract.LivePvData.COLUMN_NAME_TIMESTAMP + ">=? AND " +
                PvDataContract.LivePvData.COLUMN_NAME_TIMESTAMP + "<?";
        String[] selectionArgs = {
                "" + yearMonthDay.getFirstTimestamp(),
                "" + yearMonthDay.getLastTimestamp()
        };

        Cursor cursor = db.query(PvDataContract.LivePvData.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    long timestamp = cursor.getLong(cursor.getColumnIndex(PvDataContract.LivePvData.COLUMN_NAME_TIMESTAMP));
                    double energyGeneration = cursor.getDouble(cursor.getColumnIndex(PvDataContract.LivePvData.COLUMN_NAME_ENERGY_GENERATION));
                    double powerGeneration = cursor.getDouble(cursor.getColumnIndex(PvDataContract.LivePvData.COLUMN_NAME_POWER_GENERATION));
                    livePvData.add(new LivePvDatum(timestamp, energyGeneration, powerGeneration));
                } while (cursor.moveToNext());
            }
        }
        db.close();

        return livePvData;
    }

    public void saveHistorical(HistoricalPvDatum historicalPvDatum) {
        SQLiteDatabase db = pvDataHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PvDataContract.HistoricalPvData.COLUMN_NAME_YEAR, historicalPvDatum.getYearMonthDay().getYear());
        values.put(PvDataContract.HistoricalPvData.COLUMN_NAME_MONTH, historicalPvDatum.getYearMonthDay().getMonth());
        values.put(PvDataContract.HistoricalPvData.COLUMN_NAME_DAY, historicalPvDatum.getYearMonthDay().getDay());
        values.put(PvDataContract.HistoricalPvData.COLUMN_NAME_ENERGY_GENERATED, historicalPvDatum.getEnergyGenerated());

        db.replace(PvDataContract.HistoricalPvData.TABLE_NAME, PvDataContract.HistoricalPvData.COLUMN_NAME_YEAR, values); // TODO Check if second parameter is correct
        db.close();
    }

    public void saveLive(LivePvDatum livePvDatum) {
        SQLiteDatabase db = pvDataHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PvDataContract.LivePvData.COLUMN_NAME_TIMESTAMP, livePvDatum.getTimestamp());
        values.put(PvDataContract.LivePvData.COLUMN_NAME_ENERGY_GENERATION, livePvDatum.getEnergyGeneration());
        values.put(PvDataContract.LivePvData.COLUMN_NAME_POWER_GENERATION, livePvDatum.getPowerGeneration());

        db.replace(PvDataContract.LivePvData.TABLE_NAME, PvDataContract.LivePvData.COLUMN_NAME_TIMESTAMP, values);
        db.close();
    }
}

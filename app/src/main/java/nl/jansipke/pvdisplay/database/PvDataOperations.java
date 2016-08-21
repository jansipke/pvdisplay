package nl.jansipke.pvdisplay.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import nl.jansipke.pvdisplay.data.HistoricalPvDatum;
import nl.jansipke.pvdisplay.data.LivePvDatum;

public class PvDataOperations {

    private PvDataHelper pvDataHelper;

    public PvDataOperations(Context context) {
        this.pvDataHelper = new PvDataHelper(context);
    }

    public HistoricalPvDatum loadHistorical(String date) {
        SQLiteDatabase db = pvDataHelper.getReadableDatabase();

        String[] projection = {
                PvDataContract.HistoricalPvData.COLUMN_NAME_DATE,
                PvDataContract.HistoricalPvData.COLUMN_NAME_ENERGY_GENERATED};
        String sortOrder = PvDataContract.HistoricalPvData.COLUMN_NAME_DATE + " ASC";
        String selection = PvDataContract.HistoricalPvData.COLUMN_NAME_DATE + " = ?";
        String[] selectionArgs = { date };

        HistoricalPvDatum historicalPvDatum = new HistoricalPvDatum(date, 0);
        Cursor cursor = db.query(PvDataContract.HistoricalPvData.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                double energyGenerated = cursor.getDouble(cursor.getColumnIndex(PvDataContract.HistoricalPvData.COLUMN_NAME_ENERGY_GENERATED));
                historicalPvDatum = new HistoricalPvDatum(date, energyGenerated);
            }
        }
        db.close();

        return historicalPvDatum;
    }

    public List<LivePvDatum> loadLive(String date) {
        List<LivePvDatum> livePvData = new ArrayList<>();
        SQLiteDatabase db = pvDataHelper.getReadableDatabase();

        String[] projection = {
                PvDataContract.LivePvData.COLUMN_NAME_DATE,
                PvDataContract.LivePvData.COLUMN_NAME_TIME,
                PvDataContract.LivePvData.COLUMN_NAME_ENERGY_GENERATION,
                PvDataContract.LivePvData.COLUMN_NAME_POWER_GENERATION};
        String sortOrder = PvDataContract.LivePvData.COLUMN_NAME_TIME + " ASC";
        String selection = PvDataContract.LivePvData.COLUMN_NAME_DATE + " = ?";
        String[] selectionArgs = { date };

        Cursor cursor = db.query(PvDataContract.LivePvData.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String time = cursor.getString(cursor.getColumnIndex(PvDataContract.LivePvData.COLUMN_NAME_TIME));
                    double energyGeneration = cursor.getDouble(cursor.getColumnIndex(PvDataContract.LivePvData.COLUMN_NAME_ENERGY_GENERATION));
                    double powerGeneration = cursor.getDouble(cursor.getColumnIndex(PvDataContract.LivePvData.COLUMN_NAME_POWER_GENERATION));
                    livePvData.add(new LivePvDatum(date, time, energyGeneration, powerGeneration));
                } while (cursor.moveToNext());
            }
        }
        db.close();

        return livePvData;
    }

    public void saveHistorical(HistoricalPvDatum historicalPvDatum) {
        SQLiteDatabase db = pvDataHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PvDataContract.HistoricalPvData.COLUMN_NAME_DATE, historicalPvDatum.getDate());
        values.put(PvDataContract.HistoricalPvData.COLUMN_NAME_ENERGY_GENERATED, historicalPvDatum.getEnergyGenerated());

        db.replace(PvDataContract.HistoricalPvData.TABLE_NAME, PvDataContract.HistoricalPvData.COLUMN_NAME_DATE, values);
        db.close();
    }

    public void saveLive(LivePvDatum livePvDatum) {
        SQLiteDatabase db = pvDataHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PvDataContract.LivePvData.COLUMN_NAME_DATE, livePvDatum.getDate());
        values.put(PvDataContract.LivePvData.COLUMN_NAME_TIME, livePvDatum.getTime());
        values.put(PvDataContract.LivePvData.COLUMN_NAME_ENERGY_GENERATION, livePvDatum.getEnergyGeneration());
        values.put(PvDataContract.LivePvData.COLUMN_NAME_POWER_GENERATION, livePvDatum.getPowerGeneration());

        db.replace(PvDataContract.LivePvData.TABLE_NAME, PvDataContract.LivePvData.COLUMN_NAME_DATE, values);
        db.close();
    }
}

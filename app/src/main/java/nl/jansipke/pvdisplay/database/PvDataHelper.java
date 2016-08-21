package nl.jansipke.pvdisplay.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PvDataHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "PvData.db";
    public static final int DATABASE_VERSION = 1;

    public PvDataHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + PvDataContract.LivePvData.TABLE_NAME + " (" +
                PvDataContract.LivePvData._ID + " INTEGER PRIMARY KEY," +
                PvDataContract.LivePvData.COLUMN_NAME_DATE + " TEXT," +
                PvDataContract.LivePvData.COLUMN_NAME_TIME + " TEXT," +
                PvDataContract.LivePvData.COLUMN_NAME_ENERGY_GENERATION + " REAL," +
                PvDataContract.LivePvData.COLUMN_NAME_POWER_GENERATION + " REAL," +
                " UNIQUE(" +
                PvDataContract.LivePvData.COLUMN_NAME_DATE + "," +
                PvDataContract.LivePvData.COLUMN_NAME_TIME + "))");
        db.execSQL("CREATE TABLE " + PvDataContract.HistoricalPvData.TABLE_NAME + " (" +
                PvDataContract.HistoricalPvData._ID + " INTEGER PRIMARY KEY," +
                PvDataContract.HistoricalPvData.COLUMN_NAME_DATE + " TEXT," +
                PvDataContract.HistoricalPvData.COLUMN_NAME_ENERGY_GENERATED + " REAL," +
                " UNIQUE(" +
                PvDataContract.HistoricalPvData.COLUMN_NAME_DATE + "))");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PvDataContract.LivePvData.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PvDataContract.HistoricalPvData.TABLE_NAME);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}

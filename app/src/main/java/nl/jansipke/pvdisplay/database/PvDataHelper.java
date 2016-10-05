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
                PvDataContract.LivePvData.COLUMN_NAME_YEAR + " INTEGER," +
                PvDataContract.LivePvData.COLUMN_NAME_MONTH + " INTEGER," +
                PvDataContract.LivePvData.COLUMN_NAME_DAY + " INTEGER," +
                PvDataContract.LivePvData.COLUMN_NAME_HOUR + " INTEGER," +
                PvDataContract.LivePvData.COLUMN_NAME_MINUTE + " INTEGER," +
                PvDataContract.LivePvData.COLUMN_NAME_ENERGY_GENERATION + " REAL," +
                PvDataContract.LivePvData.COLUMN_NAME_POWER_GENERATION + " REAL," +
                " UNIQUE(" +
                PvDataContract.LivePvData.COLUMN_NAME_YEAR + "," +
                PvDataContract.LivePvData.COLUMN_NAME_MONTH + "," +
                PvDataContract.LivePvData.COLUMN_NAME_DAY + "," +
                PvDataContract.LivePvData.COLUMN_NAME_HOUR + "," +
                PvDataContract.LivePvData.COLUMN_NAME_MINUTE + "))");

        db.execSQL("CREATE TABLE " + PvDataContract.HistoricalPvData.TABLE_NAME + " (" +
                PvDataContract.HistoricalPvData._ID + " INTEGER PRIMARY KEY," +
                PvDataContract.HistoricalPvData.COLUMN_NAME_YEAR + " INTEGER," +
                PvDataContract.HistoricalPvData.COLUMN_NAME_MONTH + " INTEGER," +
                PvDataContract.HistoricalPvData.COLUMN_NAME_DAY + " INTEGER," +
                PvDataContract.HistoricalPvData.COLUMN_NAME_ENERGY_GENERATED + " REAL," +
                " UNIQUE(" +
                PvDataContract.HistoricalPvData.COLUMN_NAME_YEAR + "," +
                PvDataContract.HistoricalPvData.COLUMN_NAME_MONTH + "," +
                PvDataContract.HistoricalPvData.COLUMN_NAME_DAY + "))");

        db.execSQL("CREATE TABLE " + PvDataContract.StatisticPvData.TABLE_NAME + " (" +
                PvDataContract.StatisticPvData._ID + " INTEGER PRIMARY KEY," +
                PvDataContract.StatisticPvData.COLUMN_NAME_ENERGY_GENERATED + " REAL," +
                PvDataContract.StatisticPvData.COLUMN_NAME_AVERAGE_GENERATION + " REAL," +
                PvDataContract.StatisticPvData.COLUMN_NAME_MINIMUM_GENERATION + " REAL," +
                PvDataContract.StatisticPvData.COLUMN_NAME_MAXIMUM_GENERATION + " REAL," +
                PvDataContract.StatisticPvData.COLUMN_NAME_OUTPUTS + " INTEGER," +
                PvDataContract.StatisticPvData.COLUMN_NAME_ACTUAL_DATE_FROM_YEAR + " INTEGER," +
                PvDataContract.StatisticPvData.COLUMN_NAME_ACTUAL_DATE_FROM_MONTH + " INTEGER," +
                PvDataContract.StatisticPvData.COLUMN_NAME_ACTUAL_DATE_FROM_DAY + " INTEGER," +
                PvDataContract.StatisticPvData.COLUMN_NAME_ACTUAL_DATE_TO_YEAR + " INTEGER," +
                PvDataContract.StatisticPvData.COLUMN_NAME_ACTUAL_DATE_TO_MONTH + " INTEGER," +
                PvDataContract.StatisticPvData.COLUMN_NAME_ACTUAL_DATE_TO_DAY + " INTEGER," +
                PvDataContract.StatisticPvData.COLUMN_NAME_RECORD_DATE_YEAR + " INTEGER," +
                PvDataContract.StatisticPvData.COLUMN_NAME_RECORD_DATE_MONTH + " INTEGER," +
                PvDataContract.StatisticPvData.COLUMN_NAME_RECORD_DATE_DAY + " INTEGER)");

        db.execSQL("CREATE TABLE " + PvDataContract.SystemPvData.TABLE_NAME + " (" +
                PvDataContract.StatisticPvData._ID + " INTEGER PRIMARY KEY," +
                PvDataContract.SystemPvData.COLUMN_NAME_SYSTEM_NAME + " TEXT," +
                PvDataContract.SystemPvData.COLUMN_NAME_SYSTEM_SIZE + " INTEGER," +
                PvDataContract.SystemPvData.COLUMN_NAME_NUMBER_OF_PANELS + " INTEGER," +
                PvDataContract.SystemPvData.COLUMN_NAME_PANEL_POWER + " INTEGER," +
                PvDataContract.SystemPvData.COLUMN_NAME_PANEL_BRAND + " TEXT," +
                PvDataContract.SystemPvData.COLUMN_NAME_INVERTER_POWER + " INTEGER," +
                PvDataContract.SystemPvData.COLUMN_NAME_INVERTER_BRAND + " TEXT," +
                PvDataContract.SystemPvData.COLUMN_NAME_LATITUDE + " REAL," +
                PvDataContract.SystemPvData.COLUMN_NAME_LONGITUDE + " REAL)");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PvDataContract.HistoricalPvData.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PvDataContract.LivePvData.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PvDataContract.StatisticPvData.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PvDataContract.SystemPvData.TABLE_NAME);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}

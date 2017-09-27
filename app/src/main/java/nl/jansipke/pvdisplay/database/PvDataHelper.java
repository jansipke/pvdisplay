package nl.jansipke.pvdisplay.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class PvDataHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "PvData.db";
    private static final int DATABASE_VERSION = 1;
    private final static String TAG = PvDataHelper.class.getSimpleName();

    PvDataHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database tables");
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

        db.execSQL("CREATE TABLE " + PvDataContract.DailyPvData.TABLE_NAME + " (" +
                PvDataContract.DailyPvData._ID + " INTEGER PRIMARY KEY," +
                PvDataContract.DailyPvData.COLUMN_NAME_YEAR + " INTEGER," +
                PvDataContract.DailyPvData.COLUMN_NAME_MONTH + " INTEGER," +
                PvDataContract.DailyPvData.COLUMN_NAME_DAY + " INTEGER," +
                PvDataContract.DailyPvData.COLUMN_NAME_ENERGY_GENERATED + " REAL," +
                PvDataContract.DailyPvData.COLUMN_NAME_PEAK_POWER + " REAL," +
                PvDataContract.DailyPvData.COLUMN_NAME_CONDITION + " TEXT," +
                " UNIQUE(" +
                PvDataContract.DailyPvData.COLUMN_NAME_YEAR + "," +
                PvDataContract.DailyPvData.COLUMN_NAME_MONTH + "," +
                PvDataContract.DailyPvData.COLUMN_NAME_DAY + "))");

        db.execSQL("CREATE TABLE " + PvDataContract.MonthlyPvData.TABLE_NAME + " (" +
                PvDataContract.MonthlyPvData._ID + " INTEGER PRIMARY KEY," +
                PvDataContract.MonthlyPvData.COLUMN_NAME_YEAR + " INTEGER," +
                PvDataContract.MonthlyPvData.COLUMN_NAME_MONTH + " INTEGER," +
                PvDataContract.MonthlyPvData.COLUMN_NAME_ENERGY_GENERATED + " REAL," +
                " UNIQUE(" +
                PvDataContract.MonthlyPvData.COLUMN_NAME_YEAR + "," +
                PvDataContract.MonthlyPvData.COLUMN_NAME_MONTH + "))");

        db.execSQL("CREATE TABLE " + PvDataContract.YearlyPvData.TABLE_NAME + " (" +
                PvDataContract.YearlyPvData._ID + " INTEGER PRIMARY KEY," +
                PvDataContract.YearlyPvData.COLUMN_NAME_YEAR + " INTEGER," +
                PvDataContract.YearlyPvData.COLUMN_NAME_ENERGY_GENERATED + " REAL," +
                " UNIQUE(" +
                PvDataContract.YearlyPvData.COLUMN_NAME_YEAR + "))");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database tables");
        db.execSQL("DROP TABLE IF EXISTS " + PvDataContract.LivePvData.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PvDataContract.DailyPvData.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PvDataContract.MonthlyPvData.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PvDataContract.YearlyPvData.TABLE_NAME);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Downgrading database tables");
        onUpgrade(db, oldVersion, newVersion);
    }
}

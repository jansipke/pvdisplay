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

        db.execSQL("CREATE TABLE " + PvDataContract.DayPvData.TABLE_NAME + " (" +
                PvDataContract.DayPvData._ID + " INTEGER PRIMARY KEY," +
                PvDataContract.DayPvData.COLUMN_NAME_YEAR + " INTEGER," +
                PvDataContract.DayPvData.COLUMN_NAME_MONTH + " INTEGER," +
                PvDataContract.DayPvData.COLUMN_NAME_DAY + " INTEGER," +
                PvDataContract.DayPvData.COLUMN_NAME_ENERGY_GENERATED + " REAL," +
                PvDataContract.DayPvData.COLUMN_NAME_PEAK_POWER + " REAL," +
                PvDataContract.DayPvData.COLUMN_NAME_CONDITION + " TEXT," +
                " UNIQUE(" +
                PvDataContract.DayPvData.COLUMN_NAME_YEAR + "," +
                PvDataContract.DayPvData.COLUMN_NAME_MONTH + "," +
                PvDataContract.DayPvData.COLUMN_NAME_DAY + "))");

        db.execSQL("CREATE TABLE " + PvDataContract.MonthPvData.TABLE_NAME + " (" +
                PvDataContract.MonthPvData._ID + " INTEGER PRIMARY KEY," +
                PvDataContract.MonthPvData.COLUMN_NAME_YEAR + " INTEGER," +
                PvDataContract.MonthPvData.COLUMN_NAME_MONTH + " INTEGER," +
                PvDataContract.MonthPvData.COLUMN_NAME_ENERGY_GENERATED + " REAL," +
                " UNIQUE(" +
                PvDataContract.MonthPvData.COLUMN_NAME_YEAR + "," +
                PvDataContract.MonthPvData.COLUMN_NAME_MONTH + "))");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database tables");
        db.execSQL("DROP TABLE IF EXISTS " + PvDataContract.LivePvData.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PvDataContract.DayPvData.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PvDataContract.MonthPvData.TABLE_NAME);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Downgrading database tables");
        onUpgrade(db, oldVersion, newVersion);
    }
}

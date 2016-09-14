package nl.jansipke.pvdisplay.database;

import android.provider.BaseColumns;

public final class PvDataContract {
    public PvDataContract() {}

    public static abstract class LivePvData implements BaseColumns {
        public static final String TABLE_NAME = "live";
        public static final String COLUMN_NAME_YEAR = "year";
        public static final String COLUMN_NAME_MONTH = "month";
        public static final String COLUMN_NAME_DAY = "day";
        public static final String COLUMN_NAME_HOUR = "hour";
        public static final String COLUMN_NAME_MINUTE = "minute";
        public static final String COLUMN_NAME_ENERGY_GENERATION = "energy_generation";
        public static final String COLUMN_NAME_POWER_GENERATION = "power_generation";
    }

    public static abstract class HistoricalPvData implements BaseColumns {
        public static final String TABLE_NAME = "historical";
        public static final String COLUMN_NAME_YEAR = "year";
        public static final String COLUMN_NAME_MONTH = "month";
        public static final String COLUMN_NAME_DAY = "day";
        public static final String COLUMN_NAME_ENERGY_GENERATED = "energy_generated";
    }
}

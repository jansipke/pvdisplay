package nl.jansipke.pvdisplay.database;

import android.provider.BaseColumns;

final class PvDataContract {
    public PvDataContract() {}

    static abstract class HistoricalPvData implements BaseColumns {
        static final String TABLE_NAME = "historical";
        static final String COLUMN_NAME_YEAR = "year";
        static final String COLUMN_NAME_MONTH = "month";
        static final String COLUMN_NAME_DAY = "day";
        static final String COLUMN_NAME_DATE_NUMBER = "date_number";
        static final String COLUMN_NAME_ENERGY_GENERATED = "energy_generated";
        static final String COLUMN_NAME_PEAK_POWER = "peak_power";
        static final String COLUMN_NAME_CONDITION = "condition";
    }

    static abstract class LivePvData implements BaseColumns {
        static final String TABLE_NAME = "live";
        static final String COLUMN_NAME_YEAR = "year";
        static final String COLUMN_NAME_MONTH = "month";
        static final String COLUMN_NAME_DAY = "day";
        static final String COLUMN_NAME_HOUR = "hour";
        static final String COLUMN_NAME_MINUTE = "minute";
        static final String COLUMN_NAME_ENERGY_GENERATION = "energy_generation";
        static final String COLUMN_NAME_POWER_GENERATION = "power_generation";
    }
}

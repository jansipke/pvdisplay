package nl.jansipke.pvdisplay.database;

import android.provider.BaseColumns;

public final class PvDataContract {
    public PvDataContract() {}

    public static abstract class HistoricalPvData implements BaseColumns {
        public static final String TABLE_NAME = "historical";
        public static final String COLUMN_NAME_YEAR = "year";
        public static final String COLUMN_NAME_MONTH = "month";
        public static final String COLUMN_NAME_DAY = "day";
        public static final String COLUMN_NAME_ENERGY_GENERATED = "energy_generated";
    }

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

    public static abstract class StatisticPvData implements BaseColumns {
        public static final String TABLE_NAME = "statistic";
        public static final String COLUMN_NAME_ENERGY_GENERATED = "energy_generated";
        public static final String COLUMN_NAME_AVERAGE_GENERATION = "average_generation";
        public static final String COLUMN_NAME_MINIMUM_GENERATION = "minimum_generation";
        public static final String COLUMN_NAME_MAXIMUM_GENERATION = "maximum_generation";
        public static final String COLUMN_NAME_OUTPUTS = "outputs";
        public static final String COLUMN_NAME_ACTUAL_DATE_FROM_YEAR = "actual_date_from_year";
        public static final String COLUMN_NAME_ACTUAL_DATE_FROM_MONTH = "actual_date_from_month";
        public static final String COLUMN_NAME_ACTUAL_DATE_FROM_DAY = "actual_date_from_day";
        public static final String COLUMN_NAME_ACTUAL_DATE_TO_YEAR = "actual_date_to_year";
        public static final String COLUMN_NAME_ACTUAL_DATE_TO_MONTH = "actual_date_to_month";
        public static final String COLUMN_NAME_ACTUAL_DATE_TO_DAY = "actual_date_to_day";
        public static final String COLUMN_NAME_RECORD_DATE_YEAR = "record_date_year";
        public static final String COLUMN_NAME_RECORD_DATE_MONTH = "record_date_month";
        public static final String COLUMN_NAME_RECORD_DATE_DAY = "record_date_day";
    }

    public static abstract class SystemPvData implements BaseColumns {
        public static final String TABLE_NAME = "system";
        public static final String COLUMN_NAME_SYSTEM_NAME = "system_name";
        public static final String COLUMN_NAME_SYSTEM_SIZE = "system_size";
        public static final String COLUMN_NAME_NUMBER_OF_PANELS = "number_of_panels";
        public static final String COLUMN_NAME_PANEL_POWER = "panel_power";
        public static final String COLUMN_NAME_PANEL_BRAND = "panel_brand";
        public static final String COLUMN_NAME_INVERTER_POWER = "inverter_power";
        public static final String COLUMN_NAME_INVERTER_BRAND = "inverter_brand";
        public static final String COLUMN_NAME_LATITUDE = "latitude";
        public static final String COLUMN_NAME_LONGITUDE = "longitude";
    }
}

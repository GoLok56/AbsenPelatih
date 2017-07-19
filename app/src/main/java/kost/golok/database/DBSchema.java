package kost.golok.database;

import android.provider.BaseColumns;

public class DBSchema {

    private DBSchema(){}

    public static class School implements BaseColumns{
        public static final String TABLE_NAME = "sekolah";
        public static final String NAME_COLUMN = "nama_sekolah";
    }

    public static class Student implements BaseColumns{
        public static final String TABLE_NAME = "murid_";
        public static final String NAME_COLUMN = "nama_murid";
        public static final String CLASS_COLUMN = "kelas";
    }

    public static class Attendance implements BaseColumns{
        public static final String TABLE_NAME = "absen_";
        public static final String STUDENT_ID_COLUMN = "id_murid";
        public static final String DATE_COLUMN = "tanggal";
    }

}

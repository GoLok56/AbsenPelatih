package io.github.golok56.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "absen.db";
    private static final int DATABASE_VERSION = 1;

    private DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static SQLiteDatabase getDb(Context context) {
        return new DBHelper(context).getWritableDatabase();
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE_SEKOLAH_QUERY =
                "CREATE TABLE " + DBSchema.School.TABLE_NAME + " (" +
                DBSchema.School._ID + " INTEGER PRIMARY KEY, " +
                DBSchema.School.NAME_COLUMN + " TEXT);";
        db.execSQL(CREATE_TABLE_SEKOLAH_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}


    public static void createStudentTable(SQLiteDatabase db, String schoolName){
        String tableName = DBSchema.Student.TABLE_NAME + schoolName.replaceAll("\\s", "");
        String CREATE_QUERY = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                DBSchema.Student._ID + " INTEGER PRIMARY KEY, " +
                DBSchema.Student.NAME_COLUMN + " TEXT, " +
                DBSchema.Student.CLASS_COLUMN + " TEXT);";
        db.execSQL(CREATE_QUERY);
    }

    public static void createAttendanceTable(SQLiteDatabase db, String schoolName){
        String tableName = DBSchema.Attendance.TABLE_NAME + schoolName.replaceAll("\\s", "");
        String CREATE_QUERY = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                DBSchema.Attendance._ID + " INTEGER PRIMARY KEY, " +
                DBSchema.Attendance.STUDENT_ID_COLUMN + " INTEGER, " +
                DBSchema.Attendance.DATE_COLUMN + " DATE DEFAULT CURRENT_DATE);";
        db.execSQL(CREATE_QUERY);
    }
}

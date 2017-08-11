package io.github.golok56.controller;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import io.github.golok56.database.DBSchema;

public class AttendanceController extends Controller<Integer> {

    // The name of school this controller will work on
    private String mTableName;

    public AttendanceController(Context ctx, String schoolName) {
        super(ctx);
        mTableName = DBSchema.Attendance.TABLE_NAME + schoolName.replaceAll("\\s", "");
    }

    AttendanceController(SQLiteDatabase db, String schoolName){
        super(db);
        mTableName = DBSchema.Attendance.TABLE_NAME + schoolName.replaceAll("\\s", "");
    }

    // This will return null, since Attendance is not a model
    @Override
    public ArrayList<Integer> getList(String name) {
        return null;
    }

    // Insert a student's attendance to database with give id. Obj here act as an id
    @Override
    public boolean insert(Integer id) {
        ContentValues values = new ContentValues();
        values.put(DBSchema.Attendance.STUDENT_ID_COLUMN, id);
        return mDb.insert(mTableName, null, values) != -1;
    }

    @Override
    public void clear(String name) {
        mDb.delete(mTableName, null, null);
    }

    @Override
    public boolean delete(Integer id) {
        String[] selectionArgs = { String.valueOf(id) };
        return mDb.delete(mTableName, DBSchema.Attendance.STUDENT_ID_COLUMN + "=?", selectionArgs) != 0;
    }

    public String[] getAllAvailableMonth() throws ParseException {
        // Setting up the query
        String[] column = { DBSchema.Attendance.DATE_COLUMN };
        String groupby = "strftime('%m', " + column[0] + ")";
        // Read database with given query
        Cursor cursor = mDb.query(mTableName, column, null, null, groupby, null, null);
        // Specifying the format of the date with indonesian format
        Locale id = new Locale("in", "id");
        DateFormat dfFrom = new SimpleDateFormat("yyyy-MM-dd", id);
        DateFormat dfTo = new SimpleDateFormat("MMMM", id);
        // Add all the month from database to array
        String[] arr = new String[cursor.getCount()];
        int currentPos = 0;
        while (cursor.moveToNext()) {
            String bulan = dfTo.format(dfFrom.parse(cursor.getString(0)));
            arr[currentPos++] = bulan;
        }
        cursor.close();
        return arr;
    }

    public HashMap<String, Integer> getDateAndTotalAttendance() throws ParseException {
        // Setting up the query
        String[] column = { DBSchema.Attendance.DATE_COLUMN, "COUNT(*)" };
        String groupby = DBSchema.Attendance.DATE_COLUMN;
        // Read database with given query
        Cursor cursor = mDb.query(mTableName, column, null, null, groupby, null, null);
        // Specifying the format of the date with indonesian format
        Locale id = new Locale("in", "id");
        DateFormat dfFrom = new SimpleDateFormat("yyyy-MM-dd", id);
        DateFormat dfTo = new SimpleDateFormat("dd MMMM yyyy", id);
        // Add all the month from database to the map
        HashMap<String, Integer> map = new HashMap<>();
        while (cursor.moveToNext()) {
            String tanggal = dfTo.format(dfFrom.parse(cursor.getString(0)));
            map.put(tanggal, cursor.getInt(1));
        }
        cursor.close();
        return map;
    }

    public String getAttendanceInfo(String date, int studentId){
        // Specifying the format of the date with indonesian format
        Locale id = new Locale("in", "id");
        DateFormat dfFrom = new SimpleDateFormat("dd MMMM yyyy", id);
        DateFormat dfTo = new SimpleDateFormat("yyyy-MM-dd", id);
        String newDate = null;
        try {
            newDate = dfTo.format(dfFrom.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // Setting up the query
        String[] column = { DBSchema.Attendance.STUDENT_ID_COLUMN };
        String selection = DBSchema.Attendance.DATE_COLUMN + "=? AND "
                + DBSchema.Attendance.STUDENT_ID_COLUMN + "=?";
        String[] selectionArgs = { newDate, "" + studentId };
        // Reading the database
        Cursor cursor = mDb.query(mTableName, column, selection, selectionArgs, null, null, null);
        // Return the string value based on number of row found
        String absen = cursor.getCount() == 1 ? "Hadir" : "Tidak Hadir";
        cursor.close();
        return absen;
    }
}

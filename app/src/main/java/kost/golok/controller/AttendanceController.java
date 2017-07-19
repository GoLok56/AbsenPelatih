package kost.golok.controller;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import kost.golok.database.DBSchema;

public class AttendanceController extends Controller<Integer> {

    // The name of school this controller will work on
    private String mTableName;

    public AttendanceController(Context ctx, String schoolName) {
        super(ctx);
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
        // Not implemented
    }

    public String[] getAllAvailableMonth() throws ParseException {
        // Setting up the query
        String[] column = { DBSchema.Attendance.DATE_COLUMN };
        String groupby = "strftime('%m', " + column[0] + ")";
        // Read database with given query
        Cursor cursor = mDb.query(mTableName, column, null, null, groupby, null, null);
        // Specifying the format of the date with indonesian format
        Locale id = new Locale("in", "id");
        DateFormat dfFtom = new SimpleDateFormat("yyyy-MM-dd", id);
        DateFormat dfTo = new SimpleDateFormat("MMMM", id);
        // Add all the month from database to array
        String[] arr = new String[cursor.getCount()];
        int currentPos = 0;
        while (cursor.moveToNext()) {
            String tanggal = cursor.getString(0);
            String bulan = dfTo.format(dfFtom.parse(tanggal));
            arr[currentPos++] = bulan;
        }
        cursor.close();
        return arr;
    }

}

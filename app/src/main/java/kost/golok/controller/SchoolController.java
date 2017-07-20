package kost.golok.controller;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;

import kost.golok.database.DBHelper;
import kost.golok.database.DBSchema;
import kost.golok.object.School;

public class SchoolController extends Controller<School> {

    public SchoolController(Context ctx) {
        super(ctx);
    }

    @Override
    public ArrayList<School> getList(String name) {
        StudentController studentController = new StudentController(mDb);

        // Read all the data in School Table
        Cursor cursor = mDb.query(DBSchema.School.TABLE_NAME, null, null, null, null, null, null);

        ArrayList<School> list = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            // Retrieving the school data that was found in database
            int id = cursor.getInt(cursor.getColumnIndex(DBSchema.School._ID));
            String schoolName = cursor.getString(cursor.getColumnIndex(DBSchema.School.NAME_COLUMN));
            studentController.setSchoolName(schoolName);
            // Add new school to the list
            list.add(new School(id, schoolName, studentController.getList("")));
        }
        cursor.close();

        return cursor.getCount() == 0 ? null : list;
    }

    @Override
    public boolean insert(School school) {
        // Get the name of new school
        String schoolName = school.getSchoolName();

        ContentValues values = new ContentValues();
        values.put(DBSchema.School.NAME_COLUMN, schoolName);

        // Create a table student and attendance related to this school by name
        DBHelper.createAttendanceTable(mDb, schoolName);
        DBHelper.createStudentTable(mDb, schoolName);

        long row = mDb.insert(DBSchema.School.TABLE_NAME, null, values);
        return row != -1;
    }

    @Override
    public void clear(String schoolName){
        String namaTable = DBSchema.Attendance.TABLE_NAME + schoolName;
        mDb.delete(namaTable, null, null);
    }

    @Override
    public boolean delete(School school) {
        String[] selectionArgs = { String.valueOf(school.getId()) };
        if(mDb.delete(DBSchema.School.TABLE_NAME, "_id=?", selectionArgs) != 0){
            String sql = "DROP TABLE IF EXISTS ";
            String schoolName = school.getSchoolName().replaceAll("\\s", "");
            String studentTable = DBSchema.Student.TABLE_NAME + schoolName;
            String attendanceTable = DBSchema.Attendance.TABLE_NAME + schoolName;

            mDb.execSQL(sql + studentTable + ";");
            mDb.execSQL(sql + attendanceTable + ";");

            return true;
        }
        return false;
    }
}

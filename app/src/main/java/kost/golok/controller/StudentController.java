package kost.golok.controller;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import kost.golok.database.DBSchema;
import kost.golok.object.Student;

public class StudentController extends Controller<Student> {

    // The database of school that the student is coming from
    private String mTableName;

    // The name of the school the student come from
    private String mSchoolName;

    public StudentController(Context ctx, String schoolName) {
        super(ctx);
        mSchoolName = schoolName.replaceAll("\\s", "");
        mTableName = DBSchema.Student.TABLE_NAME  + mSchoolName;
    }

    StudentController(SQLiteDatabase db){
        super(db);
    }

    @Override
    // Get the student list from the database
    public ArrayList<Student> getList(String studentName) {
        // Setting up the query
        String selection = DBSchema.Student.NAME_COLUMN + " LIKE ?";
        String[] selectionArgs = { studentName + "%" };
        // Read the database with given query
        Cursor cursor = mDb.query(mTableName, null, selection, selectionArgs, null, null, null);

        ArrayList<Student> students = new ArrayList<>(cursor.getCount());
        while(cursor.moveToNext()){
            // Retrieving the student data that was found in database
            int id = cursor.getInt(cursor.getColumnIndex(DBSchema.Student._ID));
            String name = cursor.getString(cursor.getColumnIndex(DBSchema.Student.NAME_COLUMN));
            String studentClass = cursor.getString(cursor.getColumnIndex(DBSchema.Student.CLASS_COLUMN));
            // Add new student to the list
            students.add(new Student(id, name, studentClass, getTotalAttendance(id)));
        }
        cursor.close();
        return students;
    }

    @Override
    public boolean insert(Student student) {
        return false;
    }

    @Override
    public void clear(String name) {
        mDb.delete(mTableName, null, null);
    }

    private int getTotalAttendance(int idMurid){
        // Setting up the query
        String tableName = DBSchema.Attendance.TABLE_NAME + mSchoolName;
        String selection = DBSchema.Attendance.STUDENT_ID_COLUMN + "=?";
        String[] selectionArgs = { String.valueOf(idMurid) };

        // Read database with given query, the total row found is the total attendance of the student
        Cursor cursor = mDb.query(tableName, null, selection, selectionArgs, null, null, null);
        int totalAttendance = cursor.getCount();
        cursor.close();

        return totalAttendance;
    }

    void setSchoolName(String schoolName) {
        mSchoolName = schoolName.replaceAll("\\s", "");
        this.mTableName = DBSchema.Student.TABLE_NAME  + schoolName.replaceAll("\\s", "");
    }
}

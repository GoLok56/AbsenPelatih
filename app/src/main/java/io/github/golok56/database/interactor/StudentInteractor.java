package io.github.golok56.database.interactor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import io.github.golok56.callback.IOnBasicOperationCompleted;
import io.github.golok56.callback.IOnReadCompleted;
import io.github.golok56.callback.base.IBaseOnOperationCompleted;
import io.github.golok56.database.DBSchema;
import io.github.golok56.object.School;
import io.github.golok56.object.Student;
import io.github.golok56.utility.ValuesProvider;

public class StudentInteractor extends BaseInteractor<Student> {

    // The list of students that selected for some change
    public static final ArrayList<Student> sSelectedStudents = new ArrayList<>();

    // The database of school that the student is coming from
    private String mTableName;

    // The name of the school the student come from
    private String mSchoolName;

    public StudentInteractor(Context ctx, String schoolName) {
        super(ctx);
        mSchoolName = schoolName.replaceAll("\\s", "");
        mTableName = DBSchema.Student.TABLE_NAME  + mSchoolName;
    }

    StudentInteractor(SQLiteDatabase db){
        super(db);
    }

    @Override
    public ArrayList<Student> getList(String studentName, IOnReadCompleted callback) {
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
    private void insert(Student student, IBaseOnOperationCompleted callBack) {
        ContentValues values = ValuesProvider.get(student);
        int id = (int) mDb.insert(mTableName, null, values);
        student.setId(id);
    }

    public void insert(final ArrayList<Student> students, final IBaseOnOperationCompleted callBack){
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0, size = students.size(); i < size; i++) {
                    Student current = students.get(i);
                    insert(current, null);
                }
                callBack.onFinished();
            }
        }).start();
    }

    @Override
    public void clear(String name) {
        mDb.delete(mTableName, null, null);
    }

    @Override
    public void delete(Student student, IOnBasicOperationCompleted callback) {
        String[] selectionArgs = { String.valueOf(student.getId()) };
        if(mDb.delete(mTableName, "_id=?", selectionArgs) != 0){
            new AttendanceInteractor(mDb, mSchoolName).delete(student.getId());
        }
    }

    public void delete(final School school, final IBaseOnOperationCompleted callback){
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0, size = sSelectedStudents.size(); i < size; i++) {
                    Student current = sSelectedStudents.get(i);
                    delete(current, null);
                    school.remove(current);
                }
                sSelectedStudents.clear();
                callback.onFinished();
            }
        }).start();
    }

    // Retrieving the total attendance of a student with given id from database
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

    // Change the school name of the interactor with the new given value
    void setSchoolName(String schoolName) {
        mSchoolName = schoolName.replaceAll("\\s", "");
        mTableName = DBSchema.Student.TABLE_NAME  + schoolName.replaceAll("\\s", "");
    }
}

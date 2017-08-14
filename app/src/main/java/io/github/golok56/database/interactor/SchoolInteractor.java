package io.github.golok56.database.interactor;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.Nullable;

import java.util.ArrayList;

import io.github.golok56.callback.IOnReadCompleted;
import io.github.golok56.callback.base.IBaseOnOperationCompleted;
import io.github.golok56.database.DBHelper;
import io.github.golok56.database.DBSchema;
import io.github.golok56.object.School;
import io.github.golok56.utility.ValuesProvider;

public class SchoolInteractor extends BaseInteractor<School> {

    public SchoolInteractor(Context ctx) {
        super(ctx);
    }

    @Override
    public void getList(String name, final IOnReadCompleted<School> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                StudentInteractor studentInteractor = new StudentInteractor(mDb);

                Cursor cursor = mDb.query(
                        DBSchema.School.TABLE_NAME,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );

                ArrayList<School> schools = new ArrayList<>(cursor.getCount());
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndex(DBSchema.School._ID));
                    String schoolName = cursor.getString(cursor.getColumnIndex(DBSchema.School.NAME_COLUMN));
                    studentInteractor.setSchoolName(schoolName);
                    schools.add(new School(id, schoolName, studentInteractor.getStudents("")));
                }
                cursor.close();

                if (cursor.getCount() != 0) {
                    callback.onSuccess(schools);
                } else {
                    callback.onFinished();
                }
            }
        }).start();
    }

    @Override
    public void insert(School school) {
        String schoolName = school.getSchoolName();
        DBHelper.createAttendanceTable(mDb, schoolName);
        DBHelper.createStudentTable(mDb, schoolName);
        mDb.insert(DBSchema.School.TABLE_NAME, null, ValuesProvider.get(school));
    }

    public void insert(final School school, final IBaseOnOperationCompleted callBack) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                insert(school);
                callBack.onFinished();
            }
        }).start();
    }

    @Override
    public void clear(String name, @Nullable IBaseOnOperationCompleted callback) {
        String namaTable = DBSchema.Attendance.TABLE_NAME + name;
        mDb.delete(namaTable, null, null);
    }

    public void clear(final ArrayList<School> schools, final IBaseOnOperationCompleted callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0, size = schools.size(); i < size; ++i) {
                    clear(schools.get(i).getSchoolName().replaceAll("\\s", ""), null);
                }
                callback.onFinished();
            }
        }).start();
    }

    @Override
    public void delete(School school) {
        String[] selectionArgs = {String.valueOf(school.getId())};
        mDb.delete(DBSchema.School.TABLE_NAME, "_id=?", selectionArgs);

        String sql = "DROP TABLE IF EXISTS ";
        String schoolName = school.getSchoolName().replaceAll("\\s", "");
        String studentTable = DBSchema.Student.TABLE_NAME + schoolName;
        String attendanceTable = DBSchema.Attendance.TABLE_NAME + schoolName;

        mDb.execSQL(sql + studentTable + ";");
        mDb.execSQL(sql + attendanceTable + ";");

    }

    public void delete(final School school, final IBaseOnOperationCompleted callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                delete(school);
                callback.onFinished();
            }
        }).start();
    }
}

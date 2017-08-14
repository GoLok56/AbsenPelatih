package io.github.golok56.database.interactor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;

import io.github.golok56.callback.IOnReadCompleted;
import io.github.golok56.callback.IOnBasicOperationCompleted;
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

                ArrayList<School> list = new ArrayList<>(cursor.getCount());
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndex(DBSchema.School._ID));
                    String schoolName = cursor.getString(cursor.getColumnIndex(DBSchema.School.NAME_COLUMN));
                    studentInteractor.setSchoolName(schoolName);
                    list.add(new School(id, schoolName, studentInteractor.getList("")));
                }
                cursor.close();

                if (cursor.getCount() != 0) {
                    callback.onSuccess(list);
                } else {
                    callback.onFinished();
                }
            }
        }).start();
    }

    @Override
    public void insert(School school) {
        ContentValues values = ValuesProvider.get(school);

        String schoolName = school.getSchoolName();
        DBHelper.createAttendanceTable(mDb, schoolName);
        DBHelper.createStudentTable(mDb, schoolName);
        mDb.insert(DBSchema.School.TABLE_NAME, null, values);
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
    public void clear(String schoolName) {
        String namaTable = DBSchema.Attendance.TABLE_NAME + schoolName;
        mDb.delete(namaTable, null, null);
    }

    public void clear(final ArrayList<School> schools, final IOnBasicOperationCompleted callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0, size = schools.size(); i < size; ++i) {
                    clear(schools.get(i).getSchoolName().replaceAll("\\s", ""));
                }
                callback.onSuccess();
            }
        }).start();
    }

    @Override
    public void delete(final School school, final IOnBasicOperationCompleted callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String[] selectionArgs = {String.valueOf(school.getId())};
                if (mDb.delete(DBSchema.School.TABLE_NAME, "_id=?", selectionArgs) != 0) {
                    String sql = "DROP TABLE IF EXISTS ";
                    String schoolName = school.getSchoolName().replaceAll("\\s", "");
                    String studentTable = DBSchema.Student.TABLE_NAME + schoolName;
                    String attendanceTable = DBSchema.Attendance.TABLE_NAME + schoolName;

                    mDb.execSQL(sql + studentTable + ";");
                    mDb.execSQL(sql + attendanceTable + ";");
                    callback.onSuccess();
                } else {
                    callback.onFinished();
                }
            }
        }).start();
    }
}

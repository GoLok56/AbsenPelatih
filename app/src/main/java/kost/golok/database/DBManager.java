package kost.golok.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import static kost.golok.database.DBSchema.Student.NAME_COLUMN;

public class DBManager {

    private static DBManager sInstance;

    private SQLiteDatabase mDb;

    private DBManager(Context context){
        mDb = DBHelper.getDb(context);
    }

    public static DBManager getInstance(Context context){
        if(sInstance == null) sInstance = new DBManager(context);

        return sInstance;
    }

    public int insert(kost.golok.object.Student student, String namaSekolah){
        String namaTable = DBSchema.Student.TABLE_NAME + namaSekolah.replaceAll("\\s", "");
        return (int) mDb.insert(namaTable, null, contentValues(student));
    }

    private ContentValues contentValues(kost.golok.object.Student student){
        ContentValues values = new ContentValues();
        values.put(DBSchema.Student.CLASS_COLUMN, student.getKelas());
        values.put(NAME_COLUMN, student.getName());
        return values;
    }

}

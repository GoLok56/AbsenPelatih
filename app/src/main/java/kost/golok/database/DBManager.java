package kost.golok.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

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

    public Cursor read(String table, @Nullable String namaSekolah){
        String namaTable = table + (namaSekolah == null ? "" : namaSekolah.replaceAll("\\s", ""));
        return mDb.query(namaTable, null, null, null, null, null, null);
    }

    public Cursor readTanggal(String namaSekolah){
        String namaTable = DBSchema.Attendance.TABLE_NAME + namaSekolah.replaceAll("\\s", "");
        String query = "SELECT " + DBSchema.Attendance.DATE_COLUMN + ", COUNT(*)" +
                " FROM " + namaTable +
                " GROUP BY " + DBSchema.Attendance.DATE_COLUMN + ";";
        return mDb.rawQuery(query, null);
    }

    public String readAbsen(String namaSekolah, String tanggal, int idMurid){
        String namaTable = DBSchema.Attendance.TABLE_NAME + namaSekolah.replaceAll("\\s", "");
        String query = "SELECT " + DBSchema.Attendance.STUDENT_ID_COLUMN +
                " FROM " + namaTable +
                " WHERE " + DBSchema.Attendance.DATE_COLUMN + "='" + tanggal + "'" +
                " AND " + DBSchema.Attendance.STUDENT_ID_COLUMN + "=" + idMurid + ";";
        Cursor cursor = mDb.rawQuery(query, null);
        String absen = cursor.getCount() == 1 ? "Hadir" : "Tidak Hadir";
        cursor.close();
        return absen;
    }

    public int getJumlahAbsen(String namaSekolah, String bulan, int idMurid){
        String namaTable = DBSchema.Attendance.TABLE_NAME + namaSekolah.replaceAll("\\s", "");
        String query = "SELECT COUNT(*) " +
                " FROM " + namaTable +
                " WHERE  strftime('%m', " + DBSchema.Attendance.DATE_COLUMN + ")='" + bulan + "'" +
                " AND " + DBSchema.Attendance.STUDENT_ID_COLUMN + "=" + idMurid + ";";
        Cursor cursor = mDb.rawQuery(query, null);
        cursor.moveToNext();
        int jumlah = cursor.getInt(0);
        cursor.close();
        return jumlah;
    }

    public int insert(kost.golok.object.Student student, String namaSekolah){
        String namaTable = DBSchema.Student.TABLE_NAME + namaSekolah.replaceAll("\\s", "");
        return (int) mDb.insert(namaTable, null, contentValues(student));
    }

    public boolean delete(String table, String namaSekolah, int id){
        boolean tableSekolah = table.equals(DBSchema.School.TABLE_NAME);
        boolean tableMurid   = table.equals(DBSchema.Student.TABLE_NAME);
        String nama = namaSekolah.replaceAll("\\s", "");
        String namaTable = table + (tableSekolah ? "" : nama);
        String whereClause = "_id=?";
        String[] whereArgs = {String.valueOf(id)};
        int affected = mDb.delete(namaTable, whereClause, whereArgs);
        if(tableSekolah) {
            deleteRelateTable(nama);
        } else if(tableMurid){
            deleteAbsen(nama, id);
        }
        return affected != 0;
    }

    private void deleteAbsen(String namaSekolah, int id){
        String namaTable = DBSchema.Attendance.TABLE_NAME + namaSekolah;
        String whereClause = DBSchema.Attendance.STUDENT_ID_COLUMN + "=?";
        String[] whereArgs = { String.valueOf(id) };
        mDb.delete(namaTable, whereClause, whereArgs);
    }

    private void deleteRelateTable(String namaSekolah){
        String tableMurid = DBSchema.Student.TABLE_NAME + namaSekolah;
        String tableAbsen = DBSchema.Attendance.TABLE_NAME + namaSekolah;
        String sql = "DROP TABLE IF EXISTS ";
        mDb.execSQL(sql + tableMurid + ";");
        mDb.execSQL(sql + tableAbsen + ";");
    }

    private ContentValues contentValues(kost.golok.object.Student student){
        ContentValues values = new ContentValues();
        values.put(DBSchema.Student.CLASS_COLUMN, student.getKelas());
        values.put(NAME_COLUMN, student.getNamaMurid());
        return values;
    }

}

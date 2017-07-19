package kost.golok.absenpelatih;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Toast;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import kost.golok.adapter.MuridAdapter;
import kost.golok.controller.StudentController;
import kost.golok.database.DBManager;
import kost.golok.database.DBSchema;
import kost.golok.object.School;
import kost.golok.utility.manager.IntentManager;
import kost.golok.utility.manager.PreferenceManager;


public class MenuSekolahActivity extends AppCompatActivity {

    private int mLayout;
    private Menu mMenu;
    private DBManager mDb;
    private School mSchool;
    private ArrayList<kost.golok.object.Student> mSelectedStudent;
    private StudentController mStudentController;

    protected static Intent getIntent(Context context, Parcelable object, boolean finish) {
        Intent intent = new Intent(context, MenuSekolahActivity.class);
        intent.putExtra(IntentManager.SCHOOL_EXTRA, object);
        if (finish) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_sekolah);
        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.mMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_menu_sekolah, menu);
        if(mSchool.jumlahMurid() > 0){
            menu.findItem(R.id.menu_item_tambah_absen).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_tambah_murid:
                createDialogJumlahMurid();
                break;
            case R.id.menu_item_hapus_sekolah:
                createConfirmDialog();
                break;
            case R.id.menu_item_hapus_murid:
                setAdapter(R.layout.item_murid_delete);
                break;
            case R.id.menu_item_konfirmasi_hapus_murid:
                hapusMurid();
                break;
            case R.id.menu_item_tambah_absen:
                goToAbsen();
                break;
            case R.id.menu_item_export_xls:
                exportDB();
                break;
            case R.id.menu_item_hapus_absen:
                clear();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if(mLayout == 0) {
            super.onBackPressed();
        } else {
            setAdapter(R.layout.item_murid);
        }
    }

    private void init() {
        mDb = DBManager.getInstance(this);
        mSchool = getIntent().getParcelableExtra(IntentManager.SCHOOL_EXTRA);
        mSelectedStudent = new ArrayList<>();
        mStudentController = new StudentController(this, mSchool.getSchoolName());

        if (mSchool.jumlahMurid() > 0 && mSchool != null) {
            setAdapter(R.layout.item_murid);
            findViewById(R.id.view_activity_menu_sekolah_not_found).setVisibility(View.GONE);
        } else {
            Button btn = (Button) findViewById(R.id.btn_activity_menu_sekolah_tambah_murid);
            btn.setOnClickListener(v -> createDialogJumlahMurid());
        }
    }

    private void clear(){
        EditText etPass = new EditText(this);
        etPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        new AlertDialog.Builder(this)
                .setTitle("Masukkan password!")
                .setView(etPass)
                .setPositiveButton("Konfirmasi", (dialogInterface, i) -> {
                    String password = etPass.getText().toString();
                    if(password.equals(PreferenceManager.PASSWORD)){
                        mStudentController.clear("");
                        Toast.makeText(this, "Berhasil menghapus absen!", Toast.LENGTH_SHORT).show();
                        startActivity(MainActivity.getIntent(this, mSchool, true));
                    } else {
                        Toast.makeText(this, "Password yang dimasukkan salah!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Batal", (dialogInterface, i) ->
                        Toast.makeText(this, "Batal menghapus!", Toast.LENGTH_SHORT).show())
                .create()
                .show();
    }

    private void createConfirmDialog() {
        final String namaSekolah = mSchool.getSchoolName();
        new AlertDialog.Builder(this)
                    .setTitle("Iya")
                    .setMessage("Apakah anda yakin ingin menghapus " + namaSekolah + "?")
                    .setPositiveButton("Iya", (dialog, which) -> {
                        if (mDb.delete(DBSchema.School.TABLE_NAME, namaSekolah, mSchool.getId())) {
                            Toast.makeText(this, "Berhasil menghapus " + namaSekolah + "!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        } else {
                            Toast.makeText(this, "Ada kesalahan saat menghapus " + namaSekolah + "!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Tidak", (dialog, which) -> Toast.makeText(this, "Batal menghapus " + namaSekolah + "!", Toast.LENGTH_SHORT).show())
                    .create()
                    .show();
    }

    private void createDialogJumlahMurid() {
        @SuppressLint("InflateParams")
        final View view = getLayoutInflater().inflate(R.layout.dialog_number_picker, null);
        new AlertDialog.Builder(this)
                    .setView(view)
                    .setPositiveButton("Konfirmasi", (dialog, which) -> {
                        NumberPicker np = (NumberPicker) view.findViewById(R.id.np_dialog_jumlah_murid);

                        Intent intent = new Intent(getApplicationContext(), TambahMuridActivity.class);
                        intent.putExtra(IntentManager.EXTRA_NILAI_NUMBER_PICKER, np.getValue());
                        intent.putExtra(IntentManager.SCHOOL_EXTRA, mSchool);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    })
                    .setNegativeButton("Batal", (dialog, which) -> Toast.makeText(getApplicationContext(), "Penambahan murid dibatalkan!", Toast.LENGTH_SHORT).show())
                    .create()
                    .show();
        numberPickerSetup(view, 1, 10);
    }

    private void numberPickerSetup(View view, int min, int max) {
        NumberPicker np = (NumberPicker) view.findViewById(R.id.np_dialog_jumlah_murid);
        np.setMinValue(min);
        np.setMaxValue(max);
        np.setWrapSelectorWheel(false);
    }

    private void hapusMurid(){
        boolean error = false;
        for(int i = 0; i < mSelectedStudent.size(); i++){
            kost.golok.object.Student student = mSelectedStudent.get(i);
            if(!mDb.delete(DBSchema.Student.TABLE_NAME, mSchool.getSchoolName(), student.getId())){
                error = true;
            } else {
                mSchool.remove(student);
            }
        }
        if(!error){
            Toast.makeText(this, "Berhasil menghapus semua murid!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Terjadi beberapa kesalahan saat menghapus!", Toast.LENGTH_SHORT).show();
        }
        startActivity(getIntent(this, mSchool, true));
        menuDisabler(true);
    }

    private void setAdapter(int layoutId){
        ListView lv = (ListView) findViewById(R.id.lv_menu_sekolah_list_murid);
        lv.setAdapter(new MuridAdapter(this, mSchool.getStudents(), layoutId));
        lv.setVisibility(View.VISIBLE);
        if(layoutId == R.layout.item_murid){
            lv.setOnItemLongClickListener((parent, view, position, id) -> {
                setAdapter(R.layout.item_murid_delete);
                return false;
            });
            mLayout = 0;
        } else {
            lv.setOnItemClickListener((parent, view, position, id1) -> {
                kost.golok.object.Student student = (kost.golok.object.Student) parent.getItemAtPosition(position);
                CheckBox cb = (CheckBox) view.findViewById(R.id.cb_item_murid_delete_murid_selected);
                cb.setChecked(!cb.isChecked());
                if(mSelectedStudent.contains(student)){
                    mSelectedStudent.remove(student);
                } else{
                    mSelectedStudent.add(student);
                }
            });
            menuDisabler(false);
            mLayout = 1;
        }
        if(mSchool.jumlahMurid() == 0){
            findViewById(R.id.view_activity_menu_sekolah_not_found).setVisibility(View.VISIBLE);
            Button btn = (Button) findViewById(R.id.btn_activity_menu_sekolah_tambah_murid);
            btn.setOnClickListener(v -> createDialogJumlahMurid());
        }
    }

    private void menuDisabler(boolean enabled){
        mMenu.findItem(R.id.menu_item_hapus_murid).setEnabled(enabled);
        mMenu.findItem(R.id.menu_item_konfirmasi_hapus_murid).setVisible(!enabled);
    }

    private void goToAbsen(){
        startActivity(AttendanceCheckActivity.getIntent(this, mSchool));
    }

    private void exportDB(){
        String namaSekolah = mSchool.getSchoolName();
        Workbook workbook = new HSSFWorkbook();
        Sheet absenSheet = workbook.createSheet("Attendance");
        CellStyle hStyle = headerStyle(workbook);

        Cursor cursor = mDb.readTanggal(namaSekolah);
        String[] tanggal = getTanggal(cursor);
        String[] jumlahKehadiran = getJumlahKehadiran(cursor);
        cursor.close();

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd", new Locale("in", "id"));
        DateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", new Locale("in", "id"));

        int colCount = tanggal.length + 2;
        int rowCount = mSchool.jumlahMurid() + 5;
        int lastCol = colCount - 1;
        for(int i = 0; i < rowCount; i++){
            Row row = absenSheet.createRow(i);
            for (int j = 0; j < colCount; j++){
                Cell cell = row.createCell(j);

                if(i == 0){
                    cell.setCellValue("Attendance " + namaSekolah);
                    absenSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, lastCol));
                    cell.setCellStyle(titleStyle(workbook));
                } else if(i == 2){
                    if(j == 0){
                        setHeaderTable(cell, absenSheet, hStyle, "Nama", 0);
                    } else if(j == lastCol){
                        setHeaderTable(cell, absenSheet, hStyle, "Jml", lastCol);
                    } else {
                        cell.setCellValue("Tanggal");
                        absenSheet.addMergedRegion(new CellRangeAddress(2, 2, 1, lastCol-1));
                        CellUtil.setAlignment(cell, workbook, CellStyle.ALIGN_CENTER);
                    }
                } else if(i == 3){
                    if(j > 0 && j < lastCol){
                        String formatedTanggal;
                        try {
                            formatedTanggal = sdf.format(df.parse(tanggal[j-1]));
                            cell.setCellValue(formatedTanggal);
                            CellUtil.setAlignment(cell, workbook, CellStyle.ALIGN_CENTER);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                } else if(i == rowCount - 1){
                    if(j == 0){
                        cell.setCellValue("Jml");
                    } else if(j != lastCol){
                        cell.setCellValue(jumlahKehadiran[j-1]);
                    } else {
                        cell.setCellValue(mSchool.getTotalKehadiran());
                    }
                } else if(i != 1){
                    kost.golok.object.Student currentStudent = mSchool.getStudents().get(i-4);
                    if(j == 0){
                        cell.setCellValue(currentStudent.getNamaMurid());
                    } else if(j == lastCol){
                        cell.setCellValue(currentStudent.getJumlahKehadiran());
                    } else {
                        cell.setCellValue(mDb.readAbsen(namaSekolah, tanggal[j-1], currentStudent.getId()));
                    }

                }
            }
        }
        String fileName = "Absen_" + namaSekolah.replaceAll("\\s", "_") + ".xls";
        File file = new File(getExternalFilesDir(null), fileName);
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(file);
            workbook.write(os);
            Toast.makeText(this, "Berhasil membuat file!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.w("FileUtils", "Error writing " + file, e);
        } catch (Exception e) {
            Toast.makeText(this, "Kesalahan dalam membuat file!", Toast.LENGTH_SHORT).show();
        } finally {
            try {
                if (null != os)
                    os.close();
            } catch (Exception ex) {
                Log.w("Error", "Something is gone wrong! " + file, ex);
            }
        }
    }

    private void setHeaderTable(Cell cell, Sheet absenSheet, CellStyle cs, String value, int colToMerge){
        cell.setCellValue(value);
        absenSheet.addMergedRegion(new CellRangeAddress(2, 3, colToMerge, colToMerge));
        cell.setCellStyle(cs);
    }

    private CellStyle headerStyle(Workbook wb){
        CellStyle cs = wb.createCellStyle();
        cs.setAlignment(CellStyle.ALIGN_CENTER);
        cs.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        return cs;
    }

    private CellStyle titleStyle(Workbook wb){
        Font font = wb.createFont();
        font.setFontHeightInPoints((short) (12));
        CellStyle cs = wb.createCellStyle();
        cs.setAlignment(CellStyle.ALIGN_CENTER);
        cs.setFont(font);
        return cs;
    }

    private String[] getTanggal(Cursor cursor){
        ArrayList<String> arr = new ArrayList<>();
        while(cursor.moveToNext()){
            arr.add(cursor.getString(0));
        }
        return arr.toArray(new String[arr.size()]);
    }

    private String[] getJumlahKehadiran(Cursor cursor){
        ArrayList<String> arr = new ArrayList<>();
        if(cursor.moveToFirst()) {
            arr.add(String.valueOf(cursor.getInt(1)));
        }
        while(cursor.moveToNext()){
            arr.add(String.valueOf(cursor.getInt(1)));
        }
        return arr.toArray(new String[arr.size()]);
    }

}

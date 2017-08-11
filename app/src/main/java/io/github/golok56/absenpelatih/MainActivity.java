package io.github.golok56.absenpelatih;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
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
import java.text.ParseException;
import java.util.ArrayList;

import io.github.golok56.R;
import io.github.golok56.adapter.SchoolAdapter;
import io.github.golok56.database.interactor.AttendanceInteractor;
import io.github.golok56.database.interactor.SchoolBaseInteractor;
import io.github.golok56.object.School;
import io.github.golok56.object.Student;
import io.github.golok56.presenter.MainActivityPresenter;
import io.github.golok56.utility.Component;
import io.github.golok56.utility.Vocab;
import io.github.golok56.view.IMainActivityView;

public class MainActivity extends AppCompatActivity implements IMainActivityView {

    /** The presenter for this Activity */
    private MainActivityPresenter mPresenter;

    // Thing to do every work that related with database
    private SchoolBaseInteractor mSchoolController;

    // The school of list to display on screen
    private ArrayList<School> mSchoolList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        mPresenter = new MainActivityPresenter(this);

        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflating the menu
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_export_to_xls_with_school_data:
                exportDB();
                break;
            case R.id.menu_item_clear_all:
                showDialogClearAll();
                break;
            case R.id.menu_item_change_password:
                showDialogChangePassword();
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Updating school list, after user add some students and get back here using navigation bar
        refreshView();
    }

    private void init() {
        // Create a dialog when the floating button is clicked
        FloatingActionButton fabCreate = (FloatingActionButton) findViewById(R.id.fab_main_menu_add_school);
        fabCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                @SuppressLint("InflateParams")
                final View view = MainActivity.this.getLayoutInflater().inflate(R.layout.dialog_add_school, null);
                new AlertDialog.Builder(MainActivity.this).setView(view)
                        .setPositiveButton("Simpan", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String schoolName = Component.getText(view, R.id.et_dialog_add_school_schools_name);
                                if (schoolName.isEmpty())
                                    Toast.makeText(MainActivity.this, "Nama sekolah tidak boleh kosong!", Toast.LENGTH_SHORT).show();
                                else {
                                    // Inserting the new school with given name to database
                                    if (mSchoolController.insert(new School(schoolName))) {
                                        MainActivity.this.refreshView();
                                        Toast.makeText(MainActivity.this, "Berhasil menambahkan " + schoolName, Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(MainActivity.this, "Terjadi Kesalahan!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        })
                        .setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(MainActivity.this, "Penambahan sekolah dibatalkan!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .create()
                        .show();
            }
        });
        refreshView();
    }
    // Updating the school list and displaying the new updated list to screen
    private void refreshView(){
        // Retrieving the list from database
        mSchoolList = mSchoolController.getList("");
        if (mSchoolList != null) {
            // Setting up the listview to display the school list
            ListView listView = (ListView) findViewById(R.id.lv_main_menu_school_list);
            listView.setAdapter(new SchoolAdapter(this, mSchoolList));
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    School current = (School) parent.getItemAtPosition(position);
                    startActivity(SchoolMenuActivity.getIntent(MainActivity.this, current, false));
                }
            });
            listView.setVisibility(View.VISIBLE);
            TextView tv = (TextView) findViewById(R.id.tv_main_menu_school_not_found);
            tv.setVisibility(View.GONE);
        }
    }

    private void showDialogClearAll() {
        // Showing the comfirmation dialog and ask for password
        final EditText etPass = new EditText(this);
        etPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        new AlertDialog.Builder(this)
                .setTitle("Masukkan password!")
                .setView(etPass)
                .setPositiveButton("Konfirmasi", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {
                        String passToCheck = etPass.getText().toString();
                        SharedPreferences pref = MainActivity.this.getSharedPreferences(Vocab.PREF_NAME, Context.MODE_PRIVATE);
                        String password = pref.getString(Vocab.PASSWORD, Vocab.DEFAULT_PASSWORD);
                        if (passToCheck.equals(password)) {
                            // Clear the attendance history from all school
                            for (int i = 0, size = mSchoolList.size(); i < size; i++) {
                                mSchoolController.clear(mSchoolList.get(i).getSchoolName().replaceAll("\\s", ""));
                            }
                            Toast.makeText(MainActivity.this, "Berhasil menghapus semua absen!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Password yang dimasukkan salah!", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(MainActivity.this, "Batal menghapus!", Toast.LENGTH_SHORT).show();
                    }
                })
                .create()
                .show();
    }

    private void showDialogChangePassword(){
        @SuppressLint("InflateParams")
        final View view = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton("Simpan", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String oldPass = Component.getText(view, R.id.et_dialog_change_password_old_password);
                        String newPass = Component.getText(view, R.id.et_dialog_change_password_new_password);
                        if (oldPass.isEmpty() || newPass.isEmpty())
                            Toast.makeText(MainActivity.this, "Tidak boleh ada form yang kosong!", Toast.LENGTH_SHORT).show();
                        else {
                            SharedPreferences pref = MainActivity.this.getSharedPreferences(Vocab.PREF_NAME, Context.MODE_PRIVATE);
                            String savedPassword = pref.getString(Vocab.PASSWORD, Vocab.DEFAULT_PASSWORD);
                            // Inserting the new school with given name to database
                            if (oldPass.equals(savedPassword)) {
                                pref.edit().putString(Vocab.PASSWORD, newPass).apply();
                                Toast.makeText(MainActivity.this, "Berhasil mengganti password!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "Password lama yang dimasukkan salah!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                })
                .setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(MainActivity.this, "Batal mengganti password!", Toast.LENGTH_SHORT).show();
                    }
                })
                .create()
                .show();
    }

    private void exportDB() {
        // Try to get all available month from database for the report in xls file
        String[] monthInText = null;
        try {
            monthInText = new AttendanceInteractor(this, mSchoolList.get(0).getSchoolName()).getAllAvailableMonth();
        } catch (ParseException ex){
            ex.printStackTrace();
        }
        // Creating the content of the xls file
        Workbook workbook = new HSSFWorkbook();
        Sheet attendanceSheet = workbook.createSheet("Absen");
        if(monthInText != null) {
            // Set the max row and max col of the xls file
            int colCount = monthInText.length + 2;
            int rowCount = mSchoolList.size() + 4;
            int lastCol = colCount - 1;
            // Iterating the row of the xls file
            for (int i = 0; i < rowCount; i++) {
                Row row = attendanceSheet.createRow(i);
                // Iterating the column of the xls file
                for (int j = 0; j < colCount; j++) {
                    Cell cell = row.createCell(j);
                    if (i == 0) {
                        // Create the sheet's title
                        cell.setCellValue("Laporan Absen");
                        attendanceSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, lastCol));
                        Font font = workbook.createFont();
                        font.setFontHeightInPoints((short) (12));
                        CellStyle cs = workbook.createCellStyle();
                        cs.setAlignment(CellStyle.ALIGN_CENTER);
                        cs.setFont(font);
                        cell.setCellStyle(cs);
                    } else if (i == 2) {
                        // Creating the headers for each column
                        if (j == 0) {
                            setHeaderTable(cell, attendanceSheet, "Nama", 0);
                        } else if (j == lastCol) {
                            setHeaderTable(cell, attendanceSheet, "Jml", lastCol);
                        } else {
                            cell.setCellValue("Bulan");
                            attendanceSheet.addMergedRegion(new CellRangeAddress(2, 2, 1, lastCol - 1));
                            CellUtil.setAlignment(cell, workbook, CellStyle.ALIGN_CENTER);
                        }
                    } else if (i == 3) {
                        // Creating the months for each column that bulan span
                        if (j > 0 && j < lastCol) {
                            cell.setCellValue(monthInText[j - 1]);
                            CellUtil.setAlignment(cell, workbook, CellStyle.ALIGN_CENTER);
                        }
                    } else if (i != 1) {
                        School currentSchool = mSchoolList.get(i - 4);
                        // Create the school name for the 1st column, attendance total for the last column
                        // and the rest is attendance total from every school each month
                        if (j == 0) {
                            cell.setCellValue(currentSchool.getSchoolName());
                        } else if (j == lastCol) {
                            cell.setCellValue(currentSchool.getTotalKehadiran());
                        } else {
                            ArrayList<Student> students = currentSchool.getStudents();
                            int attendanceTotal = 0;
                            for(int k = 0, size = students.size(); k < size; k++){
                                attendanceTotal += students.get(k).getTotalAttendance();
                            }
                            cell.setCellValue(String.valueOf(attendanceTotal));
                        }

                    }
                }
            }
        }
        // Creating the xls file
        String fileName = "Absen_Keseluruhan.xls";
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

    private void setHeaderTable(Cell cell, Sheet absenSheet, String value, int colToMerge) {
        CellStyle cs = cell.getCellStyle();
        cs.setAlignment(CellStyle.ALIGN_CENTER);
        cs.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        absenSheet.addMergedRegion(new CellRangeAddress(2, 3, colToMerge, colToMerge));
        cell.setCellStyle(cs);
        cell.setCellValue(value);
    }

    static Intent getIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

}

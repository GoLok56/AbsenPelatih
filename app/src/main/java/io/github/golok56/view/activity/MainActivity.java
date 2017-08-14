package io.github.golok56.view.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
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
import io.github.golok56.database.interactor.SchoolInteractor;
import io.github.golok56.object.School;
import io.github.golok56.object.Student;
import io.github.golok56.presenter.MainActivityPresenter;
import io.github.golok56.utility.PreferenceManager;
import io.github.golok56.view.IMainActivityView;

public class MainActivity extends AppCompatActivity implements IMainActivityView {

    public static final String SCHOOL_EXTRA = "SCHOOL_EXTRA";

    /**
     * The presenter for this Activity
     */
    private MainActivityPresenter mPresenter;

    /**
     * The school of list to display on screen
     */
    private ArrayList<School> mSchoolList;

    private ListView mLvSchools;

    private EditText mEtSchoolName;
    private EditText mEtOldPassword;
    private EditText mEtNewPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        mPresenter = new MainActivityPresenter(
                this,
                new SchoolInteractor(this),
                PreferenceManager.getInstance(this)
        );
        mLvSchools = (ListView) findViewById(R.id.lv_main_menu_school_list);

        findViewById(R.id.fab_main_menu_add_school).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.onAddSchoolClicked();
            }
        });
        mPresenter.getItems();
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
                mPresenter.onItemClearClicked();
                break;
            case R.id.menu_item_change_password:
                mPresenter.onChangePassClicked();
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.getItems();

    }

    private void exportDB() {
        // Try to get all available month from database for the report in xls file
        String[] monthInText = null;
        try {
            monthInText = new AttendanceInteractor(this, mSchoolList.get(0).getSchoolName()).getAllAvailableMonth();
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        // Creating the content of the xls file
        Workbook workbook = new HSSFWorkbook();
        Sheet attendanceSheet = workbook.createSheet("Absen");
        if (monthInText != null) {
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
                            for (int k = 0, size = students.size(); k < size; k++) {
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

    @Override
    public void setSchoolList(final ArrayList<School> schoolList) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSchoolList = schoolList;
                if (mSchoolList != null) {
                    mLvSchools.setAdapter(new SchoolAdapter(MainActivity.this, mSchoolList));
                    mLvSchools.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            mPresenter.onItemClicked((School) parent.getItemAtPosition(position));
                        }
                    });
                    mLvSchools.setVisibility(View.VISIBLE);
                    findViewById(R.id.tv_main_menu_school_not_found).setVisibility(View.GONE);
                }

            }
        });
    }

    @Override
    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void showAddSchoolDialog() {
        @SuppressLint("InflateParams")
        View view = getLayoutInflater().inflate(R.layout.dialog_add_school, null);
        mEtSchoolName = (EditText) view.findViewById(R.id.et_dialog_add_school_school_name);
        new AlertDialog.Builder(this).setView(view)
                .setPositiveButton("Simpan", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mPresenter.onSaveSchoolClicked(mEtSchoolName.getText().toString());
                    }
                })
                .setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mPresenter.onCancelSaveClicked();
                    }
                })
                .create()
                .show();
    }

    @Override
    public void showClearDialog() {
        final EditText etPass = new EditText(this);
        etPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        new AlertDialog.Builder(this)
                .setTitle("Masukkan password!")
                .setView(etPass)
                .setPositiveButton("Konfirmasi", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {
                        mPresenter.onClearConfirmClicked(mSchoolList, etPass.getText().toString());
                    }
                })
                .setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mPresenter.onClearCancelClicked();
                    }
                })
                .create()
                .show();
    }

    @Override
    public void showChangePasswordDialog() {
        @SuppressLint("InflateParams")
        View view = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        mEtOldPassword = (EditText) view.findViewById(R.id.et_dialog_change_password_old_password);
        mEtNewPassword = (EditText) view.findViewById(R.id.et_dialog_change_password_new_password);
        new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton("Simpan", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mPresenter.onChangePassComfirmed(
                                mEtOldPassword.getText().toString(),
                                mEtNewPassword.getText().toString()
                        );
                    }
                })
                .setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mPresenter.onChangePassCanceled();
                    }
                })
                .create()
                .show();
    }

    @Override
    public void showSchoolNameError(String msg) {
        mEtSchoolName.setError(msg);
    }

    @Override
    public void showSchoolMenu(School school) {
        startActivity(SchoolMenuActivity.getIntent(this, school, false));
    }

}

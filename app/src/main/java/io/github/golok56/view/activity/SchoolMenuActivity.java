package io.github.golok56.view.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.github.golok56.R;
import io.github.golok56.adapter.StudentAdapter;
import io.github.golok56.database.interactor.AttendanceInteractor;
import io.github.golok56.database.interactor.SchoolInteractor;
import io.github.golok56.database.interactor.StudentInteractor;
import io.github.golok56.object.School;
import io.github.golok56.object.Student;
import io.github.golok56.presenter.SchoolMenuPresenter;
import io.github.golok56.utility.PreferenceManager;
import io.github.golok56.view.ISchoolMenuView;

public class SchoolMenuActivity extends AppCompatActivity implements ISchoolMenuView {

    public static final String NUMBER_PICKER_VALUE_EXTRA = "NUMBER_PICKER_VALUE_EXTRA";

    public enum LayoutActive {STUDENT_LIST, STUDENT_SELECTOR}

    /**
     * The id of layout this activity currently using
     */
    private LayoutActive mLayout;

    private SchoolMenuPresenter mPresenter;

    /**
     * Menu of this activity for disabling/enabling some of the menu item
     */
    private Menu mMenu;

    /**
     * The school that currently active and its' name
     */
    private School mSchool;
    private String mSchoolName;

    private ListView mLvStudentList;
    private View mTempView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_sekolah);

        mSchool = getIntent().getParcelableExtra(MainActivity.SCHOOL_EXTRA);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(mSchool.getSchoolName());
        }

        mLayout = LayoutActive.STUDENT_SELECTOR;

        mSchoolName = mSchool.getSchoolName();

        mPresenter = new SchoolMenuPresenter(
                this,
                new StudentInteractor(this, mSchoolName),
                new SchoolInteractor(this),
                new AttendanceInteractor(this, mSchoolName),
                PreferenceManager.getInstance(this)
        );

        StudentInteractor.sSelectedStudents.clear();

        mLvStudentList = (ListView) findViewById(R.id.lv_school_menu_student_list);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        getMenuInflater().inflate(R.menu.activity_school_menu, menu);

        mPresenter.refresh(mLayout);

        // Enabling/disabling some  views based on active layout
        if (mSchool.getStudents().size() > 0) {
            mMenu.findItem(R.id.menu_item_do_attendance_check).setVisible(true);
            findViewById(R.id.view_activity_school_menu_not_found).setVisibility(View.GONE);
            findViewById(R.id.lv_school_menu_student_list).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.view_activity_school_menu_not_found).setVisibility(View.VISIBLE);
            findViewById(R.id.lv_school_menu_student_list).setVisibility(View.GONE);
            findViewById(R.id.btn_activity_school_menu_add_students)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mPresenter.onAddStudentClicked();
                        }
                    });
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_add_students:
                mPresenter.onAddStudentClicked();
                break;
            case R.id.menu_item_delete_school:
                mPresenter.onDeleteSchoolClicked();
                break;
            case R.id.menu_item_delete_students:
                mPresenter.refresh(mLayout);
                break;
            case R.id.menu_item_delete_students_confirmation:
                mPresenter.onDeleteStudentClicked(mSchool);
                break;
            case R.id.menu_item_do_attendance_check:
                mPresenter.onDoCheckClicked();
                break;
            case R.id.menu_item_export_xls:
                exportDB();
                break;
            case R.id.menu_item_clear_attendance_history:
                mPresenter.onClearClicked();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        switch (mLayout) {
            case STUDENT_LIST:
                super.onBackPressed();
                break;
            case STUDENT_SELECTOR:
                mPresenter.refresh(mLayout);
                break;
        }
    }

    @Override
    public void showClearConfirmationDialog() {
        final EditText etPass = new EditText(this);
        etPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        new AlertDialog.Builder(this)
                .setTitle("Masukkan password!")
                .setView(etPass)
                .setPositiveButton("Konfirmasi", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mPresenter.onClearConfirmClicked(etPass.getText().toString());
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
    public void showDeleteSchoolConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Iya")
                .setMessage("Apakah anda yakin ingin menghapus " + mSchoolName + "?")
                .setPositiveButton("Iya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPresenter.onSchoolDeleteConfirmClicked(mSchool);
                    }
                })
                .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPresenter.onSchoolDeleteCancelClicked(mSchoolName);
                    }
                })
                .create()
                .show();
    }

    @Override
    public void showAttendanceCheckActivity() {
        startActivity(AttendanceCheckActivity.getIntent(this, mSchool));
    }

    @Override
    public void restartView() {
        startActivity(getIntent(this, mSchool, true));
    }

    @Override
    public void showMainActivity() {
        startActivity(MainActivity.getIntent(this));
    }

    @Override
    public void changeAdapter(LayoutActive layoutActive) {
        switch (layoutActive) {
            case STUDENT_LIST:
                mLvStudentList.setAdapter(
                        new StudentAdapter(this, mSchool.getStudents(), R.layout.item_delete_student));
                mLvStudentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id1) {
                        mTempView = view;
                        mPresenter.onItemDeleteStudentClicked(
                                (Student) parent.getItemAtPosition(position));
                    }
                });
                mLayout = LayoutActive.STUDENT_SELECTOR;
                mMenu.findItem(R.id.menu_item_delete_students_confirmation).setVisible(true);
                mMenu.findItem(R.id.menu_item_delete_students).setEnabled(false);
                break;
            case STUDENT_SELECTOR:
                mLvStudentList.setAdapter(
                        new StudentAdapter(this, mSchool.getStudents(), R.layout.item_student));
                mLvStudentList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view,
                                                   int position, long id) {
                        mPresenter.onItemListLongClicked(mLayout);
                        return false;
                    }
                });
                mLayout = LayoutActive.STUDENT_LIST;
                mMenu.findItem(R.id.menu_item_delete_students).setEnabled(true);
                mMenu.findItem(R.id.menu_item_delete_students_confirmation).setVisible(false);
                break;
        }
    }

    @Override
    public void toggleStudentChecked(Student student) {
        CheckBox cb = (CheckBox) mTempView.findViewById(R.id.cb_item_murid_delete_murid_selected);
        cb.setChecked(!cb.isChecked());
        if (StudentInteractor.sSelectedStudents.contains(student)) {
            StudentInteractor.sSelectedStudents.remove(student);
        } else {
            StudentInteractor.sSelectedStudents.add(student);
        }
    }

    private void exportDB() {
        // Try to get all available date also the total attendance in each
        HashMap<String, Integer> dateMap = null;
        try {
            dateMap = new AttendanceInteractor(this, mSchoolName).getDateAndTotalAttendance();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        List<String> date;
        if (dateMap != null) {
            date = new ArrayList<>(dateMap.keySet());
        } else {
            Toast.makeText(this, "Ada kesalahan!", Toast.LENGTH_SHORT).show();
            return;
        }
        List<Integer> totalAttendance = new ArrayList<>(dateMap.values());
        // Creating the content of the xls file
        Workbook workbook = new HSSFWorkbook();
        Sheet attendanceSheet = workbook.createSheet("Absen");
        CellStyle hStyle = headerStyle(workbook);
        // Set the max row and max col of the xls file
        int colCount = date.size() + 2;
        int rowCount = mSchool.totalStudents() + 5;
        int lastCol = colCount - 1;
        // Iterating the row of the xls file
        for (int i = 0; i < rowCount; i++) {
            Row row = attendanceSheet.createRow(i);
            // Iterating the column of the xls file
            for (int j = 0; j < colCount; j++) {
                Cell cell = row.createCell(j);
                if (i == 0) {
                    // Create the sheet's title
                    cell.setCellValue("Absen " + mSchoolName);
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
                        setHeaderTable(cell, attendanceSheet, hStyle, "Nama", 0);
                    } else if (j == lastCol) {
                        setHeaderTable(cell, attendanceSheet, hStyle, "Jml", lastCol);
                    } else {
                        cell.setCellValue("Tanggal");
                        attendanceSheet.addMergedRegion(new CellRangeAddress(2, 2, 1, lastCol - 1));
                        CellUtil.setAlignment(cell, workbook, CellStyle.ALIGN_CENTER);
                    }
                } else if (i == 3) {
                    // Creating the dates for each column that date span
                    if (j > 0 && j < lastCol) {
                        cell.setCellValue(date.get(j - 1));
                    }
                } else if (i == rowCount - 1) {
                    if (j == 0) {
                        cell.setCellValue("Jml");
                    } else if (j != lastCol) {
                        cell.setCellValue(totalAttendance.get(j - 1));
                    } else {
                        cell.setCellValue(mSchool.getTotalKehadiran());
                    }
                } else if (i != 1) {
                    // Create the student name for the 1st column, attendance total for the last column
                    // and the rest is attendance total from every school each month
                    Student currentStudent = mSchool.getStudents().get(i - 4);
                    if (j == 0) {
                        cell.setCellValue(currentStudent.getName());
                    } else if (j == lastCol) {
                        cell.setCellValue(currentStudent.getTotalAttendance());
                    } else {
                        cell.setCellValue(new AttendanceInteractor(this, mSchoolName)
                                .getAttendanceInfo(date.get(j - 1), currentStudent.getId()));
                    }

                }
            }
        }
        String fileName = "Absen_" + mSchoolName.replaceAll("\\s", "_") + ".xls";
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

    private void setHeaderTable(Cell cell, Sheet absenSheet, CellStyle cs, String value, int colToMerge) {
        cell.setCellValue(value);
        absenSheet.addMergedRegion(new CellRangeAddress(2, 3, colToMerge, colToMerge));
        cell.setCellStyle(cs);
    }

    private CellStyle headerStyle(Workbook wb) {
        CellStyle cs = wb.createCellStyle();
        cs.setAlignment(CellStyle.ALIGN_CENTER);
        cs.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        return cs;
    }

    @Override
    public void showTotalStudentPickerDialog() {
        @SuppressLint("InflateParams")
        View view = getLayoutInflater().inflate(R.layout.dialog_number_picker, null);
        final NumberPicker np = (NumberPicker) view.findViewById(R.id.np_dialog_student_total);
        new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton("Konfirmasi", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPresenter.onTotalConfirmClicked(np.getValue());
                    }
                })
                .setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPresenter.onTotalCancelClicked();
                    }
                })
                .create()
                .show();
        np.setMinValue(1);
        np.setMaxValue(10);
        np.setWrapSelectorWheel(false);
    }

    @Override
    public void showAddStudentActivity(int studentTotal) {
        startActivity(AddStudentActivity.getIntent(this, mSchool, studentTotal));
    }

    @Override
    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SchoolMenuActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    static Intent getIntent(Context context, Parcelable school, boolean finish) {
        Intent intent = new Intent(context, SchoolMenuActivity.class);
        intent.putExtra(MainActivity.SCHOOL_EXTRA, school);
        if (finish) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        return intent;
    }

}

package io.github.golok56.absenpelatih;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.github.golok56.R;
import io.github.golok56.adapter.StudentAdapter;
import io.github.golok56.controller.AttendanceController;
import io.github.golok56.controller.SchoolController;
import io.github.golok56.controller.StudentController;
import io.github.golok56.object.School;
import io.github.golok56.object.Student;
import io.github.golok56.utility.Vocab;

public class SchoolMenuActivity extends AppCompatActivity {

    private enum LayoutActive { STUDENT_LIST, STUDENT_SELECTOR }

    // The id of layout this activity currently using
    private LayoutActive mLayout;

    // Menu of this activity for disabling/enabling some of the menu item
    private Menu mMenu;

    // The school that currently active and its' name
    private School mSchool;
    private String mSchoolName;

    // Attendance Controller to do database works
    private AttendanceController mAttendanceController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_sekolah);

        mSchool = getIntent().getParcelableExtra(Vocab.SCHOOL_EXTRA);
        ActionBar ab = getSupportActionBar();
        if(ab != null) {
            ab.setTitle(mSchool.getSchoolName());
        }
        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Take the reference of the menu to global variable
        this.mMenu = menu;
        // Inflating the menu
        getMenuInflater().inflate(R.menu.activity_school_menu, menu);
        setAdapter(LayoutActive.STUDENT_LIST);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_add_students:
                createDialogJumlahMurid();
                break;
            case R.id.menu_item_delete_school:
                createDelSchConfirmDialog();
                break;
            case R.id.menu_item_delete_students:
                setAdapter(LayoutActive.STUDENT_SELECTOR);
                break;
            case R.id.menu_item_delete_students_confirmation:
                deleteStudent();
                break;
            case R.id.menu_item_do_attendance_check:
                startActivity(AttendanceCheckActivity.getIntent(this, mSchool));
                break;
            case R.id.menu_item_export_xls:
                exportDB();
                break;
            case R.id.menu_item_clear_attendance_history:
                showDialogClear();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        switch (mLayout){
            // Get back to the top activity on stack
            case STUDENT_LIST:
                super.onBackPressed();
                break;
            // Change the layout back to STUDENT_LIST
            case STUDENT_SELECTOR:
                setAdapter(LayoutActive.STUDENT_SELECTOR);
                break;
        }
    }

    private void onLayoutChange(LayoutActive layout){
        // Enabling/disabling some menus based on active layout
        switch (layout){
            case STUDENT_LIST:
                mMenu.findItem(R.id.menu_item_delete_students).setEnabled(true);
                mMenu.findItem(R.id.menu_item_delete_students_confirmation).setVisible(false);
                break;
            case STUDENT_SELECTOR:
                mMenu.findItem(R.id.menu_item_delete_students_confirmation).setVisible(true);
                mMenu.findItem(R.id.menu_item_delete_students).setEnabled(false);
                break;
        }
    }

    private void init() {
        // Initializing global variables
        mSchoolName = mSchool.getSchoolName();
        mAttendanceController = new AttendanceController(this, mSchoolName);
        StudentController.sSelectedStudents.clear();
    }

    private void showDialogClear(){
        // Showing the comfirmation dialog and ask for password
        final EditText etPass = new EditText(this);
        etPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        new AlertDialog.Builder(this)
                .setTitle("Masukkan password!")
                .setView(etPass)
                .setPositiveButton("Konfirmasi", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Comparing the password
                        String passToCheck = etPass.getText().toString();
                        SharedPreferences pref = SchoolMenuActivity.this.getSharedPreferences(Vocab.PREF_NAME, Context.MODE_PRIVATE);
                        String password = pref.getString(Vocab.PASSWORD, Vocab.DEFAULT_PASSWORD);
                        if (passToCheck.equals(password)) {
                            // Clearing the attendance history
                            mAttendanceController.clear("");
                            Toast.makeText(SchoolMenuActivity.this, "Berhasil menghapus absen!", Toast.LENGTH_SHORT).show();
                            startActivity(MainActivity.getIntent(SchoolMenuActivity.this));
                        } else {
                            Toast.makeText(SchoolMenuActivity.this, "Password yang dimasukkan salah!", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(SchoolMenuActivity.this, "Batal menghapus!", Toast.LENGTH_SHORT).show();
                    }
                })
                .create()
                .show();
    }

    private void createDelSchConfirmDialog() {
        // Showing the comfirmation dialog and ask for password
        new AlertDialog.Builder(this)
                    .setTitle("Iya")
                    .setMessage("Apakah anda yakin ingin menghapus " + mSchoolName + "?")
                    .setPositiveButton("Iya", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Delete active school from database
                            if (new SchoolController(SchoolMenuActivity.this).delete(mSchool)) {
                                Toast.makeText(SchoolMenuActivity.this, "Berhasil menghapus " + mSchoolName + "!", Toast.LENGTH_SHORT).show();
                                SchoolMenuActivity.this.startActivity(MainActivity.getIntent(SchoolMenuActivity.this));
                            } else {
                                Toast.makeText(SchoolMenuActivity.this, "Ada kesalahan saat menghapus " + mSchoolName + "!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(SchoolMenuActivity.this, "Batal menghapus " + mSchoolName + "!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .create()
                    .show();
    }

    private void createDialogJumlahMurid() {
        @SuppressLint("InflateParams")
        // Showing the comfirmation dialog and ask for password
        final View view = getLayoutInflater().inflate(R.layout.dialog_number_picker, null);
        final NumberPicker np = (NumberPicker) view.findViewById(R.id.np_dialog_jumlah_murid);
        new AlertDialog.Builder(this)
                    .setView(view)
                    .setPositiveButton("Konfirmasi", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Start the activity to add more students
                            startActivity(AddStudentActivity.getIntent(SchoolMenuActivity.this, mSchool, np.getValue()));
                        }
                    })
                    .setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(SchoolMenuActivity.this, "Penambahan murid dibatalkan!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .create()
                    .show();
        np.setMinValue(1);
        np.setMaxValue(10);
        np.setWrapSelectorWheel(false);
    }

    private void deleteStudent(){
        // Trying to delete all selected students
        boolean error = false;
        StudentController studentController = new StudentController(this, mSchoolName);
        for(int i = 0, size = StudentController.sSelectedStudents.size(); i < size; i++){
            Student student = StudentController.sSelectedStudents.get(i);
            if(studentController.delete(student)){
                // Remove the student from the active school
                mSchool.remove(student);
            } else {
                error = true;
            }
        }
        if(!error){
            Toast.makeText(this, "Berhasil menghapus semua murid!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Terjadi beberapa kesalahan saat menghapus!", Toast.LENGTH_SHORT).show();
        }
        // Clear the selected students since it is no longer needed
        StudentController.sSelectedStudents.clear();
        // Refreshing the view
        startActivity(getIntent(this, mSchool, true));
    }

    // Set the adapter based on given layout
    private void setAdapter(LayoutActive layout){
        ListView lv = (ListView) findViewById(R.id.lv_menu_sekolah_list_murid);
        switch (layout){
            case STUDENT_LIST:
                lv.setAdapter(new StudentAdapter(this, mSchool.getStudents(), R.layout.item_student));
                lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        setAdapter(LayoutActive.STUDENT_SELECTOR);
                        return false;
                    }
                });
                break;
            case STUDENT_SELECTOR:
                lv.setAdapter(new StudentAdapter(this, mSchool.getStudents(), R.layout.item_delete_student));
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id1) {
                        Student student = (Student) parent.getItemAtPosition(position);
                        CheckBox cb = (CheckBox) view.findViewById(R.id.cb_item_murid_delete_murid_selected);
                        cb.setChecked(!cb.isChecked());
                        if (StudentController.sSelectedStudents.contains(student)) {
                            StudentController.sSelectedStudents.remove(student);
                        } else {
                            StudentController.sSelectedStudents.add(student);
                        }
                    }
                });
        }
        // Passing the reference of the layout to the global variabl
        mLayout = layout;
        // Call the event on layout chaneg and checking for total students to configure the view
        onLayoutChange(layout);
        checkTotalStudents(mSchool.totalStudents());
    }

    private void checkTotalStudents(int totalStudents){
        // Enabling/disabling some  views based on active layout
        if(totalStudents > 0){
            mMenu.findItem(R.id.menu_item_do_attendance_check).setVisible(true);
            findViewById(R.id.view_activity_menu_sekolah_not_found).setVisibility(View.GONE);
            findViewById(R.id.lv_menu_sekolah_list_murid).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.view_activity_menu_sekolah_not_found).setVisibility(View.VISIBLE);
            findViewById(R.id.lv_menu_sekolah_list_murid).setVisibility(View.GONE);
            Button btn = (Button) findViewById(R.id.btn_activity_school_menu_add_students);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    createDialogJumlahMurid();
                }
            });
        }
    }
    private void exportDB(){
        // Try to get all available date also the total attendance in each
        HashMap<String, Integer> dateMap = null;
        try {
            dateMap = mAttendanceController.getDateAndTotalAttendance();
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
                        cell.setCellValue(date.get(j-1));
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
                        cell.setCellValue(mAttendanceController.getAttendanceInfo(date.get(j-1), currentStudent.getId()));
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

    static Intent getIntent(Context context, Parcelable school, boolean finish) {
        Intent intent = new Intent(context, SchoolMenuActivity.class);
        intent.putExtra(Vocab.SCHOOL_EXTRA, school);
        if(finish) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        return intent;
    }

}

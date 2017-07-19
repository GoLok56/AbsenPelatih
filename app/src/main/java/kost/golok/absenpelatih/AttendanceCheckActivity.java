package kost.golok.absenpelatih;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import kost.golok.adapter.AttendanceCheckAdapter;
import kost.golok.controller.AttendanceController;
import kost.golok.controller.StudentController;
import kost.golok.object.School;
import kost.golok.object.Student;
import kost.golok.utility.manager.IntentManager;

public class AttendanceCheckActivity extends AppCompatActivity {

    // The school that currently active/user chose
    private School mSchool;

    // Thing to do every work that related with database
    private StudentController mStudentController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_check);
        init();
    }

    private void init(){
        mSchool = getIntent().getParcelableExtra(IntentManager.SCHOOL_EXTRA);
        mStudentController = new StudentController(AttendanceCheckActivity.this, mSchool.getSchoolName());

        // Clearing the attended student list in case it is not empty and contain students
        // from another school
        if(!AttendanceCheckAdapter.sAttendedStudent.isEmpty()){
            AttendanceCheckAdapter.sAttendedStudent.clear();
        }

        initView();
    }

    private void initView(){
        // Inflate the layout with students from mSchool
        ListView lv = (ListView) findViewById(R.id.lv_student_attendance_check);
        lv.setAdapter(new AttendanceCheckAdapter(this, mSchool.getStudents()));

        // Setting the button's onclicklistener and edittext's ontextchangelistener
        setButton();
        setEditText(lv);
    }

    private void setButton(){
        Button btn = (Button) findViewById(R.id.btn_absen_attendance_submit);
        btn.setOnClickListener(view -> {
            // Try to insert all the student's id in attended student list to database
            AttendanceController attendanceController = new AttendanceController(this, mSchool.getSchoolName());
            int error = 0;
            for(int i = 0, size = AttendanceCheckAdapter.sAttendedStudent.size(); i < size; ++i){
                if(attendanceController.insert(AttendanceCheckAdapter.sAttendedStudent.get(i).getId())){
                    AttendanceCheckAdapter.sAttendedStudent.get(i).addJumlahKehadiran();
                } else {
                    error++;
                }
            }

            if(error > 0){
                Toast.makeText(this, "Terjadi kesalahan! " + error + " murid gagal dimasukkan kedalam database.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Behasil melakukan absen!", Toast.LENGTH_SHORT).show();
            }

            // Clear the attended student list as it not needed anymore
            AttendanceCheckAdapter.sAttendedStudent.clear();
            startActivity(MenuSekolahActivity.getIntent(AttendanceCheckActivity.this, mSchool, true));
        });
    }

    private void setEditText(ListView lv){
        EditText etSearch = (EditText) findViewById(R.id.et_absen_cari_murid);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {}

            @Override
            // Displaying only student with name match with the etSearch value
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Try to search the database for student with the given etSearch value
                ArrayList<Student> students = mStudentController.getList(etSearch.getText().toString());
                // Updating the screen with new list
                lv.setAdapter(new AttendanceCheckAdapter(AttendanceCheckActivity.this, students));
            }
        });
    }

    protected static Intent getIntent(Context context, Parcelable object) {
        Intent intent = new Intent(context, AttendanceCheckActivity.class);
        intent.putExtra(IntentManager.SCHOOL_EXTRA, object);
        return intent;
    }

}

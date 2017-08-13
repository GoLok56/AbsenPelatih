package io.github.golok56.absenpelatih;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import io.github.golok56.R;
import io.github.golok56.adapter.AttendanceAdapter;
import io.github.golok56.database.interactor.AttendanceInteractor;
import io.github.golok56.database.interactor.StudentInteractor;
import io.github.golok56.object.School;
import io.github.golok56.object.Student;
import io.github.golok56.utility.Vocab;
import io.github.golok56.view.activity.SchoolMenuActivity;

public class AttendanceCheckActivity extends AppCompatActivity {

    // The school that currently active/user chose
    private School mSchool;

    // Thing to do every work that related with database
    private StudentInteractor mStudentController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_check);

        mSchool = getIntent().getParcelableExtra(Vocab.SCHOOL_EXTRA);
        ActionBar ab = getSupportActionBar();
        if(ab != null) {
            ab.setTitle(mSchool.getSchoolName());
        }
        init();
    }

    private void init(){
        mStudentController = new StudentInteractor(AttendanceCheckActivity.this, mSchool.getSchoolName());
        // Clearing the attended student list in case it is not empty and contain students
        // from another school
        if(!StudentInteractor.sSelectedStudents.isEmpty()){
            StudentInteractor.sSelectedStudents.clear();
        }
        initView();
    }

    private void initView(){
        // Inflate the layout with students from mSchool
        ListView lv = (ListView) findViewById(R.id.lv_student_attendance_check);
        lv.setAdapter(new AttendanceAdapter(this, mSchool.getStudents()));
        // Setting the button's onclicklistener and edittext's ontextchangelistener
        setButton();
        setEditText(lv);
    }

    private void setButton(){
        Button btn = (Button) findViewById(R.id.btn_attendance_submit);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Try to insert all the student's id in attended student list to database
                AttendanceInteractor attendanceInteractor = new AttendanceInteractor(AttendanceCheckActivity.this, mSchool.getSchoolName());
                int error = 0;
                for (int i = 0, size = StudentInteractor.sSelectedStudents.size(); i < size; ++i) {
                    if (attendanceInteractor.insert(StudentInteractor.sSelectedStudents.get(i).getId())) {
                        StudentInteractor.sSelectedStudents.get(i).addJumlahKehadiran();
                    } else {
                        error++;
                    }
                }
                if (error > 0) {
                    Toast.makeText(AttendanceCheckActivity.this, "Terjadi kesalahan! " + error + " murid gagal dimasukkan kedalam database.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AttendanceCheckActivity.this, "Behasil melakukan absen!", Toast.LENGTH_SHORT).show();
                }
                // Clear the attended student list as it not needed anymore
                StudentInteractor.sSelectedStudents.clear();
                AttendanceCheckActivity.this.startActivity(SchoolMenuActivity.getIntent(AttendanceCheckActivity.this, mSchool, true));
            }
        });
    }

    private void setEditText(final ListView lv){
        final EditText etSearch = (EditText) findViewById(R.id.et_absen_cari_murid);
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
                lv.setAdapter(new AttendanceAdapter(AttendanceCheckActivity.this, students));
            }
        });
    }

    public static Intent getIntent(Context context, Parcelable object) {
        Intent intent = new Intent(context, AttendanceCheckActivity.class);
        intent.putExtra(Vocab.SCHOOL_EXTRA, object);
        return intent;
    }

}

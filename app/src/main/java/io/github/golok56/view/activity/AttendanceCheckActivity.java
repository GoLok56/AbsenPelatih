package io.github.golok56.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
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
import io.github.golok56.presenter.AttendanceCheckPresenter;
import io.github.golok56.view.IAttendanceCheckView;

public class AttendanceCheckActivity extends AppCompatActivity implements IAttendanceCheckView {

    // The school that currently active/user chose
    private School mSchool;

    private AttendanceCheckPresenter mPresenter;

    private ListView mLvStudentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_check);

        mSchool = getIntent().getParcelableExtra(MainActivity.SCHOOL_EXTRA);

        mLvStudentList = (ListView) findViewById(R.id.lv_student_attendance_check);
        mLvStudentList.setAdapter(new AttendanceAdapter(this, mSchool.getStudents()));

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(mSchool.getSchoolName());
        }

        if (!StudentInteractor.sSelectedStudents.isEmpty()) {
            StudentInteractor.sSelectedStudents.clear();
        }

        mPresenter = new AttendanceCheckPresenter(
                this,
                new AttendanceInteractor(this, mSchool.getSchoolName()),
                new StudentInteractor(this, mSchool.getSchoolName())
        );

        findViewById(R.id.btn_attendance_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.onDoCheckClicked();
            }
        });

        final EditText etSearch = (EditText) findViewById(R.id.et_absen_cari_murid);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mPresenter.onSearchTextChanged(etSearch.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

    }

    @Override
    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(AttendanceCheckActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void showSchoolMenu() {
        startActivity(SchoolMenuActivity.getIntent(this, mSchool, true));
    }

    @Override
    public void setAdapter(ArrayList<Student> list) {
        mLvStudentList.setAdapter(new AttendanceAdapter(AttendanceCheckActivity.this, list));
    }

    public static Intent getIntent(Context context, Parcelable object) {
        Intent intent = new Intent(context, AttendanceCheckActivity.class);
        intent.putExtra(MainActivity.SCHOOL_EXTRA, object);
        return intent;
    }

}

package io.github.golok56.view.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import io.github.golok56.R;
import io.github.golok56.database.interactor.StudentInteractor;
import io.github.golok56.object.School;
import io.github.golok56.object.Student;
import io.github.golok56.presenter.AddStudentPresenter;
import io.github.golok56.view.IAddStudentView;


public class AddStudentActivity extends AppCompatActivity implements IAddStudentView {

    // Total students to add
    private int mTotalStudents;

    // Parent to inflate
    private LinearLayout mParentView;

    private AddStudentPresenter mPresenter;

    // The school to add the new students
    private School mSchool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_murid);

        mSchool = getIntent().getParcelableExtra(MainActivity.SCHOOL_EXTRA);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(mSchool.getSchoolName());
        }

        mPresenter = new AddStudentPresenter(this,
                new StudentInteractor(this, mSchool.getSchoolName()));

        mTotalStudents = getIntent().getIntExtra(SchoolMenuActivity.NUMBER_PICKER_VALUE_EXTRA, 1);
        mParentView = (LinearLayout) findViewById(R.id.view_tambah_murid_list_form);

        findViewById(R.id.btn_tambah_murid).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.onAddStudentClicked();
            }
        });

        mPresenter.setupForm();
    }

    @Override
    public boolean hasEmptyEditText() {
        for (int i = 0; i < mTotalStudents; i++) {
            View view = mParentView.getChildAt(i);
            EditText etStudentName = (EditText) view.findViewById(R.id.et_item_form_student_name);
            EditText etStudentClass = (EditText) view.findViewById(R.id.et_item_form_student_class);
            if (etStudentName.getText().toString().trim().isEmpty() ||
                    etStudentClass.getText().toString().trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void showSchoolMenu() {
        startActivity(SchoolMenuActivity.getIntent(this, mSchool, true));
    }

    @Override
    public void setupForm() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int sizeInDP = 16;

        // Converting the size to DP
        int marginInDp = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                sizeInDP,
                getResources().getDisplayMetrics()
        );

        // Inflate the form
        for (int i = 1; i <= mTotalStudents; i++) {
            @SuppressLint("InflateParams")
            View view = inflater.inflate(R.layout.item_form_murid, null);
            ((TextView) view.findViewById(R.id.tv_item_form_heading))
                    .setText(getString(R.string.student_form_heading, i));

            RelativeLayout child = (RelativeLayout) view.findViewById(R.id.view_item_form);
            mParentView.addView(child);
            LinearLayout.LayoutParams layoutParams =
                    (LinearLayout.LayoutParams) child.getLayoutParams();
            if (i == mTotalStudents) {
                layoutParams.setMargins(marginInDp, marginInDp, marginInDp, marginInDp);
            } else {
                layoutParams.setMargins(marginInDp, marginInDp, marginInDp, 0);
            }
            child.setLayoutParams(layoutParams);
        }
    }

    @Override
    public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public ArrayList<Student> getStudents() {
        ArrayList<Student> students = new ArrayList<>(mTotalStudents);
        for (int i = 0; i < mTotalStudents; i++) {
            View view = mParentView.getChildAt(i);
            EditText etStudentName = (EditText) view.findViewById(R.id.et_item_form_student_name);
            EditText etStudentClass = (EditText) view.findViewById(R.id.et_item_form_student_class);
            students.add(new Student(etStudentName.getText().toString(),
                    etStudentClass.getText().toString()));
        }
        return students;
    }

    // Get the intent and move to this activity
    public static Intent getIntent(Context context, Parcelable school, int value) {
        Intent intent = new Intent(context, AddStudentActivity.class);
        intent.putExtra(SchoolMenuActivity.NUMBER_PICKER_VALUE_EXTRA, value);
        intent.putExtra(MainActivity.SCHOOL_EXTRA, school);
        return intent;
    }

}

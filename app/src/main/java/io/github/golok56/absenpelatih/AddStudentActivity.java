package io.github.golok56.absenpelatih;

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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import io.github.golok56.R;
import io.github.golok56.database.interactor.StudentBaseInteractor;
import io.github.golok56.object.School;
import io.github.golok56.object.Student;
import io.github.golok56.utility.Component;
import io.github.golok56.utility.Vocab;


public class AddStudentActivity extends AppCompatActivity {

    // Total students to add
    private int mTotalStudents;

    // Parent to inflate
    private LinearLayout mParentView;

    // Thing to do every work that related with database
    private StudentBaseInteractor mStudentController;

    // The school to add the new students
    private School mSchool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_murid);

        mSchool = getIntent().getParcelableExtra(Vocab.SCHOOL_EXTRA);
        ActionBar ab = getSupportActionBar();
        if(ab != null) {
            ab.setTitle(mSchool.getSchoolName());
        }
        init();
    }

    private void init(){
        mTotalStudents = getIntent().getIntExtra(Vocab.NUMBER_PICKER_VALUE_EXTRA, 1);
        mParentView = (LinearLayout) findViewById(R.id.view_tambah_murid_list_form);
        mStudentController = new StudentBaseInteractor(this, mSchool.getSchoolName());

        setListView();

        // Set the listener for the button
        findViewById(R.id.btn_tambah_murid).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (emptyEditTextExist()) {
                    Toast.makeText(AddStudentActivity.this, "Tidak boleh ada field yang kosong!", Toast.LENGTH_SHORT).show();
                } else {
                    int count = 0;
                    for (int i = 0; i < mTotalStudents; i++) {
                        View view = mParentView.getChildAt(i);
                        String namaMurid = Component.getValue(view, R.id.et_item_form_nama_murid);
                        String kelasMurid = Component.getValue(view, R.id.et_item_form_kelas_murid);
                        Student student = new Student(namaMurid, kelasMurid);
                        if (mStudentController.insert(student)) {
                            mSchool.add(student);
                            count++;
                        }
                    }
                    Toast.makeText(AddStudentActivity.this, "Berhasil menambahkan sejumlah " + count + " murid!", Toast.LENGTH_SHORT).show();
                    startActivity(SchoolMenuActivity.getIntent(AddStudentActivity.this, mSchool, true));
                }
            }
        });
    }

    // Check if there any any forms that's empty
    private boolean emptyEditTextExist(){
        for(int i = 0; i < mTotalStudents; i++){
            View view = mParentView.getChildAt(i);
            String namaMurid = Component.getValue(view, R.id.et_item_form_nama_murid);
            String kelasMurid = Component.getValue(view, R.id.et_item_form_kelas_murid);
            if(namaMurid.trim().isEmpty() || kelasMurid.trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    // Creating the form as many as mTotalStudents
    private void setListView(){
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int sizeInDP = 16;

        // Converting the size to DP
        int marginInDp = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                sizeInDP,
                getResources().getDisplayMetrics()
        );

        // Inflate the form
        for(int i = 1; i <= mTotalStudents; i++){
            @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.item_form_murid, null);
            Component.setText(view, R.id.tv_item_form_heading, "Student ke-" + i);
            RelativeLayout child = (RelativeLayout) view.findViewById(R.id.view_item_form);
            mParentView.addView(child);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) child.getLayoutParams();
            if(i == mTotalStudents){
                layoutParams.setMargins(marginInDp, marginInDp, marginInDp, marginInDp);
            } else {
                layoutParams.setMargins(marginInDp, marginInDp, marginInDp, 0);
            }
            child.setLayoutParams(layoutParams);
        }
    }

    // Get the intent and move to this activity
    static Intent getIntent(Context context, Parcelable school, int value){
        Intent intent = new Intent(context, AddStudentActivity.class);
        intent.putExtra(Vocab.NUMBER_PICKER_VALUE_EXTRA, value);
        intent.putExtra(Vocab.SCHOOL_EXTRA, school);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

}

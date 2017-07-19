package kost.golok.absenpelatih;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import kost.golok.database.DBManager;
import kost.golok.object.Student;
import kost.golok.object.School;
import kost.golok.utility.Component;
import kost.golok.utility.manager.IntentManager;


public class TambahMuridActivity extends AppCompatActivity {

    private int mJumlahMurid;
    private LinearLayout mParentView;
    private DBManager mDb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_murid);
        init();
    }

    private void init(){
        mJumlahMurid = getIntent().getIntExtra(IntentManager.EXTRA_NILAI_NUMBER_PICKER, 1);
        mParentView = (LinearLayout) findViewById(R.id.view_tambah_murid_list_form);
        mDb = DBManager.getInstance(this);
        setListView();
        setButton();
    }

    private void setButton(){
        Button btn = (Button) findViewById(R.id.btn_tambah_murid);

        btn.setOnClickListener(v -> {
            if(emptyEditTextExist()){
                Toast.makeText(this, "Tidak boleh ada field yang kosong!", Toast.LENGTH_SHORT).show();
            } else {
                School school = getIntent().getParcelableExtra(IntentManager.SCHOOL_EXTRA);
                int count = 0;
                for(int i = 0; i < mJumlahMurid; i++){
                    View view = mParentView.getChildAt(i);
                    String namaMurid = Component.getValue(view, R.id.et_item_form_nama_murid);
                    String kelasMurid = Component.getValue(view, R.id.et_item_form_kelas_murid);
                    Student student = new Student(namaMurid, kelasMurid);
                    int id = mDb.insert(student, school.getSchoolName());
                    if(id != -1){
                        student.setId(id);
                        school.add(student);
                        count++;
                    }
                }
                Toast.makeText(this, "Berhasil menambahkan sejumlah " + count + " murid!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MenuSekolahActivity.class);
                intent.putExtra(IntentManager.SCHOOL_EXTRA, school);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    private boolean emptyEditTextExist(){
        for(int i = 0; i < mJumlahMurid; i++){
            View view = mParentView.getChildAt(i);
            String namaMurid = Component.getValue(view, R.id.et_item_form_nama_murid);
            String kelasMurid = Component.getValue(view, R.id.et_item_form_kelas_murid);
            if(namaMurid.trim().isEmpty() || kelasMurid.trim().isEmpty())
                return true;
        }
        return false;
    }

    private void setListView(){
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int sizeInDP = 16;

        int marginInDp = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, sizeInDP, getResources()
                        .getDisplayMetrics());

        for(int i = 1; i <= mJumlahMurid; i++){
            @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.item_form_murid, null);
            Component.setText(view, R.id.tv_item_form_heading, "Student ke-" + i);
            RelativeLayout child = (RelativeLayout) view.findViewById(R.id.view_item_form);
            mParentView.addView(child);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) child.getLayoutParams();
            if(i == mJumlahMurid){
                layoutParams.setMargins(marginInDp, marginInDp, marginInDp, marginInDp);
            } else {
                layoutParams.setMargins(marginInDp, marginInDp, marginInDp, 0);
            }
            child.setLayoutParams(layoutParams);
        }
    }

}

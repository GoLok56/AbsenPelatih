package kost.golok.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.util.SparseArrayCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

import kost.golok.absenpelatih.R;
import kost.golok.object.Student;
import kost.golok.utility.Component;

public class StudentAdapter extends ArrayAdapter<Student> {

    private int mLayoutId;

    public StudentAdapter(Context context, ArrayList<Student> sekolah, int id) {
        super(context, 0, sekolah);
        mLayoutId = id;
    }

    @NonNull
    @Override
    public View getView(int pos, View view, @NonNull ViewGroup parent) {
        // Get the data item for this pos
        Student student = getItem(pos);

        // Check if an existing view is being reused, otherwise inflate the view
        if (view == null)
            view = LayoutInflater.from(getContext()).inflate(mLayoutId, parent, false);

        if (student != null)
            createTextView(view, textViewMap(student));


        return view;
    }

    private SparseArrayCompat<String> textViewMap(Student student){
        SparseArrayCompat<String> data = new SparseArrayCompat<>();
        data.put(R.id.tv_item_murid_nama_murid, student.getName());
        data.put(R.id.tv_item_murid_kelas, "Kelas " + student.getStudentClass());
        data.put(R.id.tv_item_murid_jumlah_kehadiran_murid, "Jumlah kehadiran: " + student.getTotalAttendance());
        return data;
    }

    private  void createTextView(View view, SparseArrayCompat<String> data){
        for (int i = 0; i < data.size(); i++) {
            int id = data.keyAt(i);
            String value = data.get(id);
            Component.setText(view, id, value);
        }
    }

}

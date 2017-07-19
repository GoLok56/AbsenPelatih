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
import kost.golok.object.School;
import kost.golok.utility.Component;

public class SekolahAdapter extends ArrayAdapter<School> {

    public SekolahAdapter(Context context, ArrayList<School> school) {
        super(context, 0, school);
    }

    @NonNull
    @Override
    public View getView(int pos, View view, @NonNull ViewGroup parent) {
        // Get the data item for this pos
        School school = getItem(pos);

        // Check if an existing view is being reused, otherwise inflate the view
        if (view == null)
            view = LayoutInflater.from(getContext()).inflate(R.layout.item_sekolah, parent, false);

        if (school != null)
            createTextView(view, textViewMap(school, pos+1));


        return view;
    }

    private SparseArrayCompat<String> textViewMap(School school, int pos){
        SparseArrayCompat<String> data = new SparseArrayCompat<>();
        data.put(R.id.tv_item_sekolah_id_sekolah, "" + pos);
        data.put(R.id.tv_item_sekolah_nama_sekolah, school.getSchoolName());
        data.put(R.id.tv_item_sekolah_jumlah_murid, "Jumlah Murid: " + school.jumlahMurid());
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

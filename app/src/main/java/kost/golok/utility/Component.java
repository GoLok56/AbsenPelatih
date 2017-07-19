package kost.golok.utility;

import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class Component {

    private Component(){}

    public static void setText(View view, int id, String text){
        TextView tv = (TextView) view.findViewById(id);
        tv.setText(text);
    }

    public static String getText(AppCompatActivity activity, int id){
        TextView tv = (TextView) activity.findViewById(id);
        return tv.getText().toString();
    }

    public static String getText(View view, int id){
        TextView tv = (TextView) view.findViewById(id);
        return tv.getText().toString();
    }

    public static String getValue(View view, int id){
        EditText et = (EditText) view.findViewById(id);
        return et.getText().toString();
    }

}

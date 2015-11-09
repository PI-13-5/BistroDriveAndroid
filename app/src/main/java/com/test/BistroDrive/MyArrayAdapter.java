package com.test.BistroDrive;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.test.BistroDrive.R;

import java.util.List;

public class MyArrayAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final List<String> values;

    public MyArrayAdapter(Context context, List<String> values) {
        super(context, R.layout.list_item_view, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_item_view, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.text1);
        textView.setText(values.get(position));

        TextView textIconView = (TextView) rowView.findViewById(R.id.textIcon);
        textIconView.setText(Character.toString(values.get(position).charAt(0)));
        //LinearLayout iconLayout = (LinearLayout) rowView.findViewById(R.id.iconLayout);
/*        Drawable bgDrawable = (Drawable)rowView.getBackground();
        final GradientDrawable shape = (GradientDrawable)   bgDrawable.findDrawableByLayerId(R.id.shape);
        shape.setColor(232323);*/
        return rowView;
    }
}

package com.test.BistroDrive.Fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.test.BistroDrive.R;


public class SecondFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_second, container, false);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        TextView textView = (TextView) getActivity().findViewById(R.id.textView);
        textView.setText(getArguments().getString("result"));
    }
}

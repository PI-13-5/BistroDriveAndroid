package com.test.BistroDrive.Fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.test.BistroDrive.R;

public class FirstFragment extends Fragment {

    private String result;

    public interface onSomeEventListener{
        void someEvent(String s);
    }

    onSomeEventListener SomeEventListener;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try{
            SomeEventListener = (onSomeEventListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()+"must implement onSomeEventListener");
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_first, container, false);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        Button btn = (Button) getActivity().findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText edtFrom1Fragment = (EditText) getActivity().findViewById(R.id.editText);
                result = edtFrom1Fragment.getText().toString();
                SomeEventListener.someEvent(result);
            }
        });

    }
}

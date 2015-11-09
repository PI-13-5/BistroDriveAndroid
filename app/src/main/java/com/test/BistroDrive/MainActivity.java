package com.test.BistroDrive;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.widget.ListView;

import com.test.BistroDrive.R;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {

    private List<String> arrPubs = new ArrayList<String>();
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arrPubs = getIntent().getStringArrayListExtra("pubs");
        ListView lvMain = (ListView) findViewById(R.id.listView);

        // создаем адаптер
       /* ArrayAdapter<String> adapter = new <String>ArrayAdapter(this,
                R.layout.list_item_view, R.id.text1,arrPubs);*/
        MyArrayAdapter adapter = new MyArrayAdapter(this, arrPubs);
        // присваиваем адаптер списку
        lvMain.setAdapter(adapter);


    }
}

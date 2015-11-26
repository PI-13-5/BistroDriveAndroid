package com.test.BistroDrive.Activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.test.BistroDrive.R;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class DishActivity extends AppCompatActivity {

    TextView dishN;
    TextView dishType;
    TextView dishIngredient;
    TextView dishPrice;
    TextView dishPriceWithIng;
    TextView dishDesc;

    ArrayList<String> dish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dish);

        // enabling action bar app icon and behaving it as toggle button
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Блюдо");

        Intent intent = getIntent();
        dish = intent.getStringArrayListExtra("dish");

        dishN = (TextView) findViewById(R.id.dishName);
        dishType = (TextView) findViewById(R.id.dishType);
        dishIngredient = (TextView) findViewById(R.id.dishIngredient);
        dishPrice = (TextView) findViewById(R.id.dishTotal);
        dishPriceWithIng = (TextView) findViewById(R.id.dishTotalWithIng);
        dishDesc = (TextView) findViewById(R.id.dishComment);


        dishN.setText("Название: "+dish.get(1));
        dishType.setText("Категория: "+dish.get(4));
        dishIngredient.setText("Ингредиенты: "+dish.get(3));
        dishPrice.setText("Цена: " +dish.get(6));
        dishPriceWithIng.setText("Цена с ингредиентами: " + dish.get(7));

        if(!dish.get(2).equals(null)){
            dishDesc.setText(dish.get(2));}
        else{
            findViewById(R.id.dishDescHeader).setVisibility(View.INVISIBLE);
            dishDesc.setVisibility(View.INVISIBLE);}

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return (super.onOptionsItemSelected(menuItem));
    }
}

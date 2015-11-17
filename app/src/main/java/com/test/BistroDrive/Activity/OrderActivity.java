package com.test.BistroDrive.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.test.BistroDrive.R;


public class OrderActivity extends AppCompatActivity {

    TextView orderDeadline;
    TextView orderIngredientBuyer;
    TextView orderPayment;
    TextView orderDelivery;
    TextView orderTotal;
    ImageView orderCookImage;
    TextView orderCookText;
    TextView orderComment;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        // enabling action bar app icon and behaving it as toggle button
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Заказ");

        Intent intent = getIntent();
        orderComment = (TextView) findViewById(R.id.orderComment);
        orderDeadline = (TextView) findViewById(R.id.orderDeadline);
        orderPayment = (TextView) findViewById(R.id.orderPayment);
        orderIngredientBuyer = (TextView) findViewById(R.id.orderIngredientBuyer);
        orderDelivery = (TextView) findViewById(R.id.orderDelivery);
        orderTotal = (TextView) findViewById(R.id.orderTotal);
        orderCookText = (TextView) findViewById(R.id.orderCookText);
        orderCookImage = (ImageView) findViewById(R.id.orderCookAvatar);

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

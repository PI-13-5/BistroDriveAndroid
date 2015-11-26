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


public class OrderActivity extends AppCompatActivity {

    TextView orderDeadline;
    TextView orderIngredientBuyer;
    TextView orderPayment;
    TextView orderDelivery;
    TextView orderTotal;
    ImageView orderCookImage;
    TextView orderCookText;
    TextView orderComment;
    TextView orderEmail;

    TextView orderCookHeader;

    private ArrayList<String> order;
    private ArrayList<String> cook;
    private ArrayList<String> customer;

    private View mProgressView;
    private View mOrderFormView;

    private boolean isInput;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        // enabling action bar app icon and behaving it as toggle button
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Заказ");

        Intent intent = getIntent();
        order = intent.getStringArrayListExtra("info");
        cook= intent.getStringArrayListExtra("cook");
        customer = intent.getStringArrayListExtra("customer");
        isInput = intent.getBooleanExtra("isInput", true);

        mProgressView = findViewById(R.id.order_progress);
        mOrderFormView = findViewById(R.id.scrollOrder);

        orderComment = (TextView) findViewById(R.id.orderComment);
        orderDeadline = (TextView) findViewById(R.id.orderDeadline);
        orderPayment = (TextView) findViewById(R.id.orderPayment);
        orderIngredientBuyer = (TextView) findViewById(R.id.orderIngredientBuyer);
        orderDelivery = (TextView) findViewById(R.id.orderDelivery);
        orderTotal = (TextView) findViewById(R.id.orderTotal);
        orderCookText = (TextView) findViewById(R.id.orderCookText);
        orderCookImage = (ImageView) findViewById(R.id.orderCookAvatar);
        orderEmail = (TextView) findViewById(R.id.orderEmail);
        orderCookHeader = (TextView) findViewById(R.id.orderCookHeader);

        showProgress(true);
        if(isInput)
        new OrderTask(customer.get(6),orderCookImage).execute();
        else new OrderTask(cook.get(5),orderCookImage).execute();

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

    public class OrderTask extends AsyncTask<Void, Void, Bitmap> {

        private String url;
        private ImageView imageView;

        public OrderTask(String url, ImageView imageView) {
            this.url = url;
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                URL urlConnection = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) urlConnection
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            imageView.setImageBitmap(result);
            if(order.get(7)!=null){
            orderComment.setText(order.get(7));}
            else{
                findViewById(R.id.orderCommentHeader).setVisibility(View.INVISIBLE);
                orderComment.setVisibility(View.INVISIBLE);}
            orderDeadline.setText("Дедлайн: " + order.get(1));
            orderPayment.setText("Оплата: " + order.get(3));
            orderIngredientBuyer.setText("Покупает ингредиенты: "+ order.get(2));
            orderDelivery.setText("Доставка: " + order.get(5));
            orderTotal.setText("Оплата: " + order.get(8));
            if(isInput) {
                if( customer.get(2).equals(null))
                orderCookText.setText(customer.get(2));
                else orderCookText.setText(customer.get(3)+ " " + customer.get(4));
                orderEmail.setText(customer.get(1));
                orderCookHeader.setText("Заказчик");
            }
            else {

                orderCookText.setText(cook.get(2));
                orderEmail.setText(cook.get(1));
                orderCookHeader.setText("Повар");
            }
            showProgress(false);
        }

    }

    public void showProgress(final boolean show) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mOrderFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mOrderFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mOrderFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mOrderFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}

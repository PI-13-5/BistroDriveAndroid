package com.test.BistroDrive.Fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.test.BistroDrive.R;
import com.test.BistroDrive.RecyclerAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;


public class InputOrdersFragment extends Fragment {

    private View v;
    private View mProgressView;
    private View mInputOrdersFormView;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ParserInputOrders mInputOrdersTask = null;

    ArrayList<String> myDataset;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_input_orders, container, false);
        return rootView;
    }

    @Override
    public void onStart() {

        super.onStart();
        v = getView();
        mProgressView = v.findViewById(R.id.inputOrders_progress);
        mInputOrdersFormView = v.findViewById(R.id.inputOrders_recycler_view);

        myDataset = new ArrayList<String>();

        mRecyclerView = (RecyclerView) v.findViewById(R.id.my_recycler_view);

        showProgress(true);
        mInputOrdersTask = new ParserInputOrders(getArguments().getString("token"));
        mInputOrdersTask.execute();

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // если мы уверены, что изменения в контенте не изменят размер layout-а RecyclerView
        // передаем параметр true - это увеличивает производительность
        mRecyclerView.setHasFixedSize(true);

        // используем linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

    }

    private ArrayList<String> getDataSet(List<ArrayList<Object>> lstOrders) {

        ArrayList<String> mDataSet = new ArrayList<>();
        for (ArrayList<Object> lstOrder:lstOrders) {
            ArrayList<String> lstCustomer = (ArrayList<String>)lstOrder.get(11);
            if( lstCustomer.get(2)=="null")
                mDataSet.add("Заказ от пользователя: " + lstCustomer.get(3) + " " +lstCustomer.get(4));
            else
                mDataSet.add("Заказ от пользователя: " + lstCustomer.get(2));
        }
        return mDataSet;
    }

    public void showProgress(final boolean show) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mInputOrdersFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mInputOrdersFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mInputOrdersFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mInputOrdersFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    class ParserInputOrders extends AsyncTask<Void, Void, List<ArrayList<Object>>> {

        String resultJson = "";
        String tokenStr="";
        public ParserInputOrders(String tok)
        {
            this.tokenStr = tok;
        }

        @Override
        protected List<ArrayList<Object>> doInBackground(Void... params) {
            URL url;
            HttpURLConnection conn=null;
            try {

                url = new URL("http://bistrodrive.azurewebsites.net/api");

                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);


                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write("{\n" +
                        "    Method:\"orderlist\",\n" +
                        "    Token:\""+tokenStr+"\",\n" +
                        "    Parameters:{\n" +
                        "        incomingOrders:\"true\"\n" +
                        "    }\n" +
                        "}");

                writer.flush();
                writer.close();
                os.close();
                int responseCode=conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line=br.readLine()) != null) {
                        resultJson+=line;
                    }
                }
                else {
                    resultJson="";
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                conn.disconnect();
            }
            Log.d("INPUTORDERS", resultJson);

            JSONObject dataJsonObj = null;

            List<ArrayList<Object>> allInputOrders = new ArrayList<ArrayList<Object>>();

            try {
                dataJsonObj = new JSONObject(resultJson);
                JSONArray jArrInOrders = dataJsonObj.getJSONArray("Result");

                for (int i = 0; i < jArrInOrders.length(); i++) {

                    JSONObject jOrder = jArrInOrders.getJSONObject(i);

                    ArrayList<Object> arrOrder = new ArrayList<Object>();

                    arrOrder.add(jOrder.getString("Id_Order"));//0
                    arrOrder.add(jOrder.getString("Deadline"));//1
                    arrOrder.add(jOrder.getString("IngridientsBuyer"));//2
                    arrOrder.add(jOrder.getString("Payment"));//3
                    arrOrder.add(jOrder.getString("Comunication"));//4
                    arrOrder.add(jOrder.getString("Delivery"));//5
                    arrOrder.add(jOrder.getString("Status"));//6
                    arrOrder.add(jOrder.getString("Comment"));//7
                    arrOrder.add(jOrder.getString("Total"));//8
                    arrOrder.add(jOrder.getString("FinishTime"));//9

                    JSONObject jCook = jOrder.getJSONObject("Cook");
                    ArrayList<String> arrCook = new ArrayList<String>();
                    arrCook.add(jCook.getString("Id"));//0
                    arrCook.add(jCook.getString("Email"));//1
                    arrCook.add(jCook.getString("Username"));//2
                    arrCook.add(jCook.getString("FirstName"));//3
                    arrCook.add(jCook.getString("Surname"));//4
                    arrCook.add(jCook.getString("AvatarUrl"));//5
                    arrCook.add(jCook.getString("Phone"));//6
                    arrOrder.add(arrCook);//10  - orderList

                    JSONObject jCustomer = jOrder.getJSONObject("Customer");
                    ArrayList<String> arrCustomer = new ArrayList<String>();
                    arrCustomer.add(jCustomer.getString("Id"));//0
                    arrCustomer.add(jCustomer.getString("Email"));//1
                    arrCustomer.add(jCustomer.getString("Username"));//2
                    arrCustomer.add(jCustomer.getString("FirstName"));//3
                    arrCustomer.add(jCustomer.getString("Surname"));//4
                    arrCustomer.add(jCustomer.getString("Phone"));//5
                    arrCustomer.add(jCustomer.getString("AvatarUrl"));//6
                    arrCustomer.add(jCustomer.getString("Address"));//7
                    arrCustomer.add(jCustomer.getString("Description"));//7
                    arrOrder.add(arrCustomer);//11 - orderList

                    allInputOrders.add(arrOrder);

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return allInputOrders;

        }

        @Override
        protected void onPostExecute(List<ArrayList<Object>> results) {
            mInputOrdersTask = null;
            super.onPostExecute(results);

            myDataset = getDataSet(results);

            // создаем адаптер
            mAdapter = new RecyclerAdapter(myDataset,getActivity().getApplicationContext());
            mRecyclerView.setAdapter(mAdapter);

            showProgress(false);
        }

    }

    public class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {

        private String url;
        private ImageView imageView;

        public ImageLoadTask(String url, ImageView imageView) {
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
            showProgress(false);
        }

    }
}
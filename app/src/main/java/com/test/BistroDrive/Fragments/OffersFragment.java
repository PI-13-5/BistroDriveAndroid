package com.test.BistroDrive.Fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.test.BistroDrive.Activity.DishActivity;
import com.test.BistroDrive.DataObject;
import com.test.BistroDrive.MyRecyclerViewAdapterCardView;
import com.test.BistroDrive.R;

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
import java.util.List;

import javax.net.ssl.HttpsURLConnection;


public class OffersFragment extends Fragment {

    private View v;
    private View mProgressView;
    private View mOffersFormView;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private static String LOG_TAG = "CardViewActivity";

    private ParserOffers mOfferTask = null;
    private ParserOffers.ImageLoadTask mImageTask = null;

    private ArrayList<DataObject> myDataset = new ArrayList<DataObject>();
    private List<ArrayList<String>> mLstOrders;
    private ArrayList<Bitmap> arrBitmap;

    private int page = 0;

    private Intent intent;

    @Override
    public void onStop() {
        super.onStop();
        if (mImageTask!=null){
            Log.d("TaskOFFER", "onStop IMage task");
        mImageTask.cancel(true);}
        if(mOfferTask!=null){
            Log.d("TaskOFFER", "onStop offer task");
        mOfferTask.cancel(true);}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_offers, container, false);
        return rootView;
    }

    @Override
    public void onStart() {

        super.onStart();

        v = getView();

        mProgressView = v.findViewById(R.id.offers_progress);
        mOffersFormView = v.findViewById(R.id.offers_recycler_view);

        intent = new Intent(getActivity().getApplicationContext(), DishActivity.class);

        if(myDataset.size()==0) {
            showProgress(true);
            mOfferTask = new ParserOffers(getArguments().getString("token"));
            mOfferTask.execute();
        }
        else
        {
            initRecyclerViewCard();
        }
    }


    private void initRecyclerViewCard() {

        mRecyclerView = (RecyclerView) v.findViewById(R.id.my_recycler_view_cardView);
        mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MyRecyclerViewAdapterCardView(myDataset);
        mRecyclerView.setAdapter(mAdapter);

        // Code to Add an item with default animation
        //((MyRecyclerViewAdapter) mAdapter).addItem(obj, index);

        // Code to remove an item with default animation
        //((MyRecyclerViewAdapter) mAdapter).deleteItem(index);
                ((MyRecyclerViewAdapterCardView) mAdapter).setOnItemClickListener(new MyRecyclerViewAdapterCardView.MyClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                ArrayList <String> temp = mLstOrders.get(position);
                intent.putStringArrayListExtra("dish", temp);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    public void showProgress(final boolean show) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mOffersFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mOffersFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mOffersFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mOffersFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    class ParserOffers extends AsyncTask<Void, Void, List<ArrayList<String>>> {

        String resultJson = "";
        String tokenStr="";
        public ParserOffers(String tok)
        {
            this.tokenStr = tok;
        }

        @Override
        protected List<ArrayList<String>> doInBackground(Void... params) {
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
                        "    Method:\"offers\",\n" +
                        "    Token:\""+tokenStr+"\",\n" +
                        "    Parameters:{\n" +
                        "        Limit:\"10\",\n" +
                        "        Page:\""+page+"\"\n" +
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

            JSONObject dataJsonObj = null;

            List<ArrayList<String>> allOffers = new ArrayList<>();

            try {
                dataJsonObj = new JSONObject(resultJson);
                JSONObject dataJsonObjRes =  dataJsonObj.getJSONObject("Result");
                JSONArray jLstOffers = dataJsonObjRes.getJSONArray("List");
                for (int i = 0; i < jLstOffers.length(); i++) {
                    JSONObject jOffer = jLstOffers.getJSONObject(i);
                    ArrayList<String> lstOffer = new ArrayList<>();
                    lstOffer.add(jOffer.getString("Id"));//0
                    lstOffer.add(jOffer.getString("Name"));//1
                    lstOffer.add(jOffer.getString("Description"));//2
                    lstOffer.add(jOffer.getString("Ingridients"));//3
                    lstOffer.add(jOffer.getString("Type"));//4
                    lstOffer.add(jOffer.getString("Image"));//5
                    lstOffer.add(jOffer.getString("Price"));//6
                    lstOffer.add(jOffer.getString("PriceWithIngridients"));//7

                    allOffers.add(lstOffer);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return allOffers;

        }

        @Override
        protected void onPostExecute(List<ArrayList<String>> results) {
            mOfferTask = null;
            super.onPostExecute(results);
            mLstOrders = results;
            myDataset = getDataSet(mLstOrders,arrBitmap);
            initRecyclerViewCard();
            showProgress(false);
            mImageTask = new ImageLoadTask(results);
            mImageTask.execute();
        }

        public class ImageLoadTask extends AsyncTask<Void, Void, ArrayList<Bitmap>> {

            private List<ArrayList<String>> results;

            public ImageLoadTask(List<ArrayList<String>> results) {
                this.results = results;
            }

            @Override
            protected void onCancelled(ArrayList<Bitmap> bitmaps) {
                super.onCancelled(bitmaps);
                mOfferTask = null;
            }

            @Override
            protected ArrayList<Bitmap> doInBackground(Void... params) {
                ArrayList<Bitmap> bt = new ArrayList<>();
                try {
                    for (int i = 0; i < results.size(); i++) {
                        if (isCancelled()) break;
                        ArrayList<String> buf = results.get(i);
                    URL urlConnection = new URL(buf.get(5));
                    HttpURLConnection connection = (HttpURLConnection) urlConnection
                            .openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    Bitmap myBitmap = BitmapFactory.decodeStream(input);

                    bt.add(myBitmap);
                    }
                    return bt;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(ArrayList<Bitmap> result) {
                super.onPostExecute(result);
                arrBitmap=result;
                myDataset = getDataSet(mLstOrders,arrBitmap);
                initRecyclerViewCard();
            }

        }
    }
    private ArrayList<DataObject> getDataSet(List<ArrayList<String>> results,ArrayList<Bitmap> bt){
        ArrayList<DataObject> mDataSet = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            ArrayList<String> offer = results.get(i);
            DataObject obj = null;
            if(bt!=null) {
                obj = new DataObject(offer.get(1), bt.get(i), offer.get(6), offer.get(7), offer.get(4));
            }
            else{
                obj = new DataObject(offer.get(1), null, offer.get(6), offer.get(7), offer.get(4));
            }
            mDataSet.add(obj);
        }
        return mDataSet;
    }
    }

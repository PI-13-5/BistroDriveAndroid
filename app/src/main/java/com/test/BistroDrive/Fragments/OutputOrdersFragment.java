package com.test.BistroDrive.Fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.test.BistroDrive.Activity.OrderActivity;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.transform.Result;


public class OutputOrdersFragment extends Fragment {

    private View v;
    private View mProgressView;
    private View mOutputOrdersFormView;

    Intent intent;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ParserOutputOrders mOutputOrdersTask = null;

    private ArrayList<String> myDataset = new ArrayList<String>();
    List<ArrayList<Object>> mLstOrders;
    ArrayList<String> cook,otherOrderInfo,customer;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_output_orders, container, false);
        return rootView;
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mOutputOrdersTask!=null)
            mOutputOrdersTask.cancel(true);
    }

    @Override
    public void onStart() {
        super.onStart();

        v = getView();

        mProgressView = v.findViewById(R.id.outputOrders_progress);
        mOutputOrdersFormView = v.findViewById(R.id.outputOrders_recycler_view);


        intent = new Intent(getActivity().getApplicationContext(), OrderActivity.class);

        if(myDataset.size()==0) {
            showProgress(true);
            mOutputOrdersTask = new ParserOutputOrders(getArguments().getString("token"));
            mOutputOrdersTask.execute();
        }
        else
        {
            initRecyclerView();
        }

    }

    private ArrayList<String> getDataSet(List<ArrayList<Object>> lstOrders) {

        mLstOrders = lstOrders;
        ArrayList<String> mDataSet = new ArrayList<>();
        for (ArrayList<Object> lstOrder:lstOrders) {
            ArrayList<String> lstCook = (ArrayList<String>)lstOrder.get(10);
            if( lstCook.get(2)=="null")
                mDataSet.add("Заказ у пользователя: " + lstCook.get(3) + " " + lstCook.get(4));
            else
                mDataSet.add("Заказ у пользователя: " + lstCook.get(2));
        }
        return mDataSet;
    }

    public void showProgress(final boolean show) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mOutputOrdersFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mOutputOrdersFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mOutputOrdersFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mOutputOrdersFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    class ParserOutputOrders extends AsyncTask<Void, Void, List<ArrayList<Object>>> {

        String resultJson = "";
        String tokenStr="";
        public ParserOutputOrders(String tok)
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
                        "        incomingOrders:\"false\"\n" +
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

            List<ArrayList<Object>> allInputOrders = new ArrayList<ArrayList<Object>>();

            try {
                dataJsonObj = new JSONObject(resultJson);
                JSONArray jArrInOrders = dataJsonObj.getJSONArray("Result");

                for (int i = 0; i < jArrInOrders.length(); i++) {

                    JSONObject jOrder = jArrInOrders.getJSONObject(i);

                    ArrayList<Object> arrOrder = new ArrayList<Object>();

                    arrOrder.add(jOrder.getString("Id_Order"));//0
                    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    java.util.Date time=new java.util.Date(Long.parseLong(jOrder.getString("Deadline"))*1000);
                    String strDate = sdfDate.format(time);
                    arrOrder.add(strDate);//1
                    arrOrder.add(jOrder.getString("IngridientsBuyer"));//2
                    arrOrder.add(jOrder.getString("Payment"));//3
                    arrOrder.add(jOrder.getString("Comunication"));//4
                    arrOrder.add(jOrder.getString("Delivery"));//5
                    arrOrder.add(jOrder.getString("Status"));//6
                    if (jOrder.getString("Comment")!="null") {
                        arrOrder.add(jOrder.getString("Comment"));//7
                    }
                    else arrOrder.add(null);
                    arrOrder.add(jOrder.getString("Total"));//8
                    if (jOrder.getString("FinishTime")!="null") {
                        time = new java.util.Date(Long.parseLong(jOrder.getString("FinishTime")) * 1000);
                        strDate = sdfDate.format(time);
                        arrOrder.add(strDate);//9
                    }
                    else{
                        arrOrder.add(null);
                    }

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
            mOutputOrdersTask = null;
            super.onPostExecute(results);
            myDataset = getDataSet(results);
            initRecyclerView();
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


    private void initRecyclerView() {
        mRecyclerView = (RecyclerView) v.findViewById(R.id.output_my_recycler_view);
        mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
//        mLayoutManager = new GridLayoutManager(this, 2);
//        mLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new RecyclerView.Adapter<CustomViewHolder>() {
            @Override
            public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, final int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(android.R.layout.simple_list_item_1
                        , viewGroup, false);
                view.setBackgroundResource(android.R.drawable.list_selector_background);
                return new CustomViewHolder(view);
            }

            @Override
            public void onBindViewHolder(CustomViewHolder viewHolder, int i) {
                viewHolder.mTextView.setText(myDataset.get(i));
                viewHolder.mTextView.setPressed(false);
            }

            @Override
            public int getItemCount() {
                return myDataset.size();
            }
        };
        mRecyclerView.setAdapter(mAdapter);

/*        SwipeDismissRecyclerViewTouchListener touchListener =
                new SwipeDismissRecyclerViewTouchListener(
                        mRecyclerView,
                        new SwipeDismissRecyclerViewTouchListener.DismissCallbacks() {
                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }

                            @Override
                            public void onDismiss(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    mItems.remove(position);
                                }
                                // do not call notifyItemRemoved for every item, it will cause gaps on deleting items
                                mAdapter.notifyDataSetChanged();
                            }
                        });
        mRecyclerView.setOnTouchListener(touchListener);
        // Setting this scroll listener is required to ensure that during ListView scrolling,
        // we don't look for swipes.
        mRecyclerView.setOnScrollListener(touchListener.makeScrollListener());*/
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(mRecyclerView,
                new OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        getListsOnPos(mLstOrders.get(position));
                        intent.putStringArrayListExtra("cook", cook);
                        intent.putStringArrayListExtra("customer", customer);
                        intent.putStringArrayListExtra("info",otherOrderInfo);
                        intent.putExtra("isInput",false);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                }));
    }

    private class CustomViewHolder extends RecyclerView.ViewHolder {

        private TextView mTextView;

        public CustomViewHolder(View itemView) {
            super(itemView);

            mTextView = (TextView) itemView.findViewById(android.R.id.text1);
        }
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

    public class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {

        private OnItemClickListener mListener;

        private static final long DELAY_MILLIS = 100;

        private RecyclerView mRecyclerView;
        private GestureDetector mGestureDetector;
        private boolean mIsPrepressed = false;
        private boolean mIsShowPress = false;
        private View mPressedView = null;

        public RecyclerItemClickListener(RecyclerView recyclerView, OnItemClickListener listener) {
            mListener = listener;
            mRecyclerView = recyclerView;
            mGestureDetector = new GestureDetector(recyclerView.getContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDown(MotionEvent e) {
                    mIsPrepressed = true;
                    mPressedView = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
                    super.onDown(e);
                    return false;
                }

                @Override
                public void onShowPress(MotionEvent e) {
                    if (mIsPrepressed && mPressedView != null) {
                        mPressedView.setPressed(true);
                        mIsShowPress = true;
                    }
                    super.onShowPress(e);
                }

                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    if (mIsPrepressed && mPressedView != null) {
                        mPressedView.setPressed(true);
                        final View pressedView = mPressedView;
                        pressedView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                pressedView.setPressed(false);
                            }
                        }, DELAY_MILLIS);
                        mIsPrepressed = false;
                        mPressedView = null;
                    }
                    return true;
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
            View childView = view.findChildViewUnder(e.getX(), e.getY());
            if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
                mListener.onItemClick(childView, view.getChildPosition(childView));
            } else if (e.getActionMasked() == MotionEvent.ACTION_UP && mPressedView != null && mIsShowPress) {
                mPressedView.setPressed(false);
                mIsShowPress = false;
                mIsPrepressed = false;
                mPressedView = null;
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) {
        }
        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    private void getListsOnPos(ArrayList<Object> lst){
        cook = (ArrayList<String>)lst.get(10);
        customer = (ArrayList<String>)lst.get(11);
        otherOrderInfo = new ArrayList<String>();
        for (int i = 0; i < lst.size(); i++) {
            if(i!=10 && i!=11)
            otherOrderInfo.add((String)lst.get(i));
        }

    }

}

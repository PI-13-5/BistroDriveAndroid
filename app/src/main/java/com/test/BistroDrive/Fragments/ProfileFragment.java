package com.test.BistroDrive.Fragments;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.test.BistroDrive.R;

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

public class ProfileFragment extends Fragment{

    private View mProgressView;
    private View mProfileFormView;
    private View mProfileFormScrollView;

    private ParserProfile mProfileTask = null;

    TextView textViewEmail;
    TextView textViewUserName;
    TextView textViewName;
    TextView textViewDescriprion;
    TextView textViewAddress;
    ImageView imageViewPortrait;


    public void showProgress(final boolean show) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mProfileFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mProfileFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProfileFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProfileFormScrollView.setVisibility(show ? View.GONE : View.VISIBLE);
            mProfileFormScrollView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProfileFormScrollView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mProfileFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mProfileFormScrollView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        View v = getView();
        mProgressView = v.findViewById(R.id.profile_progress);
        mProfileFormView = v.findViewById(R.id.profile);
        mProfileFormScrollView = v.findViewById(R.id.scrollProfile);

        textViewEmail = (TextView) getActivity().findViewById(R.id.email);
        textViewUserName = (TextView) getActivity().findViewById(R.id.name);
        textViewName = (TextView) getActivity().findViewById(R.id.NameAndSurname);
        textViewDescriprion = (TextView) getActivity().findViewById(R.id.profileDesc);
        textViewAddress = (TextView) getActivity().findViewById(R.id.profilePlace);
        imageViewPortrait = (ImageView) getActivity().findViewById(R.id.profileImg);

        showProgress(true);
        mProfileTask = new ParserProfile(getArguments().getString("token"));
        mProfileTask.execute();

    }

    class ParserProfile extends AsyncTask<Void, Void, List<String>> {

        String resultJson = "";
        String tokenStr="";
        public ParserProfile(String tok)
        {
            this.tokenStr = tok;
        }

        @Override
        protected List<String> doInBackground(Void... params) {
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
                        "    Method:\"profile\",\n" +
                        "    Token:\""+tokenStr+"\",\n" +
                        "    Parameters:{\n" +
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
            Log.d("Profile", resultJson);

            JSONObject dataJsonObj = null;

            List<String> allStr= new <String>ArrayList();

            try {
                dataJsonObj = new JSONObject(resultJson);
                JSONObject jObj = dataJsonObj.getJSONObject("Result");
                allStr.add(jObj.getString("Id")); //0
                allStr.add(jObj.getString("Email")); //1
                allStr.add(jObj.getString("Username")); //2
                allStr.add(jObj.getString("FirstName")); //3
                allStr.add(jObj.getString("Surname"));//4
                allStr.add(jObj.getString("Phone"));//5
                allStr.add(jObj.getString("AvatarUrl"));//6
                allStr.add(jObj.getString("Address"));//7
                allStr.add(jObj.getString("Description"));//8

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return allStr;

        }

        @Override
        protected void onPostExecute(List<String> results) {
            mProfileTask = null;
            super.onPostExecute(results);
            CheckContentOfTextView(textViewName, results.get(3) + "  " + results.get(4));
            CheckContentOfTextView(textViewUserName, results.get(2));
            CheckContentOfTextView(textViewAddress, results.get(7));
            CheckContentOfTextView(textViewDescriprion, results.get(8));
            CheckContentOfTextView(textViewEmail, results.get(1));
            new ImageLoadTask(results.get(6), imageViewPortrait).execute();
        }

        private void CheckContentOfTextView (TextView txtView, String content)
        {
            if(content=="null") content="Вы не заполнили данное поле";
            txtView.setText(content);
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




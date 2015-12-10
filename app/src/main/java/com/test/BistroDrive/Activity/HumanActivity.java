package com.test.BistroDrive.Activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
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


public class HumanActivity extends Activity{


    private View mProgressView;
    private View mProfileFormView;
    private View mProfileFormScrollView;

    private ParserHuman mHumanTask = null;
    private ImageLoadTaskHuman mHumanImageTask = null;

    TextView textViewEmail;
    TextView textViewUserName;
    TextView textViewName;
    TextView textViewDescriprion;
    TextView textViewAddress;
    ImageView imageViewPortrait;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_human);


        mProgressView = findViewById(R.id.human_progress);
        mProfileFormView = findViewById(R.id.human);
        mProfileFormScrollView = findViewById(R.id.scrollHuman);

        textViewEmail = (TextView) findViewById(R.id.email_human);
        textViewUserName = (TextView) findViewById(R.id.name_human);
        textViewName = (TextView) findViewById(R.id.NameAndSurnameHuman);
        textViewDescriprion = (TextView) findViewById(R.id.humanDesc);
        textViewAddress = (TextView) findViewById(R.id.humanPlace);
        imageViewPortrait = (ImageView) findViewById(R.id.humanImg);

        showProgress(true);
        mHumanTask = new ParserHuman(getIntent().getStringExtra("token"),getIntent().getStringExtra("userName"));
        mHumanTask.execute();


    }

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

    class ParserHuman extends AsyncTask<Void, Void, List<String>> {

        String resultJson = "";
        String tokenStr="";
        String userName;
        public ParserHuman(String tok, String user)
        {
            this.tokenStr = tok;
            this.userName = user;
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
                        "        username:\""+userName+"\"\n" +
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
            mHumanTask = null;
            super.onPostExecute(results);
            CheckContentOfTextView(textViewName, results.get(3) + "  " + results.get(4));
            CheckContentOfTextView(textViewUserName, results.get(2));
            CheckContentOfTextView(textViewAddress, results.get(7));
            CheckContentOfTextView(textViewDescriprion, results.get(8));
            CheckContentOfTextView(textViewEmail, results.get(1));
            showProgress(false);
            mHumanImageTask = new ImageLoadTaskHuman(results.get(6), imageViewPortrait);
            mHumanImageTask.execute();
        }

        private void CheckContentOfTextView (TextView txtView, String content)
        {
            if(content=="null") content="Данное поле не заполнено";
            txtView.setText(content);
        }


    }

    public class ImageLoadTaskHuman extends AsyncTask<Void, Void, Bitmap> {
        @Override
        protected void onCancelled() {
            super.onCancelled();
            mHumanImageTask = null;
        }

        private String url;
        private ImageView imageView;

        public ImageLoadTaskHuman(String url, ImageView imageView) {
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
            mHumanImageTask = null;
            super.onPostExecute(result);
            imageView.setImageBitmap(result);
        }

    }
}

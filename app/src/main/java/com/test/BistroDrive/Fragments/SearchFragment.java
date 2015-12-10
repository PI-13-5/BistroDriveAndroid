package com.test.BistroDrive.Fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;


import com.test.BistroDrive.Activity.MainActivity;
import com.test.BistroDrive.NothingSelectedSpinnerAdapter;
import com.test.BistroDrive.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;


public class SearchFragment extends Fragment implements View.OnClickListener{

    private ParserCityAndCategory mCategoryCityTask = null;

    private List<String> lstCategory;
    private List<String> lstCity;

    private View mSearchProgress = null;
    private View mSearchFormView = null;

    private TextView text;
    private EditText min;
    private EditText max;
    private Spinner spinner;
    NothingSelectedSpinnerAdapter adapter;
    private Spinner spinner2;
    NothingSelectedSpinnerAdapter adapter2;
    private Button upButton;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSearch:
                    Long lCategory = spinner.getSelectedItemId() + 1;
                    String strCategory = lCategory.toString();
                    if (lCategory == 0) strCategory = "";
                    Long lCity = spinner2.getSelectedItemId() + 1;
                    String strCity = lCity.toString();
                    String strSearch = text.getText().toString();
                    String strMin = min.getText().toString();
                    String strMax = max.getText().toString();
                if(check(strMin,strMax)) {
                    ((MainActivity) getActivity()).callSearch(strSearch, strMin, strMax, strCategory, strCity);
                }
                break;
        }
    }

    private boolean check(String strMin,String strMax){
        View focusView;
        if(strMin.isEmpty()) {
            min.setError("Заполните значения");
            focusView = min;
            focusView.requestFocus();
            return false;}
        else if (strMax.isEmpty()){
            max.setError("Заполните значения");
            focusView = max;
            focusView.requestFocus();
            return false;
        }else if (Integer.parseInt(strMin)>Integer.parseInt(strMax)) {
            min.setError("Минимальное значение должно быть меньше максимального");
            focusView = min;
            focusView.requestFocus();
            return false;
        }
        return true;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);
        upButton = (Button) rootView.findViewById(R.id.btnSearch);
        upButton.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mCategoryCityTask!=null) {
            mCategoryCityTask.cancel(true);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        View v = getView();
        mSearchFormView = v.findViewById(R.id.searchFormView);
        mSearchProgress = v.findViewById(R.id.search_progress);
        spinner = (Spinner) v.findViewById(R.id.searchSpinnerCategory);
        spinner2 = (Spinner) v.findViewById(R.id.searchSpinnerCity);
        text = (TextView) v.findViewById(R.id.searchText);
        min = (EditText) v.findViewById(R.id.minSearchNum);
        max = (EditText) v.findViewById(R.id.maxSearchNum);

        showProgress(true);
        mCategoryCityTask = new ParserCityAndCategory(getArguments().getString("token"));
        mCategoryCityTask.execute();
}

    private void initSpinners(Spinner spin,NothingSelectedSpinnerAdapter adap,List<String> lst, int layout){
        String[] arr = lst.toArray(new String[lst.size()]);
        final ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(getActivity().getApplicationContext(), R.layout.spinner_selected_item, arr);
        adapter2.setDropDownViewResource(R.layout.spinner_item);
        spin.setPrompt("Выберите город");

        adap = new NothingSelectedSpinnerAdapter( adapter2,
                layout,
                // R.layout.contact_spinner_nothing_selected_dropdown, // Optional
                getActivity().getApplicationContext());
        spin.setAdapter(adap);

        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    public void showProgress(final boolean show) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mSearchFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mSearchFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mSearchFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });


            mSearchProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            mSearchProgress.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mSearchProgress.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mSearchProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            mSearchFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    class ParserCityAndCategory extends AsyncTask<Void, Void, List<List<String>>> {

        String resultJson1 = "";
        String resultJson2="";
        String tokenStr="";
        public ParserCityAndCategory(String tok)
        {
            this.tokenStr = tok;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mCategoryCityTask=null;
        }

        @Override
        protected List<List<String>> doInBackground(Void... params) {
            URL url;
            HttpURLConnection conn=null;
            List<List<String>> CategoryCity = new ArrayList<>();
            List<String> category= new ArrayList<>();
            List<String> city = new ArrayList<>();
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
                        "    Method:\"getcategorylist\",\n" +
                        "    Token:\""+tokenStr+"\"\n" +
                        "}");

                writer.flush();
                writer.close();
                os.close();
                int responseCode=conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line=br.readLine()) != null) {
                        resultJson1+=line;
                    }
                }
                else {
                    resultJson1="";
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                conn.disconnect();
            }

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
                        "    Method:\"getcitylist\",\n" +
                        "    Token:\"" + tokenStr + "\"\n" +
                        "}");

                writer.flush();
                writer.close();
                os.close();
                int responseCode=conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line=br.readLine()) != null) {
                        resultJson2+=line;
                    }
                }
                else {
                    resultJson2="";
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                conn.disconnect();
            }

            JSONObject dataJsonObj = null;

            try {
                dataJsonObj = new JSONObject(resultJson1);
                JSONObject jObj = dataJsonObj.getJSONObject("Result");
                for (int i = 1; i <= jObj.length(); i++) {
                    Integer intI = i;
                    category.add(jObj.getString(intI.toString()));
                }
                CategoryCity.add(category);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                dataJsonObj = new JSONObject(resultJson2);
                JSONObject jObj = dataJsonObj.getJSONObject("Result");
                for (int i = 1; i < jObj.length(); i++) {
                    Integer intI = i;
                    city.add(jObj.getString(intI.toString()));
                }
                CategoryCity.add(city);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return CategoryCity;

        }

        @Override
        protected void onPostExecute(List<List<String>> results) {
            mCategoryCityTask = null;
            lstCategory=results.get(0);
            lstCity=results.get(1);
            initSpinners(spinner,adapter,lstCategory,R.layout.contact_spinner_row_nothing_selected);
            initSpinners(spinner2,adapter2,lstCity,R.layout.contact_spinner_row_nothing_selected2);
            super.onPostExecute(results);
            showProgress(false);
        }

    }
}

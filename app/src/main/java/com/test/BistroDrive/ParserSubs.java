package com.test.BistroDrive;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


public  class ParserSubs {
    protected List<String> resultStr = new <String>ArrayList();

    public List<String> getResult(String idName){
        ParseTask pt = new ParseTask(idName);
        pt.execute();
        try{
            resultStr = pt.get();
        }catch(InterruptedException e){
            e.printStackTrace();
        }catch(ExecutionException e)
        {
            e.printStackTrace();
        }
        return resultStr;
    }

    private class ParseTask extends AsyncTask<Void, Void, List<String>> {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";
        String idVK = null;

        public ParseTask(String name){
            this.idVK = name;
        }

        @Override
        protected List<String> doInBackground(Void... params) {
            // получаем данные с внешнего ресурса
            try {
                URL url = new URL("https://api.vk.com/method/users.getSubscriptions?user_id="+idVK+"&v=5.37&extended=1");

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                resultJson = buffer.toString();

            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                urlConnection.disconnect();
            }

            JSONObject dataJsonObj = null;
            List<String> allStr= new <String>ArrayList();

            try {
                dataJsonObj = new JSONObject(resultJson);
                JSONObject jObj = dataJsonObj.getJSONObject("response");
                JSONArray publics = jObj.getJSONArray("items");


                for (int i = 0; i < publics.length(); i++) {
                    JSONObject pub = publics.getJSONObject(i);

                    String name = pub.getString("name");
                    allStr.add(name);

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return allStr;
        }

        @Override
        protected void onPostExecute(List<String> results) {

            super.onPostExecute(results);
        }
    }
}

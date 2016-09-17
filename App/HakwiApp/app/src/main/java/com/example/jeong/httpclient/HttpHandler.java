package com.example.jeong.httpclient;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Jeong on 2016-09-04.
 */
public class HttpHandler extends AsyncTask<String, Void, String>{

    private static final String TAG = HttpHandler.class.getSimpleName();
    TextView textView;
    View rootView; //MainActivity

    public HttpHandler() {}
    public HttpHandler(View rootView){
        this.rootView = rootView;
    }

    @Override
    protected String doInBackground(String... params) {
        URL url ;
        String response = null;
        try {
            url = new URL(params[0]);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);//응답 헤더와 메시지를 읽어들이겠다
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-type", "application/json; charset=utf-8");

            OutputStreamWriter os = new OutputStreamWriter(conn.getOutputStream());
            os.write(makeJson().toString());
            os.flush();

            InputStream in = new BufferedInputStream(conn.getInputStream());
            response = convertStreamToString(in);

//            conn.connect();
//            response = conn.getResponseMessage();
//            Log.d("RESPONSE", "The response is: " + response);

            //read response
            StringBuilder sb = new StringBuilder();
            int HttpResult = conn.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), "utf-8"));
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                System.out.println("" + sb.toString());
            } else {
                Log.d("TAG", "HttpResult error!" );
            }

        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        }

        return response;
    }

    public String makeServiceCall(String reqUrl) {
        String response = null;
        try {
            URL url = new URL(reqUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            response = convertStreamToString(in);
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
        return response;
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        try {
            String line;
            while ((line = reader.readLine()) != null) {
                // TODO: 2016. 9. 13. 왜 라인 맨앞에 " 하나 더드가는가 
                sb.append(line +'\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        sb = sb.deleteCharAt(0);

        return sb.toString();
    }

    public JSONObject makeJson(){
        JSONObject jsonObj = new JSONObject();
        try {

            jsonObj.put("bid", "0228777");
            jsonObj.put("name", "socc_building");
            jsonObj.put("longitude", "123");
            jsonObj.put("latitude", "456");

        } catch (JSONException e1) {
            e1.printStackTrace();
        }

        return jsonObj;
    }

    /*
    @Override
    protected String doInBackground(String... params) {
        String url = params[0];
        String method = params[1];
        HttpResponse response = null;

        StringBuilder sb = new StringBuilder();

        DefaultHttpClient client = new DefaultHttpClient();

        if(method.equals("POST")){
            HttpPost post = new HttpPost(url);
            HttpParams http_params = client.getParams();
            HttpConnectionParams.setConnectionTimeout(http_params, 3000);
            HttpConnectionParams.setSoTimeout(http_params, 3000);
            post.setHeader("Content-type", "application/json; charset=utf-8");

            JSONObject jsonObject = makeJson();
            StringEntity str_entity = null;
            try {
                str_entity = new StringEntity(jsonObject.toString());

                HttpEntity http_entity = str_entity;
                post.setEntity(http_entity);
                response = client.execute(post);//post
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        else if(method.equals("GET")){
            HttpGet get = new HttpGet(url);
            try {
                response = client.execute(get);//post
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //read data
        BufferedReader bufReader = null;
        String line = null;
        String result = "";

        try {
            bufReader = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent(), "utf-8")
            );

            while ((line = bufReader.readLine()) != null) {
                sb.append(line + "\n");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    @Override
    protected void onPostExecute(String res_str) {

        // TODO: 2016. 9. 10. 여기에 제이선 파싱해서 리스트 어댑터씌워서 출력해야된다
        Json layer = new Json();	// JSON 파일을 다룰 객체
        JSONObject jo = null;
        //JSONArray dataArray = null;
        List<Marker> markers = null;

        try {
            System.out.println(res_str);
            JSONObject root = new JSONObject(res_str);
            System.out.println(root);
            markers = layer.load(root, DataSource.DATAFORMAT.BuildingInfo);
            // TODO: 2016. 9. 11. 원래 markers에 위치정보들이 들어가 있고 그걸 포문 돌려서 출력해야됨 이쁘게 만들려면 어댑터를 넣어야될것같음
            // TODO: 2016. 9. 11. 서버에서 제이썬형태로 보내게 처리를 해야됨


        } catch (JSONException e) {
            e.printStackTrace();
        }

        textView = (TextView) rootView.findViewById(R.id.textView);
        for(int i = 0 ; i < markers.size() ; i++)
            System.out.println(markers.get(i).getBuildId());

        textView.setText(res_str);

        //Log.d("---onPostExecute---: ", res_str);
    }

    public JSONObject makeJson(){
        JSONObject jsonObj = new JSONObject();
        try {

            jsonObj.put("bid", "0228777");
            jsonObj.put("name", "socc_building");
            jsonObj.put("longitude", "123");
            jsonObj.put("latitude", "456");

        } catch (JSONException e1) {
            e1.printStackTrace();
        }

        return jsonObj;
    }*/

}
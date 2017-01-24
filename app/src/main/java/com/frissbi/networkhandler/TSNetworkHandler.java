package com.frissbi.networkhandler;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

/**
 * Created by thrymr on 21/4/16.
 */
public class TSNetworkHandler {

    public static TSNetworkHandler instance;
    private Context mContext;


    public static final int TYPE_POST = 1;
    public static final int TYPE_GET = 2;

    private TSNetworkHandler(Context context) {

        mContext = context;
    }

    public interface ResponseHandler {
        public void handleResponse(TSResponse response);
    }


    public static TSNetworkHandler getInstance(Context context) {

        if (instance == null)
            instance = new TSNetworkHandler(context);

        return instance;

    }

    public void getResponse(String _url, HashMap<String, String> params,
                            int type, ResponseHandler handler) {

        if (type == TYPE_POST) {
            new RequestAsyncTask(_url, params, handler).execute();
        } else if (type == TYPE_GET) {
            new RequestAsyncGet(_url, params, handler).execute();
        }

    }

    public void getResponse(String _url, JSONObject json, ResponseHandler handler) {

        Log.d("TAG", "sending json request " + json.toString());


        new JsonRequestAsyncTask(_url, json, handler).execute();

    }


    public void getResponse(String _url, JSONObject json, String type, ResponseHandler handler) {

        Log.d("TAG", "sending json request " + json.toString());


        new JsonRequestGetAsyncTask(_url, json, handler).execute();

    }


    public void getResponse(String _url, String paramString, ResponseHandler handler) {

        //Log.d("TAG", "sending json request " + paramString);
        new XmlRequestAsyncTask(_url, paramString, handler).execute();

    }

    public class TSResponse {

        public static final int STATUS_SUCCESS = 100;
        public static final int STATUS_FAIL = -100;

        public int status;
        public String response;
        public String message;
        protected String ok = null;

        public TSResponse(int status, String response, String message) {
            this.status = status;
            this.response = response;
            this.message = message;
        }

    }

    private class RequestAsyncGet extends AsyncTask<Void, Void, TSResponse> {

        String _url;
        HashMap<String, String> inputParams;
        ResponseHandler handler;

        public RequestAsyncGet(String _url, HashMap<String, String> params,
                               ResponseHandler handler) {
            // TODO Auto-generated constructor stub
            this._url = _url;
            if (params != null)
                this.inputParams = params;
            else
                this.inputParams = new HashMap<String, String>();
            this.handler = handler;
        }

        @Override
        protected void onPostExecute(TSResponse tsResponse) {
            super.onPostExecute(tsResponse);
            Log.d("TSNetworkHandler", "tsResponse" + tsResponse);
            handler.handleResponse(tsResponse);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected TSResponse doInBackground(Void... params) {
            URL url;
            CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
            StringBuilder builder = new StringBuilder();

            String[] keys = new String[inputParams.size()];
            inputParams.keySet().toArray(keys);

            try {
                builder.append(_url);
                builder.append("?");

                for (int i = 0; i < keys.length; i++) {

                    /*builder.append(keys[i]
                            + "="
                            + URLEncoder.encode(inputParams.get(keys[i]),
                            "UTF-8"));
                    if (i < inputParams.size() - 1)
                        builder.append("&");*/

                    builder.append(keys[i] + "=" + inputParams.get(keys[i]));
                    if (i < inputParams.size() - 1) {
                        builder.append("&");
                    }
                }

                Log.d("TAG", "url : " + builder.toString());
                url = new URL(builder.toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                int responseCode = conn.getResponseCode();
                StringBuffer response = null;
                System.out.println("GET Response Code :: " + responseCode);
                if (responseCode == HttpURLConnection.HTTP_OK) { // success
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            conn.getInputStream()));
                    String inputLine;
                    response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // print result
                    System.out.println(response.toString());
                } else {
                    System.out.println("GET request not worked");
                }


               /* if (response != null) {
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response.toString());
                        if (jsonObject.has("ok")) {
                            return new TSResponse(TSResponse.STATUS_SUCCESS, jsonObject.get("ok").toString(), "");
                        } else if (jsonObject.has("error")) {
                            JSONObject error = jsonObject.getJSONObject("error");
                            return new TSResponse(TSResponse.STATUS_FAIL, "", error.get("message").toString());
                        } else {
                            return new TSResponse(TSResponse.STATUS_FAIL, "", "Something went wrong. Please check your network connection and try again.");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return new TSResponse(TSResponse.STATUS_FAIL, "", "Something went wrong. Please check your network connection and try again.");
                    }
                }*/
                if (response != null) {

                    JSONObject jsonObject = new JSONObject(response.toString());

                    if (jsonObject.getBoolean("status")) {
                        return new TSResponse(TSResponse.STATUS_SUCCESS, response.toString(), "");
                    } else {
                        return new TSResponse(TSResponse.STATUS_FAIL, jsonObject.getString("message"), "");
                    }


                }


            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

    }

    private class RequestAsyncTask extends AsyncTask<Void, Void, TSResponse> {

        String _url;
        HashMap<String, String> inputParams;
        ResponseHandler handler;

        public RequestAsyncTask(String _url, HashMap<String, String> params,
                                ResponseHandler handler) {
            // TODO Auto-generated constructor stub
            this._url = _url;
            if (params != null)
                this.inputParams = params;
            else
                this.inputParams = new HashMap<String, String>();

            this.handler = handler;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

        }

        @Override
        protected TSResponse doInBackground(Void... params) {
            // TODO Auto-generated method stub
            try {

                URL url = new URL(_url);
                StringBuilder builder = new StringBuilder();

                String[] keys = new String[inputParams.size()];
                inputParams.keySet().toArray(keys);

                try {

                    for (int i = 0; i < keys.length; i++) {

                        builder.append(keys[i]
                                + "="
                                + URLEncoder.encode(inputParams.get(keys[i]),
                                "UTF-8"));
                        if (i < inputParams.size() - 1)
                            builder.append("&");

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");

                conn.setFixedLengthStreamingMode(builder.toString().getBytes().length);
                conn.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");


                PrintWriter pw = new PrintWriter(conn.getOutputStream());
                pw.print(builder.toString());
                pw.close();

                conn.connect();

                // Response
                StringBuilder responseBuilder = new StringBuilder();
                InputStream is = conn.getInputStream();

                BufferedReader r = new BufferedReader(new InputStreamReader(is));

                String line;
                while ((line = r.readLine()) != null) {
                    responseBuilder.append(line);
                }

                Log.d("TSNetworkHandler", "responseBuilder" + responseBuilder);
                return new TSResponse(TSResponse.STATUS_SUCCESS, responseBuilder.toString(), "");
                /*JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(responseBuilder.toString());
                    if (jsonObject.has("ok")) {
                        return new TSResponse(TSResponse.STATUS_SUCCESS, jsonObject.get("ok").toString(), "");
                    } else if (jsonObject.has("error")) {
                        JSONObject error = jsonObject.getJSONObject("error");
                        return new TSResponse(TSResponse.STATUS_FAIL, "", error.get("message").toString());
                    } else {
                        return new TSResponse(TSResponse.STATUS_FAIL, "", "Something went wrong. Please check your network connection and try again.");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    return new TSResponse(TSResponse.STATUS_FAIL, "", "Something went wrong. Please check your network connection and try again.");
                }*/


            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return new TSResponse(TSResponse.STATUS_FAIL, "", "Something went wrong. Please check your network connection and try again.");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return new TSResponse(TSResponse.STATUS_FAIL, "", "Something went wrong. Please check your network connection and try again.");
            }

        }

        @Override
        protected void onPostExecute(TSResponse result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            handler.handleResponse(result);

        }

    }

    private class JsonRequestAsyncTask extends AsyncTask<Void, Void, TSResponse> {

        String _url;
        JSONObject paramsJson;
        ResponseHandler handler;

        public JsonRequestAsyncTask(String _url, JSONObject json, ResponseHandler handler) {

            this._url = _url;
            this.paramsJson = json;
            this.handler = handler;

        }

        @Override
        protected TSResponse doInBackground(Void... params) {

            try {

                URL url = new URL(_url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type",
                        "application/json");


                PrintWriter pw = new PrintWriter(conn.getOutputStream());
                pw.print(paramsJson.toString());
                pw.close();

                conn.connect();

                // Response
                StringBuilder responseBuilder = new StringBuilder();
                InputStream is = conn.getInputStream();

                BufferedReader r = new BufferedReader(new InputStreamReader(is));

                String line;
                while ((line = r.readLine()) != null) {
                    responseBuilder.append(line);
                }

                if (responseBuilder != null) {

                    JSONObject jsonObject = new JSONObject(responseBuilder.toString());

                    if (jsonObject.getBoolean("status")) {
                        return new TSResponse(TSResponse.STATUS_SUCCESS, responseBuilder.toString(), "");
                    } else {
                        return new TSResponse(TSResponse.STATUS_FAIL, jsonObject.getString("message"), "");
                    }


                }

                return new TSResponse(TSResponse.STATUS_SUCCESS, responseBuilder.toString(), "");
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return new TSResponse(TSResponse.STATUS_FAIL, "", "Error while connecting to server");

        }

        @Override
        protected void onPostExecute(TSResponse result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            Log.d("TAG", "response : " + result.response);
            handler.handleResponse(result);

        }
    }


    private class JsonRequestGetAsyncTask extends AsyncTask<Void, Void, TSResponse> {

        String _url;
        JSONObject paramsJson;
        ResponseHandler handler;

        public JsonRequestGetAsyncTask(String _url, JSONObject json, ResponseHandler handler) {
            this._url = _url;
            this.paramsJson = json;
            this.handler = handler;

        }

        @Override
        protected TSResponse doInBackground(Void... params) {

            try {

                URL url = new URL(_url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type",
                        "application/json");


                /*PrintWriter pw = new PrintWriter(conn.getOutputStream());
                pw.print(paramsJson.toString());
                pw.close();*/

                conn.connect();

                // Response
                StringBuilder responseBuilder = new StringBuilder();
                InputStream is = conn.getInputStream();

                BufferedReader r = new BufferedReader(new InputStreamReader(is));

                String line;
                while ((line = r.readLine()) != null) {
                    responseBuilder.append(line);
                }

                return new TSResponse(TSResponse.STATUS_SUCCESS, responseBuilder.toString(), "");
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return new TSResponse(TSResponse.STATUS_FAIL, "", "Error while connecting to server");

        }

        @Override
        protected void onPostExecute(TSResponse result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            Log.d("TAG", "response : " + result.response);
            handler.handleResponse(result);

        }
    }


    private class XmlRequestAsyncTask extends AsyncTask<Void, Void, TSResponse> {

        String _url;
        String paramsJson;
        ResponseHandler handler;

        public XmlRequestAsyncTask(String _url, String json, ResponseHandler handler) {

            this._url = _url;
            this.paramsJson = json;
            this.handler = handler;

        }

        @Override
        protected TSResponse doInBackground(Void... params) {

            try {

                URL url = new URL(_url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/xml");

                PrintWriter pw = new PrintWriter(conn.getOutputStream());
                pw.print(paramsJson);
                pw.close();

                conn.connect();

                // Response
                StringBuilder responseBuilder = new StringBuilder();
                InputStream is = conn.getInputStream();

                BufferedReader r = new BufferedReader(new InputStreamReader(is));

                String line;
                while ((line = r.readLine()) != null) {
                    responseBuilder.append(line);
                }

                return new TSResponse(TSResponse.STATUS_SUCCESS, responseBuilder.toString(), "");
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return new TSResponse(TSResponse.STATUS_FAIL, "", "Error while connecting to server");

        }

        @Override
        protected void onPostExecute(TSResponse result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            handler.handleResponse(result);

        }
    }


}

package com.wm.instawebmob.instagram;

import android.os.AsyncTask;
import android.util.Log;

import com.wm.instawebmob.utils.Constants;
import com.wm.instawebmob.utils.Utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class InstagramRequest {
    private String mAccessToken;

    public InstagramRequest() {
        mAccessToken = "";
    }

    public InstagramRequest(String accessToken) {
        mAccessToken = accessToken;
    }

    public String createRequest(String method, String endpoint, List<NameValuePair> params) throws Exception {
        if (method.equals("POST")) {
            return requestPost(endpoint, params);
        } else {
            return requestGet(endpoint, params);
        }
    }

    public void createRequest(String method, String endpoint, List<NameValuePair> params, InstagramRequestListener listener) {
        new RequestTask(method, endpoint, params, listener).execute();
    }

    private String requestGet(String endpoint, List<NameValuePair> params) throws Exception {
        String requestUri = Constants.API_BASE_URL + ((endpoint.indexOf("/") == 0) ? endpoint : "/" + endpoint);

        return get(requestUri, params);
    }

    private String requestPost(String endpoint, List<NameValuePair> params) throws Exception {
        String requestUri = Constants.API_BASE_URL + ((endpoint.indexOf("/") == 0) ? endpoint : "/" + endpoint);

        return post(requestUri, params);
    }

    public String get(String requestUri, List<NameValuePair> params) throws Exception {
        InputStream stream = null;
        String response = "";

        try {
            String requestUrl = requestUri;

            if (!mAccessToken.equals("")) {
                if (params == null) {
                    params = new ArrayList<>(1);

                    params.add(new BasicNameValuePair("access_token", mAccessToken));
                } else {
                    params.add(new BasicNameValuePair("access_token", mAccessToken));
                }
            }

            Log.e("PARAMS", "Params are->" + params);
            if (params != null) {
                StringBuilder requestParamSb = new StringBuilder();
                int size = params.size();

                for (int i = 0; i < size; i++) {
                    BasicNameValuePair param = (BasicNameValuePair) params.get(i);

                    requestParamSb.append(param.getName() + "=" + param.getValue() + ((i != size - 1) ? "&" : ""));
                }

                String requestParam = requestParamSb.toString();

                requestUrl = requestUri + ((requestUri.contains("?")) ? "&" + requestParam : "?" + requestParam);
            }

            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(requestUrl);

            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();

            if (httpEntity == null) {
                throw new Exception("Request returns empty result");
            }

            stream = httpEntity.getContent();
            response = Utils.streamToString(stream);

            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new Exception(httpResponse.getStatusLine().getReasonPhrase());
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (stream != null) {
                stream.close();
            }
        }

        return response;
    }

    public String post(String requestUrl, List<NameValuePair> params) throws Exception {
        InputStream stream;
        String response = "";

        try {
            if (!mAccessToken.equals("")) {
                if (params == null) {
                    params = new ArrayList<>(1);

                    params.add(new BasicNameValuePair("access_token", mAccessToken));
                } else {
                    params.add(new BasicNameValuePair("access_token", mAccessToken));
                }
            }

            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(requestUrl);

            httpPost.setEntity(new UrlEncodedFormEntity(params));

            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();

            if (httpEntity == null) {
                throw new Exception("Request returns empty result");
            }

            stream = httpEntity.getContent();
            response = Utils.streamToString(stream);

            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new Exception(httpResponse.getStatusLine().getReasonPhrase());
            }
        } catch (Exception e) {
            throw e;
        }

        return response;
    }

    //Request listener
    public interface InstagramRequestListener {
        void onSuccess(String response);

        void onError(String error);
    }

    private class RequestTask extends AsyncTask<URL, Integer, Long> {
        String method, endpoint, response = "";

        List<NameValuePair> params;

        InstagramRequestListener listener;

        public RequestTask(String method, String endpoint, List<NameValuePair> params, InstagramRequestListener listener) {
            this.method = method;
            this.endpoint = endpoint;
            this.params = params;
            this.listener = listener;
        }

        protected void onCancelled() {
        }

        protected void onPreExecute() {
        }

        protected Long doInBackground(URL... urls) {
            long result = 0;

            try {
                if (method.equals("POST")) {
                    response = requestPost(endpoint, params);
                } else {
                    response = requestGet(endpoint, params);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return result;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(Long result) {
            if (!response.equals("")) {
                listener.onSuccess(response);
            } else {
                listener.onError("Failed to process api request");
            }
        }
    }
}
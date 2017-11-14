package com.wm.instawebmob.instagram;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.wm.instawebmob.utils.Constants;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Instagram {
    private Context mContext;

    private InstagramDialog mDialog;
    private InstagramAuthListener mListener;
    private InstagramSession mSession;

    private String mClientId;
    private String mClientSecret;
    private String mRedirectUri;

    public Instagram(Context context, String clientId, String clientSecret, String redirectUri) {
        mContext = context;

        mClientId = clientId;
        mClientSecret = clientSecret;
        mRedirectUri = redirectUri;

        String authUrl = Constants.AUTH_URL + "client_id=" + mClientId + "&redirect_uri=" + mRedirectUri + "&response_type=code";

        mSession = new InstagramSession(context);

        mDialog = new InstagramDialog(context, authUrl, redirectUri, new InstagramDialog.InstagramDialogListener() {

            @Override
            public void onSuccess(String code) {
                retreiveAccessToken(code);
            }

            @Override
            public void onError(String error) {
                mListener.onError(error);
            }

            @Override
            public void onCancel() {
                mListener.onCancel();

            }
        });
    }

    public void authorize(InstagramAuthListener listener) {
        mListener = listener;

        mDialog.show();
    }

    public void resetSession() {
        mSession.reset();

        mDialog.clearCache();
    }

    public InstagramSession getSession() {
        return mSession;
    }

    private void retreiveAccessToken(String code) {
        new AccessTokenTask(code).execute();
    }

    public interface InstagramAuthListener {
        void onSuccess(InstagramUser user);

        void onError(String error);

        void onCancel();
    }

    public class AccessTokenTask extends AsyncTask<URL, Integer, Long> {
        ProgressDialog progressDlg;
        InstagramUser user;
        String code;

        public AccessTokenTask(String code) {
            this.code = code;

            progressDlg = new ProgressDialog(mContext);

            progressDlg.setMessage("Getting access token...");
        }

        protected void onCancelled() {
            progressDlg.cancel();
        }

        protected void onPreExecute() {
            progressDlg.show();
        }

        protected Long doInBackground(URL... urls) {
            long result = 0;

            try {
                List<NameValuePair> params = new ArrayList<NameValuePair>(5);

                params.add(new BasicNameValuePair("client_id", mClientId));
                params.add(new BasicNameValuePair("client_secret", mClientSecret));
                params.add(new BasicNameValuePair("grant_type", "authorization_code"));
                params.add(new BasicNameValuePair("redirect_uri", mRedirectUri));
                params.add(new BasicNameValuePair("code", code));

                InstagramRequest request = new InstagramRequest();
                String response = request.post(Constants.ACCESS_TOKEN_URL, params);

                if (!response.equals("")) {
                    JSONObject jsonObj = (JSONObject) new JSONTokener(response).nextValue();
                    JSONObject jsonUser = jsonObj.getJSONObject("user");

                    user = new InstagramUser();

                    user.accessToken = jsonObj.getString("access_token");

                    user.id = jsonUser.getString("id");
                    user.username = jsonUser.getString("username");
                    user.fullName = jsonUser.getString("full_name");
                    user.profilPicture = jsonUser.getString("profile_picture");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return result;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(Long result) {
            progressDlg.dismiss();

            if (user != null) {
                mSession.store(user);

                mListener.onSuccess(user);
            } else {
                mListener.onError("Failed to get access token");
            }
        }
    }
}
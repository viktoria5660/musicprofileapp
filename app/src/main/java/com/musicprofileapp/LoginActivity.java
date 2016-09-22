package com.musicprofileapp;
import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import java.util.HashMap;
import java.util.Map;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;

import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends FragmentActivity implements GoogleApiClient.OnConnectionFailedListener {

    // ===========================================================
    // Constants
    // ===========================================================
    //calback id
    private static RequestQueue mRequestQueue;

    private static final int RC_SIGN_IN = 1;
    private static final int REQUEST_AUTHORIZATION = 2;

    private static final int LOGIN_TYPE_CONSUMER = 0;
    private static final int LOGIN_TYPE_PRODUCER = 1;

    // ===========================================================
    // Fields
    // ===========================================================

    private CallbackManager mCallbackManager;
    private GoogleApiClient mGoogleApiClient;
    private String mGoogleAccount;
    private String mToken;
    private GoogleSignInAccount mGoogleSignInAccount;
    private int mLoginType;

    // ===========================================================
    // Activity methods
    // ===========================================================

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_login);

        findViewById(R.id.facebook).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginWithFacebook();
            }
        });
        findViewById(R.id.google).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        initFacebook();
        initGoglePlus();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (RC_SIGN_IN == requestCode) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        } else if (REQUEST_AUTHORIZATION == requestCode) {
            getGoogleToken();
        } else {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    // ===========================================================
    // GoogleApiClient.OnConnectionFailedListener methods
    // ===========================================================

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    // ===========================================================
    // Private methods
    // ===========================================================

    private void initGoglePlus() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .requestScopes(new Scope("https://www.googleapis.com/auth/youtube.readonly"),
                        new Scope("https://www.googleapis.com/auth/youtubepartner"))
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        SignInButton signInButton = (SignInButton) findViewById(R.id.google);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setScopes(gso.getScopeArray());
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        SignInButton signInProButton = (SignInButton) findViewById(R.id.google_producer);
        signInProButton.setSize(SignInButton.SIZE_STANDARD);
        signInProButton.setScopes(gso.getScopeArray());
        signInProButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLoginType = LOGIN_TYPE_PRODUCER;
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
    }

    private void initFacebook() {

        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.musicprofileapp",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }

        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                try {
                    mToken = loginResult.getAccessToken().getToken();
                    fetchFacebookUserDetails();
                } catch (Exception e) {
                    Toast.makeText(LoginActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException e) {
                Toast.makeText(LoginActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchFacebookUserDetails() {
        GraphRequest request = new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        String userInfo = null;
                        if (null != response) {
                            userInfo = response.getJSONObject().toString();
                        }
                        onLoginDone(mToken, userInfo, "facebook");
                    }
                }
        );
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,first_name,last_name,email,picture,birthday,location");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void loginWithFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(this,
                Arrays.asList("public_profile", "email", "user_likes", "user_birthday",
                        "user_actions.music", "user_location"));
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            mGoogleSignInAccount = result.getSignInAccount();
            mGoogleAccount = mGoogleSignInAccount.getEmail();
            getGoogleToken();
        } else {
            Toast.makeText(LoginActivity.this, "Error", Toast.LENGTH_SHORT).show();
        }
    }

    private void getGoogleToken() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String token = GoogleAuthUtil.getToken(
                            getApplicationContext(),
                            mGoogleAccount,
                            "oauth2:"
                                    + Scopes.PLUS_LOGIN + " "
                                    + Scopes.PLUS_ME +" https://www.googleapis.com/auth/youtube.readonly https://www.googleapis.com/auth/youtubepartner");
                    if (TextUtils.isEmpty(token)) {

                    } else {
                        mToken = token;
                        JSONObject json = new JSONObject();
                        try {
                            json.put("email", mGoogleSignInAccount.getEmail());
                            json.put("name", mGoogleSignInAccount.getDisplayName());
                            json.put("id", mGoogleSignInAccount.getId());
                            json.put("photo", mGoogleSignInAccount.getPhotoUrl());
                        } catch (Exception e) {}
                        onLoginDone(token, json.toString(), "google");
                    }

                } catch (com.google.android.gms.auth.UserRecoverableAuthException e1) {
                    startActivityForResult(e1.getIntent(), REQUEST_AUTHORIZATION);
                } catch (Exception e) { Log.e("error", e.toString()); }
            }
        }).start();
    }

    private void onLoginDone(String token, String userInfo, String platform ) {
        //userinfo = json with all the data
        //Toast.makeText(this, token, Toast.LENGTH_SHORT).show();

        SharedPreferences pref = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        pref.edit().putString("token", token).commit();
        if (null != userInfo) {
            pref.edit().putString("user", userInfo).commit();
        }
        pref.edit().putInt("login_type", mLoginType).commit();

        //send to server user info
        String strURL = "http://192.168.1.26:3000/enterfromMobile";
        Log.d("user",userInfo);
        final String final_token = token;
        final String final_userInfo = userInfo;
        final String final_type =  String.valueOf(mLoginType);
        final String final_platform = platform;

        StringRequest MyStringRequest = new StringRequest(com.android.volley.Request.Method.POST, strURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
               // response = response.substring(1, response.length()-1);
                Log.d("userid",response );
                Intent myIntent = new Intent(LoginActivity.this, MainActivity.class);
                myIntent.putExtra("id", response);
                Log.d("response", response);
                startActivity(myIntent);
                finish();
                //This code is executed if the server responds, whether or not the response contains data.
                //The String 'response' contains the server's response.
            }
        }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
            @Override
            public void onErrorResponse(VolleyError error) {
                //This code is executed if there is an error.
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<String, String>();
                MyData.put("token", final_token); //Add the data you'd like to send to the server.
                MyData.put("user", final_userInfo);
                MyData.put("type", final_type);
                MyData.put("platform", final_platform);
                return MyData;
            }
        };



    mRequestQueue.add(MyStringRequest);



//        String ans = TalkToServer.PostToUrl(strURL, userInfo);
//        Toast.makeText(this, ans, Toast.LENGTH_SHORT).show();

    }
}
package com.musicprofileapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.json.JSONObject;


public class MainActivity extends Activity {

    // ===========================================================
    // Constants
    // ===========================================================

    //private  String WEBAPP_URL = "http://themusicprofile.com";
    private String userId;

    private int group1Id = 1;

    int homeId = Menu.FIRST;

    // ===========================================================
    // Activity methods
    // ===========================================================

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);

        Bundle extras = getIntent().getExtras();

        SharedPreferences pref = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        String token = pref.getString("token", null); // if token is empty we need to start the login activity
        //clear the token: getSharedPreferences(getPackageName(), Context.MODE_PRIVATE).edit().putString("token", "").commit();
        if (TextUtils.isEmpty(token)) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
            return;
        }

        String userInfo = pref.getString("user", null);
        try {
            JSONObject json = new JSONObject(userInfo);
            String email = json.optString("email");
        } catch (Exception e) {}

        WebView webView = (WebView)findViewById(R.id.webview);
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.setWebChromeClient(new WebChromeClient());
        String url = "http://www.google.com";
        if(extras != null) {
            userId = extras.getString("id");
        }
        String URL = "http://192.168.1.26:3000/Mobile?userId=" + userId;
        Log.d("URL",URL);
        webView.loadUrl(URL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Logout");

        return super.onCreateOptionsMenu(menu);
    }

    //git test
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("id", String.valueOf(item.getItemId()));
        switch (item.getItemId()) {
            case 0:
                //clear my data
                SharedPreferences prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit().clear();
                editor.apply();
                Intent myIntent = new Intent(MainActivity.this, LoginActivity.class);
                MainActivity.this.startActivity(myIntent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
package com.gianlu.playconsole.apidemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.gianlu.playconsole.api.Models.SessionInfo;
import com.gianlu.playconsole.api.PlayConsoleAuthenticator;
import com.gianlu.playconsole.api.PlayConsoleWebView;
import com.gianlu.playconsole.api.Prefs;

public class MainActivity extends AppCompatActivity implements PlayConsoleAuthenticator.IWebViewAuth, PlayConsoleAuthenticator.ISilentAuth {
    private PlayConsoleWebView webView;
    private ProgressBar loading;
    private View success;
    private Button test;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = (PlayConsoleWebView) findViewById(R.id.main_webView);
        loading = (ProgressBar) findViewById(R.id.main_loading);
        success = findViewById(R.id.main_success);
        test = (Button) findViewById(R.id.main_test);

        // PlayConsoleAuthenticator.authenticateWithWebView(webView, this);
        PlayConsoleAuthenticator.authenticateSilently(this, this);
    }

    @Override
    public void onAuthenticated(final SessionInfo auth) {
        webView.setVisibility(View.GONE);
        loading.setVisibility(View.GONE);
        success.setVisibility(View.VISIBLE);

        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TestActivity.startActivity(MainActivity.this, auth);
            }
        });

        Prefs.putString(this, Prefs.Keys.LAST_DEV_ACC, auth.startupData.account.accountCode);
    }

    @Override
    public void notAuthenticated() {
        webView.setVisibility(View.VISIBLE);
        loading.setVisibility(View.GONE);

        PlayConsoleAuthenticator.authenticateWithWebView(webView, this);
    }

    @Override
    public void userAttentionNoMoreRequired() {
        webView.setVisibility(View.GONE);
        loading.setVisibility(View.VISIBLE);
    }

    @Override
    public void userAttentionRequired() {
        webView.setVisibility(View.VISIBLE);
        loading.setVisibility(View.GONE);
    }

    @Override
    public void onFailedAuthenticating(Exception ex) {
        if (BuildConfig.DEBUG) ex.printStackTrace();
        Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
    }
}

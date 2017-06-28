package com.gianlu.playconsole.apidemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.gianlu.playconsole.api.PlayConsoleWebView;
import com.gianlu.playconsole.api.Models.SessionInfo;

public class MainActivity extends AppCompatActivity implements PlayConsoleWebView.ISessionAuthenticator {
    private PlayConsoleWebView webView;
    private ProgressBar loading;
    private View success;
    private Button test;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = (PlayConsoleWebView) findViewById(R.id.main_webView);
        webView.askForSessionAuthenticator(this);

        loading = (ProgressBar) findViewById(R.id.main_loading);
        success = findViewById(R.id.main_success);
        test = (Button) findViewById(R.id.main_test);
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
    }

    @Override
    public void userAttentionNoMoreRequired() {
        webView.setVisibility(View.GONE);
        loading.setVisibility(View.VISIBLE);
    }

    @Override
    public void onFailedAuthenticating(Exception ex) {
        if (BuildConfig.DEBUG) ex.printStackTrace();
        Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
    }
}

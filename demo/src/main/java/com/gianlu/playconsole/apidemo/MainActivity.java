package com.gianlu.playconsole.apidemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.gianlu.playconsole.api.PlayConsoleWebView;
import com.gianlu.playconsole.api.SessionAuthenticator;

public class MainActivity extends AppCompatActivity implements PlayConsoleWebView.ISessionAuthenticator {
    private PlayConsoleWebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = (PlayConsoleWebView) findViewById(R.id.main_webView);
        webView.askForSessionAuthenticator(this);
    }

    @Override
    public void onAuthenticated(SessionAuthenticator auth) {
        System.out.println(auth);
    }

    @Override
    public void userAttentionNoMoreRequired() {
        if (webView != null) webView.setVisibility(View.GONE);
        // TODO: Hide webView and show indeterminate ProgressBar
    }

    @Override
    public void onFailedAuthenticating(Exception ex) {
        if (BuildConfig.DEBUG) ex.printStackTrace();
        Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
    }
}

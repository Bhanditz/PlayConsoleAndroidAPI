package com.gianlu.playconsole.apidemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gianlu.playconsole.api.GeneralException;
import com.gianlu.playconsole.api.PlayConsoleRequester;
import com.gianlu.playconsole.api.SessionAuthenticator;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class TestActivity extends AppCompatActivity {

    public static void startActivity(Context context, SessionAuthenticator auth) {
        context.startActivity(new Intent(context, TestActivity.class)
                .putExtra("authenticator", auth));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        final SessionAuthenticator authenticator = (SessionAuthenticator) getIntent().getSerializableExtra("authenticator");
        if (authenticator == null) {
            onBackPressed();
            return;
        }

        setTitle(getString(R.string.testingFor, authenticator.dev_acc));

        final EditText url = (EditText) findViewById(R.id.test_url);
        url.setText("https://play.google.com/apps/publish/androidapps?dev_acc=" + authenticator.dev_acc);
        final EditText body = (EditText) findViewById(R.id.test_body);
        body.setText("{\"method\":\"fetch\",\"params\":\"{\\\"2\\\":1,\\\"3\\\":7}\"}");
        final TextView response = (TextView) findViewById(R.id.test_response);

        Button send = (Button) findViewById(R.id.test_send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final JSONObject resp = PlayConsoleRequester.doRequestSync(url.getText().toString(), new JSONObject(body.getText().toString()), authenticator);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    response.setText(resp.toString());
                                }
                            });
                            System.out.println("RESP: " + resp);
                        } catch (JSONException | IOException | GeneralException ex) {
                            if (BuildConfig.DEBUG) ex.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(TestActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                }).start();
            }
        });
    }
}

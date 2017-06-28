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

import com.gianlu.playconsole.api.Models.SessionInfo;
import com.gianlu.playconsole.api.NetworkException;
import com.gianlu.playconsole.api.PlayConsoleRequest;
import com.gianlu.playconsole.api.PlayConsoleRequester;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class TestActivity extends AppCompatActivity {

    public static void startActivity(Context context, SessionInfo info) {
        context.startActivity(new Intent(context, TestActivity.class)
                .putExtra("sessionInfo", info));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        final SessionInfo info = (SessionInfo) getIntent().getSerializableExtra("sessionInfo");
        if (info == null) {
            onBackPressed();
            return;
        }

        setTitle(getString(R.string.testingFor, info.startupData.account.accountCode));

        final EditText url = (EditText) findViewById(R.id.test_url);
        url.setText("https://play.google.com/apps/publish/androidapps?dev_acc=" + info.startupData.account.accountCode);
        final EditText method = (EditText) findViewById(R.id.test_method);
        method.setText("fetch");
        final EditText params = (EditText) findViewById(R.id.test_params);
        params.setText("{\"2\":1,\"3\":7}");
        final TextView response = (TextView) findViewById(R.id.test_response);

        Button send = (Button) findViewById(R.id.test_send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PlayConsoleRequest.Builder builder = new PlayConsoleRequest.Builder();
                builder.setUrl(url.getText().toString())
                        .setRequestMethod(method.getText().toString())
                        .setRequestParams(params.getText().toString());

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final JSONObject resp = PlayConsoleRequester.get(info).execute(builder.build());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    response.setText(resp.toString());
                                }
                            });
                        } catch (JSONException | IOException | NetworkException ex) {
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

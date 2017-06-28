package com.gianlu.playconsole.api;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.client.CookieStore;

public class PlayConsoleWebView extends WebView {
    private static final Pattern FLOW_END_URL_PATTERN = Pattern.compile("https://play\\.google\\.com/apps/publish/\\?dev_acc=(\\d{20})#AppListPlace");
    private InternalListener listener;
    private CustomWebClient webClient;
    private boolean authenticating = false;

    public PlayConsoleWebView(Context context) {
        super(context);
    }

    public PlayConsoleWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PlayConsoleWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    void askForSessionAuthenticator(InternalListener listener) {
        this.listener = listener;
        startFlow();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void startFlow() {
        webClient = new CustomWebClient();
        setWebViewClient(webClient);
        getSettings().setJavaScriptEnabled(true); // We are visiting only Google pages so it's safe
        loadUrl(Utils.DEVELOPER_CONSOLE_URL);
    }

    interface InternalListener {
        void onGotPageData(String dev_acc, CookieStore cookies);

        void onFailed(Exception ex);
    }

    private class CustomWebClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return false; // Avoid requests to be submitted to the browser
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            Matcher matcher = FLOW_END_URL_PATTERN.matcher(url);
            if (!authenticating && matcher.find()) {
                authenticating = true;
                if (listener != null) listener.onGotPageData(matcher.group(1), Utils.extractCookies(Uri.parse(url)));
            }

            // TODO: Must handle if something goes wrong!
        }
    }
}

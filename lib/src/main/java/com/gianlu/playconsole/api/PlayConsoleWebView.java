package com.gianlu.playconsole.api;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.StatusLine;
import cz.msebera.android.httpclient.client.CookieStore;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.BasicCookieStore;
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder;
import cz.msebera.android.httpclient.impl.cookie.BasicClientCookie;
import cz.msebera.android.httpclient.util.EntityUtils;

public class PlayConsoleWebView extends WebView {
    private static final String DEVELOPER_CONSOLE_URL = "https://play.google.com/apps/publish/";
    private static final Pattern FLOW_END_URL_PATTERN = Pattern.compile("https://play.google.com/apps/publish/androidapps\\?dev_acc=(\\d{20})");
    private static final Pattern STARTUP_DATA_PATTERN = Pattern.compile("startupData = (\\{.*?\\});");
    private final Handler handler;
    private ISessionAuthenticator listener;
    private CustomWebClient webClient;
    private boolean authenticating = false;

    public PlayConsoleWebView(Context context) {
        super(context);
        handler = new Handler(context.getMainLooper());
    }

    public PlayConsoleWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        handler = new Handler(context.getMainLooper());
    }

    public PlayConsoleWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        handler = new Handler(context.getMainLooper());
    }

    private static Map<String, String> filterImportantHeaders(Map<String, String> headers) {
        Map<String, String> importantHeaders = new HashMap<>();
        importantHeaders.put("X-GWT-Module-Base", headers.get("X-GWT-Module-Base"));
        importantHeaders.put("X-GWT-Permutation", headers.get("X-GWT-Permutation"));
        importantHeaders.put("Content-Type", headers.get("Content-Type"));
        return importantHeaders;
    }

    public void askForSessionAuthenticator(ISessionAuthenticator listener) {
        this.listener = listener;
        startFlow();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void startFlow() {
        webClient = new CustomWebClient();
        setWebViewClient(webClient);
        getSettings().setJavaScriptEnabled(true); // We are visiting only Google pages so it's safe
        loadUrl(DEVELOPER_CONSOLE_URL);
    }

    private CookieStore extractCookies(Uri url) {
        String rawCookies = CookieManager.getInstance().getCookie(url.toString());
        CookieStore store = new BasicCookieStore();

        for (String rawCookie : rawCookies.split(";")) {
            String[] split = rawCookie.split("=");
            if (split.length < 2) continue;

            BasicClientCookie cookie = new BasicClientCookie(split[0].trim(), split[1].trim());
            cookie.setDomain(url.getHost());
            store.addCookie(cookie);
        }

        return store;
    }

    private String getXSRFTokenSync(String dev_acc, CookieStore cookieStore) throws IOException, GeneralException, JSONException {
        HttpGet get = new HttpGet(DEVELOPER_CONSOLE_URL + "?dev_acc=" + dev_acc);

        HttpClient client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();

        HttpResponse resp = client.execute(get, PlayConsoleRequester.httpContext);
        StatusLine sl = resp.getStatusLine();
        if (sl.getStatusCode() != 200) throw new GeneralException(sl);

        String html = EntityUtils.toString(resp.getEntity(), Charset.forName("UTF-8"));
        Matcher matcher = STARTUP_DATA_PATTERN.matcher(html);
        if (matcher.find()) {
            JSONObject startupData = new JSONObject(matcher.group(1)); // This data may be useful for other things also
            return new JSONObject(startupData.getString("XsrfToken")).getString("1");
        } else {
            throw new GeneralException("Cannot find startupData!");
        }
    }

    private void authenticate(final String dev_acc, final Map<String, String> headers, final CookieStore cookieStore) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String xsrfToken = getXSRFTokenSync(dev_acc, cookieStore);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null)
                                listener.onAuthenticated(new SessionAuthenticator(dev_acc, headers, xsrfToken, cookieStore));
                        }
                    });
                } catch (IOException | GeneralException | JSONException ex) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null) listener.onFailedAuthenticating(ex);
                        }
                    });
                }
            }
        }).start();
    }

    public interface ISessionAuthenticator {
        void onAuthenticated(SessionAuthenticator auth);

        void userAttentionNoMoreRequired();

        void onFailedAuthenticating(Exception ex);
    }

    private class CustomWebClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return false; // Avoid requests to be submitted to the browser
        }

        @Override
        @Nullable
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            if (authenticating) return null; // Avoid multiple calls to #authenticate()

            String url = request.getUrl().toString();
            Matcher matcher = FLOW_END_URL_PATTERN.matcher(url);
            if (matcher.find()) {
                // The flow is ended
                String dev_acc = matcher.group(1);
                Map<String, String> headers = filterImportantHeaders(request.getRequestHeaders());
                CookieStore cookieStore = extractCookies(request.getUrl());

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) listener.userAttentionNoMoreRequired();
                    }
                });

                authenticating = true;
                authenticate(dev_acc, headers, cookieStore);
            }

            // TODO: What if any of the requests matches?

            return null;
        }
    }
}

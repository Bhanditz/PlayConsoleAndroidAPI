package com.gianlu.playconsole.api;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.gianlu.playconsole.api.Models.SessionInfo;
import com.gianlu.playconsole.api.Models.StartupData;

import org.json.JSONException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.StatusLine;
import cz.msebera.android.httpclient.client.CookieStore;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder;
import cz.msebera.android.httpclient.util.EntityUtils;

public class PlayConsoleWebView extends WebView {
    private static final String DEVELOPER_CONSOLE_URL = "https://play.google.com/apps/publish/";
    private static final Pattern FLOW_END_URL_PATTERN = Pattern.compile("https://play\\.google\\.com/apps/publish/\\?dev_acc=(\\d{20})#AppListPlace");
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

    private String getHtmlConsolePage(String dev_acc, CookieStore cookieStore) throws IOException, NetworkException {
        HttpGet get = new HttpGet(DEVELOPER_CONSOLE_URL + "?dev_acc=" + dev_acc);

        HttpClient client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();

        HttpResponse resp = client.execute(get, PlayConsoleRequester.httpContext);
        StatusLine sl = resp.getStatusLine();
        if (sl.getStatusCode() != 200) throw new NetworkException(sl);

        return EntityUtils.toString(resp.getEntity(), Charset.forName("UTF-8"));
    }

    private void authenticate(final String dev_acc, final CookieStore cookieStore) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String html = getHtmlConsolePage(dev_acc, cookieStore);
                    final StartupData startupData = Utils.extractStartupData(html);
                    final String xGwtModuleBase = Utils.extractGwtModuleBase(html);
                    final String xGwtPermutation = Utils.extractGwtPermutation(html, xGwtModuleBase);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null)
                                listener.onAuthenticated(new SessionInfo(startupData, xGwtModuleBase, xGwtPermutation, cookieStore));
                        }
                    });
                } catch (IOException | NetworkException | JSONException | HtmlParsingException ex) {
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
        void onAuthenticated(SessionInfo auth);

        void userAttentionNoMoreRequired();

        void onFailedAuthenticating(Exception ex);
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
                authenticate(matcher.group(1), Utils.extractCookies(Uri.parse(url)));

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) listener.userAttentionNoMoreRequired();
                    }
                });
            }
        }
    }
}

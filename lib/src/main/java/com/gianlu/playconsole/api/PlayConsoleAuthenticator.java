package com.gianlu.playconsole.api;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;

import com.gianlu.playconsole.api.Models.SessionInfo;
import com.gianlu.playconsole.api.Models.StartupData;

import org.json.JSONException;

import java.io.IOException;
import java.nio.charset.Charset;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.StatusLine;
import cz.msebera.android.httpclient.client.CookieStore;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder;
import cz.msebera.android.httpclient.util.EntityUtils;

public class PlayConsoleAuthenticator {

    public static void authenticateSilently(Context context, final ISilentAuth listener) {
        final String lastDevAcc = Prefs.getString(context, Prefs.Keys.LAST_DEV_ACC, null);
        if (lastDevAcc == null) {
            listener.notAuthenticated();
            return;
        }

        final Handler handler = new Handler(context.getMainLooper());
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final CookieStore cookies = Utils.extractCookies(Uri.parse(Utils.buildConsoleUrl(lastDevAcc)));
                    final String html = getHtmlConsolePage(lastDevAcc, cookies);

                    final SessionInfo info = authenticate(html, cookies);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onAuthenticated(info);
                        }
                    });
                } catch (NotAuthenticatedException ex) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.notAuthenticated();
                        }
                    });
                } catch (IOException | NetworkException | HtmlParsingException | JSONException ex) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onFailedAuthenticating(ex);
                        }
                    });
                }
            }
        }).start();
    }

    public static void authenticateWithWebView(PlayConsoleWebView webView, final IWebViewAuth listener) {
        listener.userAttentionRequired();

        final Handler handler = new Handler(webView.getContext().getMainLooper());
        webView.askForSessionAuthenticator(new PlayConsoleWebView.InternalListener() {
            @Override
            public void onGotPageData(final String dev_acc, final CookieStore cookies) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.userAttentionNoMoreRequired();
                    }
                });

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final SessionInfo info = authenticateSync(dev_acc, cookies);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onAuthenticated(info);
                                }
                            });
                        } catch (final NotAuthenticatedException ex) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    listener.userAttentionRequired();
                                }
                            });
                        } catch (final IOException | NetworkException | HtmlParsingException | JSONException ex) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onFailedAuthenticating(ex);
                                }
                            });
                        }
                    }
                }).start();
            }

            @Override
            public void onFailed(final Exception ex) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onFailedAuthenticating(ex);
                    }
                });
            }
        });
    }

    private static String getHtmlConsolePage(String dev_acc, CookieStore cookieStore) throws IOException, NetworkException {
        HttpGet get = new HttpGet(Utils.DEVELOPER_CONSOLE_URL + "?dev_acc=" + dev_acc);

        HttpClient client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();

        HttpResponse resp = client.execute(get);
        StatusLine sl = resp.getStatusLine();
        if (sl.getStatusCode() != 200) throw new NetworkException(sl);

        return EntityUtils.toString(resp.getEntity(), Charset.forName("UTF-8"));
    }

    private static SessionInfo authenticateSync(final String dev_acc, final CookieStore cookieStore) throws IOException, NetworkException, HtmlParsingException, JSONException, NotAuthenticatedException {
        return authenticate(getHtmlConsolePage(dev_acc, cookieStore), cookieStore);
    }

    private static SessionInfo authenticate(String html, final CookieStore cookieStore) throws IOException, NetworkException, HtmlParsingException, JSONException, NotAuthenticatedException {
        if (html.contains("Redirecting...")) throw new NotAuthenticatedException();

        final StartupData startupData = Utils.extractStartupData(html);
        final String xGwtModuleBase = Utils.extractGwtModuleBase(html);
        final String xGwtPermutation = Utils.extractGwtPermutation(html, xGwtModuleBase);

        return new SessionInfo(startupData, xGwtModuleBase, xGwtPermutation, cookieStore);
    }

    public interface ISilentAuth {
        void onAuthenticated(SessionInfo info);

        void notAuthenticated();

        void onFailedAuthenticating(Exception ex);
    }

    public interface IWebViewAuth {
        void onAuthenticated(SessionInfo info);

        void userAttentionNoMoreRequired();

        void userAttentionRequired();

        void onFailedAuthenticating(Exception ex);
    }
}

package com.gianlu.playconsole.api;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.gianlu.playconsole.api.Exceptions.NetworkException;
import com.gianlu.playconsole.api.Exceptions.PlayConsoleException;
import com.gianlu.playconsole.api.Models.AndroidApp;
import com.gianlu.playconsole.api.Models.AppVersionsHistory;
import com.gianlu.playconsole.api.Models.DetailedAndroidApp;
import com.gianlu.playconsole.api.Models.SessionInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayConsole {
    private final PlayConsoleRequester requester;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final Handler handler;

    public PlayConsole(Context context, PlayConsoleRequester requester) {
        this.requester = requester;
        this.handler = new Handler(context.getMainLooper());
    }

    public SessionInfo getSessionInfo() {
        return requester.info;
    }

    private String buildUrl(String endpoint) {
        return Utils.DEVELOPER_CONSOLE_URL + endpoint + "/?dev_acc=" + getSessionInfo().startupData.account.accountCode;
    }

    public void androidapps(Method method, JSONObject params, @NonNull final IJSONObject listener) {
        final PlayConsoleRequest request = new PlayConsoleRequest.Builder()
                .setUrl(buildUrl("androidapps"))
                .setRequestMethod(method.method)
                .setRequestParams(params.toString()).build();

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final JSONObject resp = requester.execute(request);
                    raiseException(resp);

                    try {
                        listener.onResponse(resp);
                    } catch (JSONException ex) {
                        listener.onException(ex);
                    }
                } catch (JSONException | NetworkException | IOException | PlayConsoleException ex) {
                    listener.onException(ex);
                }
            }
        });
    }

    /**
     * Retrieves a list of {@link AndroidApp} associated with the current account
     *
     * @param listener handles the request
     */
    public void listAndroidApps(@NonNull final IResult<List<AndroidApp>> listener) {
        try {
            androidapps(Method.FETCH, new JSONObject("{\"2\":1,\"3\":7}"), new IJSONObject() {
                @Override
                public void onResponse(JSONObject resp) throws JSONException {
                    final List<AndroidApp> apps = AndroidApp.toAndroidAppsList(resp.getJSONObject("result").getJSONArray("1"));
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onResult(apps);
                        }
                    });
                }

                @Override
                public void onException(final Exception ex) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onException(ex);
                        }
                    });
                }
            });
        } catch (final JSONException ex) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onException(ex);
                }
            });
        }
    }

    /**
     * Retrieves a list of {@link DetailedAndroidApp} specified by their package name
     *
     * @param packageNames package names of the {@link DetailedAndroidApp} to retrieve
     * @param listener     handles the request
     */
    public void listDetailedAndroidApps(List<String> packageNames, @NonNull final IResult<List<DetailedAndroidApp>> listener) {
        try {
            JSONObject params = new JSONObject().put("1", Utils.toJSONArray(packageNames)).put("3", 1);
            androidapps(Method.FETCH, params, new IJSONObject() {
                @Override
                public void onResponse(JSONObject resp) throws JSONException {
                    final List<DetailedAndroidApp> apps = DetailedAndroidApp.toDetailedAndroidAppsList(resp.getJSONObject("result").getJSONArray("1"));
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onResult(apps);
                        }
                    });
                }

                @Override
                public void onException(final Exception ex) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onException(ex);
                        }
                    });
                }
            });
        } catch (final JSONException ex) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onException(ex);
                }
            });
        }
    }

    /**
     * Retrieves a {@link DetailedAndroidApp} specified by its package name
     *
     * @param packageName package name of the {@link DetailedAndroidApp} to retrieve
     * @param listener    handles the request
     */
    public void fetchDetailedAndroidApp(String packageName, @NonNull final IResult<DetailedAndroidApp> listener) {
        listDetailedAndroidApps(Collections.singletonList(packageName), new IResult<List<DetailedAndroidApp>>() {
            @Override
            public void onResult(List<DetailedAndroidApp> result) {
                listener.onResult(result.get(0));
            }

            @Override
            public void onException(Exception ex) {
                listener.onException(ex);
            }
        });
    }

    /**
     * Retrieves a list of {@link DetailedAndroidApp} specified by their package name
     *
     * @param packageNames package names of the {@link DetailedAndroidApp} to retrieve
     * @param listener     handles the request
     */
    public void listAppVersionsHistories(List<String> packageNames, @NonNull final IResult<List<AppVersionsHistory>> listener) {
        try {
            JSONObject params = new JSONObject().put("1", Utils.toJSONArray(packageNames)).put("3", 4);
            androidapps(Method.FETCH, params, new IJSONObject() {
                @Override
                public void onResponse(JSONObject resp) throws JSONException {
                    final List<AppVersionsHistory> apps = AppVersionsHistory.toAppVersionsHistoriesList(resp.getJSONObject("result").getJSONArray("1"));
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onResult(apps);
                        }
                    });
                }

                @Override
                public void onException(final Exception ex) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onException(ex);
                        }
                    });
                }
            });
        } catch (final JSONException ex) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onException(ex);
                }
            });
        }
    }

    /**
     * Retrieves a {@link AppVersionsHistory} specified by its package name
     *
     * @param packageName package name of the {@link AppVersionsHistory} to retrieve
     * @param listener    handles the request
     */
    public void fetchAppVersionsHistory(String packageName, @NonNull final IResult<AppVersionsHistory> listener) {
        listAppVersionsHistories(Collections.singletonList(packageName), new IResult<List<AppVersionsHistory>>() {
            @Override
            public void onResult(List<AppVersionsHistory> result) {
                listener.onResult(result.get(0));
            }

            @Override
            public void onException(Exception ex) {
                listener.onException(ex);
            }
        });
    }

    private void raiseException(JSONObject resp) throws PlayConsoleException {
        if (resp.has("error")) throw new PlayConsoleException(resp.optJSONObject("error"));
    }

    public enum Method {
        FETCH("fetch");

        private final String method;

        Method(String method) {
            this.method = method;
        }
    }

    public interface IJSONObject {
        void onResponse(JSONObject resp) throws JSONException;

        void onException(Exception ex);
    }

    public interface IResult<E> {
        void onResult(E result);

        void onException(Exception ex);
    }
}

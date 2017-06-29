package com.gianlu.playconsole.api;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.gianlu.playconsole.api.Exceptions.NetworkException;
import com.gianlu.playconsole.api.Exceptions.PlayConsoleException;
import com.gianlu.playconsole.api.Models.AndroidApp;
import com.gianlu.playconsole.api.Models.Annotation;
import com.gianlu.playconsole.api.Models.AppVersionsHistory;
import com.gianlu.playconsole.api.Models.CountriesStats;
import com.gianlu.playconsole.api.Models.CrashesStats;
import com.gianlu.playconsole.api.Models.DetailedAndroidApp;
import com.gianlu.playconsole.api.Models.InstallsStats;
import com.gianlu.playconsole.api.Models.RatingStats;
import com.gianlu.playconsole.api.Models.SessionInfo;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Duration;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
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

    public void basicRequest(Endpoint endpoint, Method method, JSONObject params, @NonNull final IJSONObject listener) {
        final PlayConsoleRequest request = new PlayConsoleRequest.Builder()
                .setUrl(buildUrl(endpoint.endpoint))
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
                    } catch (JSONException | ParseException ex) {
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
            basicRequest(Endpoint.ANDROIDAPPS, Method.FETCH, new JSONObject("{\"2\":1,\"3\":7}"), new IJSONObject() {
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
            basicRequest(Endpoint.ANDROIDAPPS, Method.FETCH, params, new IJSONObject() {
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
            basicRequest(Endpoint.ANDROIDAPPS, Method.FETCH, params, new IJSONObject() {
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

    /**
     * Retrieves a {@link InstallsStats} specified by the package name and the time interval
     *
     * @param packageName package name of the {@link InstallsStats} to retrieve
     * @param interval    a {@link TimeInterval}
     * @param listener    handles the request
     */
    public void fetchInstallsStats(String packageName, TimeInterval interval, @NonNull final IResult<InstallsStats> listener) {
        Pair<Long, Long> timestamps = interval.getAsTimestamps();
        fetchInstallsStats(packageName, timestamps.first, timestamps.second, listener);
    }

    /**
     * Retrieves a {@link InstallsStats} specified by the package name and the time interval
     *
     * @param packageName package name of the {@link InstallsStats} to retrieve
     * @param start       interval start (in millis)
     * @param end         interval end (in millis)
     * @param listener    handles the request
     */
    public void fetchInstallsStats(String packageName, long start, long end, @NonNull final IResult<InstallsStats> listener) {
        try {
            JSONObject params = new JSONObject();
            JSONObject actualParams = new JSONObject();
            JSONObject appInfo = new JSONObject();
            appInfo.put("1", packageName)
                    .put("2", 1); // TODO: What's this?

            // Interval
            DateTime today = new DateTime();
            actualParams.put("2", Days.daysBetween(today, new DateTime(start)).getDays())
                    .put("3", Days.daysBetween(today, new DateTime(end)).getDays());

            // Dimensions
            actualParams.put("7", new JSONArray().put(17))
                    .put("8", new JSONArray().put(10).put(11));

            actualParams.put("12", 0).put("15", 1); // TODO: What's this?

            actualParams.put("1", appInfo);
            params.put("1", new JSONArray().put(actualParams));

            basicRequest(Endpoint.STATISTICS, Method.FETCH_STATS, params, new IJSONObject() {
                @Override
                public void onResponse(JSONObject resp) throws JSONException, ParseException {
                    final InstallsStats stats = new InstallsStats(resp.getJSONObject("result").getJSONArray("1").getJSONObject(0));
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onResult(stats);
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
     * Retrieves a {@link CountriesStats} specified by the package name and the time interval
     *
     * @param packageName package name of the {@link CountriesStats} to retrieve
     * @param interval    a {@link TimeInterval}
     * @param listener    handles the request
     */
    public void fetchCountriesStats(String packageName, TimeInterval interval, @NonNull final IResult<CountriesStats> listener) {
        Pair<Long, Long> timestamps = interval.getAsTimestamps();
        fetchCountriesStats(packageName, timestamps.first, timestamps.second, listener);
    }

    /**
     * Retrieves a {@link CountriesStats} specified by the package name and the time interval
     *
     * @param packageName package name of the {@link CountriesStats} to retrieve
     * @param start       interval start (in millis)
     * @param end         interval end (in millis)
     * @param listener    handles the request
     */
    public void fetchCountriesStats(String packageName, long start, long end, @NonNull final IResult<CountriesStats> listener) {
        try {
            JSONObject params = new JSONObject();
            JSONObject actualParams = new JSONObject();
            JSONObject appInfo = new JSONObject();
            appInfo.put("1", packageName)
                    .put("2", 1); // TODO: What's this?

            // Interval
            DateTime today = new DateTime();
            actualParams.put("2", Days.daysBetween(today, new DateTime(start)).getDays())
                    .put("3", Days.daysBetween(today, new DateTime(end)).getDays());

            // Dimensions
            actualParams.put("7", new JSONArray().put(3))
                    .put("8", new JSONArray().put(10));

            actualParams.put("1", appInfo);
            params.put("1", new JSONArray().put(actualParams));

            basicRequest(Endpoint.STATISTICS, Method.FETCH_STATS, params, new IJSONObject() {
                @Override
                public void onResponse(JSONObject resp) throws JSONException, ParseException {
                    final CountriesStats stats = new CountriesStats(resp.getJSONObject("result").getJSONArray("1").getJSONObject(0));
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onResult(stats);
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
     * Retrieves a {@link RatingStats} specified by the package name and the time interval
     *
     * @param packageName package name of the {@link RatingStats} to retrieve
     * @param interval    a {@link TimeInterval}
     * @param listener    handles the request
     */
    public void fetchRatingsStats(String packageName, TimeInterval interval, @NonNull final IResult<RatingStats> listener) {
        Pair<Long, Long> timestamps = interval.getAsTimestamps();
        fetchRatingsStats(packageName, timestamps.first, timestamps.second, listener);
    }

    /**
     * Retrieves a {@link RatingStats} specified by the package name and the time interval
     *
     * @param packageName package name of the {@link RatingStats} to retrieve
     * @param start       interval start (in millis)
     * @param end         interval end (in millis)
     * @param listener    handles the request
     */
    public void fetchRatingsStats(String packageName, long start, long end, @NonNull final IResult<RatingStats> listener) {
        try {
            JSONObject params = new JSONObject();
            JSONObject actualParams = new JSONObject();
            JSONObject appInfo = new JSONObject();
            appInfo.put("1", packageName)
                    .put("2", 1); // TODO: What's this?

            // Interval
            DateTime today = new DateTime();
            actualParams.put("2", Days.daysBetween(today, new DateTime(start)).getDays())
                    .put("3", Days.daysBetween(today, new DateTime(end)).getDays());

            // Dimensions
            actualParams.put("7", new JSONArray().put(17))
                    .put("8", new JSONArray().put(71).put(15));

            actualParams.put("12", 0).put("15", 1); // TODO: What's this?

            actualParams.put("1", appInfo);
            params.put("1", new JSONArray().put(actualParams));

            basicRequest(Endpoint.STATISTICS, Method.FETCH_STATS, params, new IJSONObject() {
                @Override
                public void onResponse(JSONObject resp) throws JSONException, ParseException {
                    final RatingStats stats = new RatingStats(resp.getJSONObject("result").getJSONArray("1").getJSONObject(0));
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onResult(stats);
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
     * Retrieves a {@link CrashesStats} specified by the package name and the time interval
     *
     * @param packageName package name of the {@link CrashesStats} to retrieve
     * @param interval    a {@link TimeInterval}
     * @param listener    handles the request
     */
    public void fetchCrashesStats(String packageName, TimeInterval interval, @NonNull final IResult<CrashesStats> listener) {
        Pair<Long, Long> timestamps = interval.getAsTimestamps();
        fetchCrashesStats(packageName, timestamps.first, timestamps.second, listener);
    }

    /**
     * Retrieves a {@link CrashesStats} specified by the package name and the time interval
     *
     * @param packageName package name of the {@link CrashesStats} to retrieve
     * @param start       interval start (in millis)
     * @param end         interval end (in millis)
     * @param listener    handles the request
     */
    public void fetchCrashesStats(String packageName, long start, long end, @NonNull final IResult<CrashesStats> listener) {
        try {
            JSONObject params = new JSONObject();
            JSONObject actualParams = new JSONObject();
            JSONObject appInfo = new JSONObject();
            appInfo.put("1", packageName)
                    .put("2", 1); // TODO: What's this?

            // Interval
            DateTime today = new DateTime();
            actualParams.put("2", Days.daysBetween(today, new DateTime(start)).getDays())
                    .put("3", Days.daysBetween(today, new DateTime(end)).getDays());

            // Dimensions
            actualParams.put("7", new JSONArray().put(17))
                    .put("8", new JSONArray().put(96));

            actualParams.put("12", 0).put("15", 1); // TODO: What's this?

            actualParams.put("1", appInfo);
            params.put("1", new JSONArray().put(actualParams));

            basicRequest(Endpoint.STATISTICS, Method.FETCH_STATS, params, new IJSONObject() {
                @Override
                public void onResponse(JSONObject resp) throws JSONException, ParseException {
                    final CrashesStats stats = new CrashesStats(resp.getJSONObject("result").getJSONArray("1").getJSONObject(0));
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onResult(stats);
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
     * Retrieves the number of active installs on the app specified by its package name
     *
     * @param packageName package name of the {@link Long} to retrieve
     * @param listener    handles the request
     */
    public void fetchActiveInstallsNum(String packageName, @NonNull final IResult<Long> listener) {
        try {
            JSONObject params = new JSONObject();
            JSONObject actualParams = new JSONObject();
            JSONObject appInfo = new JSONObject();
            appInfo.put("1", packageName)
                    .put("2", 1); // TODO: What's this?

            // Interval
            actualParams.put("2", -1).put("3", -1);

            // Dimensions
            actualParams.put("8", new JSONArray().put(81));

            actualParams.put("1", appInfo);
            params.put("1", new JSONArray().put(actualParams));

            basicRequest(Endpoint.STATISTICS, Method.FETCH_STATS, params, new IJSONObject() {
                @Override
                public void onResponse(JSONObject resp) throws JSONException, ParseException {
                    final Long num = resp.getJSONObject("result").getJSONArray("1").getJSONObject(0).getJSONArray("1").getJSONObject(0).getJSONArray("2").getJSONObject(0).getLong("1"); // Insane
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onResult(num);
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
     * Retrieves a list of {@link Annotation} specified by the package name
     *
     * @param packageName package name of the list of {@link Annotation} to retrieve
     * @param listener    handles the request
     */
    public void fetchAnnotations(String packageName, @NonNull final IResult<List<Annotation>> listener) {
        try {
            JSONObject params = new JSONObject();
            params.put("1", packageName);

            basicRequest(Endpoint.STATISTICS, Method.GET_ANNOTATIONS, params, new IJSONObject() {
                @Override
                public void onResponse(JSONObject resp) throws JSONException, ParseException {
                    final List<Annotation> annotations = Annotation.toAnnotationsList(resp.getJSONObject("result").getJSONObject("1").getJSONArray("1")); // Insane
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onResult(annotations);
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

    private void raiseException(JSONObject resp) throws PlayConsoleException {
        if (resp.has("error")) throw new PlayConsoleException(resp.optJSONObject("error"));
    }

    public enum TimeInterval {
        LAST_7_DAYS,
        LAST_30_DAYS,
        LAST_60_DAYS;

        Pair<Long, Long> getAsTimestamps() {
            switch (this) {
                default:
                case LAST_7_DAYS:
                    return getTimeAsTimestampInternal(7);
                case LAST_30_DAYS:
                    return getTimeAsTimestampInternal(30);
                case LAST_60_DAYS:
                    return getTimeAsTimestampInternal(60);
            }
        }

        private Pair<Long, Long> getTimeAsTimestampInternal(int days) {
            return new Pair<>(
                    new DateTime().minus(Duration.standardDays(days)).getMillis(),
                    new DateTime().minus(Duration.standardDays(1)).getMillis());
        }
    }

    // Not all endpoints work with all methods
    public enum Endpoint {
        STATISTICS("statistics"),
        ANDROIDAPPS("androidapps");
        private final String endpoint;

        Endpoint(String endpoint) {
            this.endpoint = endpoint;
        }
    }

    // Not all methods work with all endpoints
    public enum Method {
        FETCH("fetch"),
        FETCH_STATS("fetchStats"),
        GET_ANNOTATIONS("getAnnotations");

        private final String method;

        Method(String method) {
            this.method = method;
        }
    }

    public interface IJSONObject {
        void onResponse(JSONObject resp) throws JSONException, ParseException;

        void onException(Exception ex);
    }

    public interface IResult<E> {
        void onResult(E result);

        void onException(Exception ex);
    }
}

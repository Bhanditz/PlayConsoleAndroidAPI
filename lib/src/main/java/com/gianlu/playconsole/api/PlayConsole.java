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
import com.gianlu.playconsole.api.Models.DetailedAndroidApp;
import com.gianlu.playconsole.api.Models.SessionInfo;
import com.gianlu.playconsole.api.Models.Stats;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Duration;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    /**
     * Fetches stats
     *
     * @param builder  see {@link StatsRequestBuilder}
     * @param listener handles the request
     */
    public void fetchStats(StatsRequestBuilder builder, @NonNull final IResult<Stats> listener) {
        try {
            JSONObject params = new JSONObject();
            JSONObject actualParams = new JSONObject();
            JSONObject appInfo = new JSONObject();
            appInfo.put("1", builder.packageName).put("2", 1);

            Pair<Long, Long> interval = builder.interval;
            if (interval == null) {
                actualParams.put("2", -1).put("3", -1);
            } else {
                DateTime today = new DateTime();
                actualParams.put("2", Days.daysBetween(today, new DateTime(interval.first)).getDays())
                        .put("3", Days.daysBetween(today, new DateTime(interval.second)).getDays());
            }

            if (builder.dimension != null)
                actualParams.put("7", new JSONArray().put(builder.dimension.val));
            if (builder.metric != null)
                actualParams.put("8", new JSONArray().put(builder.metric.val));

            if (builder.additionalParams != null) {
                for (Map.Entry<String, Object> entry : builder.additionalParams.entrySet())
                    actualParams.put(entry.getKey(), entry.getValue());
            }

            actualParams.put("1", appInfo);
            params.put("1", new JSONArray().put(actualParams));

            basicRequest(Endpoint.STATISTICS, Method.FETCH_STATS, params, new IJSONObject() {
                @Override
                public void onResponse(JSONObject resp) throws JSONException, ParseException {
                    final Stats stats = new Stats(resp.getJSONObject("result").getJSONArray("1").getJSONObject(0));
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

    private void raiseException(JSONObject resp) throws PlayConsoleException {
        if (resp.has("error")) throw new PlayConsoleException(resp.optJSONObject("error"));
    }

    public enum TimeInterval {
        LAST_7_DAYS,
        LAST_30_DAYS,
        LAST_60_DAYS;

        public Pair<Long, Long> getAsTimestamps() {
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

    @SuppressWarnings("unused")
    public enum Metric {
        /**
         * Number of installs per day
         */
        INSTALLS(10),

        /**
         * Number of uninstalls per day
         */
        UNINSTALLS(11),

        /**
         * Number of ratings per day
         */
        RATINGS_NUM(71),

        /**
         * Average rating per day
         */
        AVERAGE_RATING(15),

        /**
         * Number of active installs registered every day
         */
        ACTIVE_INSTALLS(81, Dimension.SDK_VERSION, Dimension.COUNTRY),

        /**
         * Number of crashes per day
         */
        CRASHES(96, Dimension.SDK_VERSION, Dimension.COUNTRY);

        private final int val;
        private final Dimension[] unsupportedDimensions;

        Metric(int val, Dimension... unsupportedDimensions) {
            this.val = val;
            this.unsupportedDimensions = unsupportedDimensions;
        }

        public static String[] formalValues(Context context) {
            Metric[] values = values();
            String[] formalValues = new String[values.length];

            for (int i = 0; i < values.length; i++)
                formalValues[i] = values[i].getFormal(context);

            return formalValues;
        }

        public List<Dimension> unsupportedDimensions() {
            return new ArrayList<>(Arrays.asList(unsupportedDimensions));
        }

        public String getFormal(Context context) {
            switch (this) {
                case INSTALLS:
                    return context.getString(R.string.installs);
                case UNINSTALLS:
                    return context.getString(R.string.uninstalls);
                case RATINGS_NUM:
                    return context.getString(R.string.ratingsNum);
                case AVERAGE_RATING:
                    return context.getString(R.string.averageRating);
                case ACTIVE_INSTALLS:
                    return context.getString(R.string.activeInstalls);
                case CRASHES:
                    return context.getString(R.string.crashes);
                default:
                    return context.getString(R.string.unknown);
            }
        }

        public boolean supportsNoDimension() {
            return this == ACTIVE_INSTALLS;
        }
    }

    @SuppressWarnings("unused")
    public enum Dimension {
        /**
         * Date in yyyyMMdd format
         */
        DATE(17),

        /**
         * Country as both country code and country name
         */
        COUNTRY(3),

        /**
         * Device Android SDK version as both API code and Android version
         */
        SDK_VERSION(1);

        private final int val;

        Dimension(int val) {
            this.val = val;
        }

        public static List<Dimension> listValues() {
            return new ArrayList<>(Arrays.asList(values()));
        }

        public static String[] formalValues(Context context, List<Dimension> dimensions) {
            String[] formalValues = new String[dimensions.size()];

            for (int i = 0; i < dimensions.size(); i++)
                formalValues[i] = dimensions.get(i).getFormal(context);

            return formalValues;
        }

        public String getFormal(Context context) {
            switch (this) {
                case DATE:
                    return context.getString(R.string.date);
                case COUNTRY:
                    return context.getString(R.string.country);
                case SDK_VERSION:
                    return context.getString(R.string.sdkVersion);
                default:
                    return context.getString(R.string.unknown);
            }
        }
    }

    @SuppressWarnings("unused")
    public enum Endpoint {
        STATISTICS("statistics"),
        ANDROIDAPPS("androidapps");
        private final String endpoint;

        Endpoint(String endpoint) {
            this.endpoint = endpoint;
        }
    }

    @SuppressWarnings("unused")
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

    /**
     * Creates a request to pass in {@link #fetchStats(StatsRequestBuilder, IResult)}
     */
    public static class StatsRequestBuilder {
        private final String packageName;
        private final Map<String, Object> additionalParams;
        private Metric metric;
        private Dimension dimension;
        private Pair<Long, Long> interval;

        /**
         * Default constructor
         *
         * @param packageName the package name of the app
         */
        public StatsRequestBuilder(String packageName) {
            this.packageName = packageName;
            this.additionalParams = new HashMap<>();
        }

        /**
         * Time interval for the request.
         *
         * @param interval time interval
         * @return this
         */
        public StatsRequestBuilder setInterval(TimeInterval interval) {
            this.interval = interval.getAsTimestamps();
            return this;
        }

        /**
         * See {@link Metric}
         *
         * @param metric the metric
         * @return this
         */
        public StatsRequestBuilder setMetric(Metric metric) {
            this.metric = metric;
            return this;
        }

        /**
         * See {@link Dimension}
         *
         * @param dimension the dimension
         * @return this
         */
        public StatsRequestBuilder setDimension(Dimension dimension) {
            this.dimension = dimension;
            return this;
        }

        /**
         * Time interval for the request. Must be {@param start} < {@param end}.
         *
         * @param start period start in millis
         * @param end   period end in millis
         * @return this
         */
        public StatsRequestBuilder setInterval(long start, long end) {
            this.interval = new Pair<>(start, end);
            return this;
        }

        /**
         * If enabled avoid missing dates in the interval.
         * This may not work with {@link Dimension}s other than {@link Dimension#DATE}.
         *
         * @param enabled enabled
         * @return this
         */
        public StatsRequestBuilder setPaddingEnabled(boolean enabled) {
            this.additionalParams.put("15", enabled ? 1 : 0);
            return this;
        }

        /**
         * Required in some requests, pay attention.
         *
         * @param additionalParams Map of keys and values like a JSONObject
         * @return this
         */
        public StatsRequestBuilder addAdditionalParams(Map<String, Object> additionalParams) {
            this.additionalParams.putAll(additionalParams);
            return this;
        }
    }
}

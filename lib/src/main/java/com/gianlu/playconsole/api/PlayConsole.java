package com.gianlu.playconsole.api;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.gianlu.playconsole.api.Exceptions.NetworkException;
import com.gianlu.playconsole.api.Exceptions.PlayConsoleException;
import com.gianlu.playconsole.api.Models.AndroidApp;
import com.gianlu.playconsole.api.Models.Annotation;
import com.gianlu.playconsole.api.Models.Apk;
import com.gianlu.playconsole.api.Models.AppRelease;
import com.gianlu.playconsole.api.Models.DetailedAndroidApp;
import com.gianlu.playconsole.api.Models.Notification;
import com.gianlu.playconsole.api.Models.ReleaseTracksSummary;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
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

    public Notifications notifications() {
        return new Notifications();
    }

    public AppReleases appReleases() {
        return new AppReleases();
    }

    public AndroidApps androidApps() {
        return new AndroidApps();
    }

    public Statistics statistics() {
        return new Statistics();
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
         * The number of devices that users install your app on for the first time.
         */
        DEVICE_INSTALLS(4),

        /**
         * The number of devices which your app is uninstalled from.
         */
        DEVICE_UNINSTALLS(5),

        /**
         * The number of devices that install an update for your app.
         */
        DEVICE_UPGRADES(6),

        /**
         * The number of unique users who have ever installed your app, including those who have uninstalled it.
         */
        USER_CUMULATIVE_INSTALLS(8),

        /**
         * The number of unique users who install your app on at least one device for the first time.
         */
        USER_INSTALLS(10),

        /**
         * The number of unique users who uninstall your app from all of their devices.
         */
        USER_UNINSTALLS(11),

        /**
         * The average rating your app receives.
         */
        AVERAGE_RATING(15),

        /**
         * The average rating your app has received since you first launched.
         */
        CUMULATIVE_AVERAGE_RATING(16),

        /**
         * The revenue that your app generates each day. This is based on estimated sales and includes any tax or other fees.
         */
        REVENUE(17, Dimension.SDK_VERSION, Dimension.LANGUAGE, Dimension.APPLICATION_VERSION, Dimension.CARRIER),

        /**
         * The number of unique users who buy one or more items in your app.
         */
        NEW_BUYERS(36, Dimension.SDK_VERSION, Dimension.LANGUAGE, Dimension.APPLICATION_VERSION, Dimension.CARRIER),

        /**
         * The number of unique users who buy anything in your app for the first time.
         */
        BUYERS(38, Dimension.SDK_VERSION, Dimension.LANGUAGE, Dimension.APPLICATION_VERSION, Dimension.CARRIER),

        /**
         * The number of ratings your app received.
         */
        RATINGS_NUM(71),

        /**
         * Number of Android devices that have been active in the previous 30 days that your app is installed on.
         */
        ACTIVE_INSTALLS(81, Dimension.SDK_VERSION, Dimension.COUNTRY),

        /**
         * Number of crash reports collected from Android devices.
         */
        CRASHES(96, Dimension.SDK_VERSION, Dimension.COUNTRY),

        /**
         * Number of ANR (Application Not Responding) reports collected from Android devices.
         */
        ANR(97, Dimension.SDK_VERSION, Dimension.COUNTRY),

        /**
         * How many times the app has been updated.
         */
        EVENTS_UPDATE(104, Dimension.SDK_VERSION, Dimension.DEVICE, Dimension.COUNTRY, Dimension.LANGUAGE, Dimension.CARRIER),

        /**
         * How many times the app has been uninstalled.
         */
        EVENTS_UNINSTALL(146, Dimension.SDK_VERSION, Dimension.DEVICE, Dimension.COUNTRY, Dimension.LANGUAGE, Dimension.CARRIER),

        /**
         * How many times the app has been installed.
         */
        EVENTS_INSTALL(147, Dimension.SDK_VERSION, Dimension.DEVICE, Dimension.COUNTRY, Dimension.LANGUAGE, Dimension.CARRIER);

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
                case DEVICE_INSTALLS:
                    return context.getString(R.string.deviceInstalls);
                case DEVICE_UNINSTALLS:
                    return context.getString(R.string.deviceUninstalls);
                case DEVICE_UPGRADES:
                    return context.getString(R.string.deviceUpgrades);
                case USER_CUMULATIVE_INSTALLS:
                    return context.getString(R.string.userCumulativeInstalls);
                case USER_INSTALLS:
                    return context.getString(R.string.userInstalls);
                case USER_UNINSTALLS:
                    return context.getString(R.string.userUninstalls);
                case CUMULATIVE_AVERAGE_RATING:
                    return context.getString(R.string.cumulativeAverageRating);
                case REVENUE:
                    return context.getString(R.string.revenue);
                case NEW_BUYERS:
                    return context.getString(R.string.newBuyers);
                case BUYERS:
                    return context.getString(R.string.buyers);
                case RATINGS_NUM:
                    return context.getString(R.string.ratingsNum);
                case AVERAGE_RATING:
                    return context.getString(R.string.averageRating);
                case ACTIVE_INSTALLS:
                    return context.getString(R.string.activeInstalls);
                case CRASHES:
                    return context.getString(R.string.crashes);
                case ANR:
                    return context.getString(R.string.anrs);
                case EVENTS_UPDATE:
                    return context.getString(R.string.eventsUpdate);
                case EVENTS_UNINSTALL:
                    return context.getString(R.string.eventsUninstall);
                case EVENTS_INSTALL:
                    return context.getString(R.string.eventsInstall);
                default:
                    return context.getString(R.string.unknown);
            }
        }
    }

    @SuppressWarnings("unused")
    public enum Dimension {
        /**
         * Device Android SDK version as both API code and Android version
         */
        SDK_VERSION(1),

        /**
         * Device model
         */
        DEVICE(2),

        /**
         * Country as both country code and country name
         */
        COUNTRY(3),

        /**
         * Device language as both language code and language name
         */
        LANGUAGE(4),

        /**
         * Application version as version code
         */
        APPLICATION_VERSION(5),

        /**
         * Carrier name
         */
        CARRIER(6),

        /**
         * Date in yyyyMMdd format
         */
        DATE(17);

        public final int val;

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

        public static Dimension parse(int val) {
            switch (val) {
                case 1:
                    return SDK_VERSION;
                case 2:
                    return DEVICE;
                case 3:
                    return COUNTRY;
                case 4:
                    return LANGUAGE;
                case 5:
                    return APPLICATION_VERSION;
                case 6:
                    return CARRIER;
                default:
                case 17:
                    return DATE;
            }
        }

        public String getFormal(Context context) {
            switch (this) {
                case DATE:
                    return context.getString(R.string.date);
                case DEVICE:
                    return context.getString(R.string.device);
                case COUNTRY:
                    return context.getString(R.string.country);
                case SDK_VERSION:
                    return context.getString(R.string.sdkVersion);
                case LANGUAGE:
                    return context.getString(R.string.language);
                case APPLICATION_VERSION:
                    return context.getString(R.string.appVersion);
                case CARRIER:
                    return context.getString(R.string.carrier);
                default:
                    return context.getString(R.string.unknown);
            }
        }
    }

    @SuppressWarnings("unused")
    public enum Endpoint {
        STATISTICS("statistics"),
        ANDROIDAPPS("androidapps"),
        APPRELEASES("appreleases"),
        NOTIFICATIONS("notifications");
        private final String endpoint;

        Endpoint(String endpoint) {
            this.endpoint = endpoint;
        }
    }

    @SuppressWarnings("unused")
    public enum Method {
        FETCH("fetch"),
        GET_RELEASE_TRACKS_SUMMARY("getReleaseTracksSummary"),
        FETCH_STATS("fetchStats"),
        GET_ANNOTATIONS("getAnnotations"),
        GET_RELEASE_TRACK_HISTORY("getReleaseTrackHistory");

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

    public interface IReleaseTrackHistory {

        void onResult(List<AppRelease> result, @Nullable String nextPageId);

        void onException(Exception ex);
    }

    /**
     * Creates a request to pass in {@link Statistics#fetchStats(StatsRequestBuilder, IResult)}
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

    public class Notifications {

        private Notifications() {
        }

        /**
         * Retrieves a list of {@link Notification}
         *
         * @param timeZone the local {@link TimeZone}
         * @param listener handles the request
         */
        public void listNotifications(TimeZone timeZone, @NonNull final IResult<List<Notification>> listener) {
            try {
                JSONObject params = new JSONObject();
                params.put("1", timeZone.getID());

                basicRequest(Endpoint.NOTIFICATIONS, Method.FETCH, params, new IJSONObject() {
                    @Override
                    public void onResponse(JSONObject resp) throws JSONException, ParseException {
                        final List<Notification> notifications = Notification.toNotificationsList(resp.getJSONObject("result").getJSONArray("1"));
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onResult(notifications);
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
    }

    public class AppReleases {

        private AppReleases() {
        }

        /**
         * See {@link #getReleaseTrackHistory(String, String, IReleaseTrackHistory)}. This will retrieve first 10 items.
         *
         * @param releaseId start release id
         * @param listener  handles the request
         */
        public void getReleaseTrackHistory(String releaseId, @NonNull final IReleaseTrackHistory listener) {
            getReleaseTrackHistory(releaseId, null, listener);
        }

        /**
         * Fetches a list of {@link AppRelease}.
         *
         * @param releaseId  start release id
         * @param nextPageId an id provided from the previous request, if null see {@link #getReleaseTrackHistory(String, IReleaseTrackHistory)}
         * @param listener   handles the request
         */
        public void getReleaseTrackHistory(String releaseId, @Nullable String nextPageId, @NonNull final IReleaseTrackHistory listener) {
            try {
                JSONObject params = new JSONObject();
                params.put("1", releaseId);
                if (nextPageId != null) params.put("2", nextPageId);

                basicRequest(Endpoint.APPRELEASES, Method.GET_RELEASE_TRACK_HISTORY, params, new IJSONObject() {
                    @Override
                    public void onResponse(JSONObject resp) throws JSONException, ParseException {
                        JSONObject result = resp.getJSONObject("result");
                        final String nextPageId = result.optString("2");
                        final List<AppRelease> releases = AppRelease.toAppReleasesList(result.getJSONArray("1"));
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onResult(releases, nextPageId);
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
         * Fetches the release tracks info
         *
         * @param packageName the package name of the desired app
         * @param listener    handles the request
         */
        public void getReleaseTracksSummary(String packageName, @NonNull final IResult<ReleaseTracksSummary> listener) {
            try {
                JSONObject params = new JSONObject();
                params.put("1", packageName);

                basicRequest(Endpoint.APPRELEASES, Method.GET_RELEASE_TRACKS_SUMMARY, params, new IJSONObject() {
                    @Override
                    public void onResponse(JSONObject resp) throws JSONException, ParseException {
                        final ReleaseTracksSummary summary = new ReleaseTracksSummary(resp.getJSONObject("result").getJSONArray("1"));
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onResult(summary);
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
    }

    public class Statistics {

        private Statistics() {
        }

        /**
         * Retrieves a list of {@link Annotation} specified by the package name
         *
         * @param packageName package name of the list of {@link Annotation} to retrieve
         * @param listener    handles the request
         */
        public void listAnnotations(String packageName, @NonNull final IResult<List<Annotation>> listener) {
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

                for (Map.Entry<String, Object> entry : builder.additionalParams.entrySet())
                    actualParams.put(entry.getKey(), entry.getValue());

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
    }

    public class AndroidApps {

        private AndroidApps() {
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

        public void getDetailedAndroidApp(String packageName, @NonNull final IResult<DetailedAndroidApp> listener) {
            try {
                JSONObject params = new JSONObject().put("1", new JSONArray().put(packageName)).put("3", 1);
                basicRequest(Endpoint.ANDROIDAPPS, Method.FETCH, params, new IJSONObject() {
                    @Override
                    public void onResponse(JSONObject resp) throws JSONException {
                        final DetailedAndroidApp app = new DetailedAndroidApp(resp.getJSONObject("result").getJSONArray("1").getJSONObject(0));
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onResult(app);
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

        public void getAppApksHistory(String packageName, @NonNull final IResult<List<Apk>> listener) {
            try {
                JSONObject params = new JSONObject().put("1", new JSONArray().put(packageName)).put("3", 4);
                basicRequest(Endpoint.ANDROIDAPPS, Method.FETCH, params, new IJSONObject() {
                    @Override
                    public void onResponse(JSONObject resp) throws JSONException {
                        final List<Apk> apks = Apk.toApksList(resp.getJSONObject("result").getJSONArray("1").getJSONObject(0).getJSONObject("1").getJSONObject("4").getJSONObject("1").getJSONArray("1"));
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onResult(apks);
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
    }
}

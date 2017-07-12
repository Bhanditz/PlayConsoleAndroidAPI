package com.gianlu.playconsole.api.Models;

import android.content.Context;
import android.support.annotation.NonNull;

import com.gianlu.playconsole.api.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// TODO: Missing 1->17, 6->7, 4, (1->2)
public class App implements Serializable {
    public final String packageName;
    public final String name;
    public final Status status;
    public final String iconUrl;
    public final long lastStoreDetailsUpdate;

    public App(JSONObject obj) throws JSONException {
        JSONObject info = obj.getJSONObject("1");
        packageName = info.getString("1");
        status = Status.parseCode(info.getInt("7"));
        JSONObject timingsObj = info.getJSONObject("11");
        lastStoreDetailsUpdate = timingsObj.getLong("3");

        JSONObject evenMoreInfo = obj.getJSONObject("6");
        name = evenMoreInfo.getString("1");
        iconUrl = evenMoreInfo.getString("3");
    }

    /**
     * Copies another {@link App} (deep copy not required)
     *
     * @param app the copied {@link App}
     */
    public App(App app) {
        packageName = app.packageName;
        name = app.name;
        status = app.status;
        iconUrl = app.iconUrl;
        lastStoreDetailsUpdate = app.lastStoreDetailsUpdate;
    }

    public static List<App> toAndroidAppsList(JSONArray array) throws JSONException {
        List<App> apps = new ArrayList<>();
        for (int i = 0; i < array.length(); i++)
            apps.add(new App(array.getJSONObject(i)));

        return apps;
    }

    // The order of these element is important for sorting
    public enum Status {
        PUBLISHED,
        DRAFT,
        NOT_PUBLISHED;

        private static Status parseCode(int code) {
            switch (code) {
                default:
                case 1:
                    return PUBLISHED;
                case 2:
                    return NOT_PUBLISHED;
                case 5:
                    return DRAFT;
            }
        }

        /**
         * Returns a formal representation of the {@link Status}
         *
         * @param context a {@link Context}
         * @return a formal representation of the {@link Status}
         */
        @NonNull
        public String getFormal(@NonNull Context context) {
            switch (this) {
                case PUBLISHED:
                    return context.getString(R.string.published);
                case NOT_PUBLISHED:
                    return context.getString(R.string.notPublished);
                case DRAFT:
                    return context.getString(R.string.draft);
                default:
                    return context.getString(R.string.unknown);
            }
        }
    }
}

package com.gianlu.playconsole.api.Models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

// TODO: Missing 1->17, 6->7, 4, (1->2)
public class AndroidApp {
    public final String packageName;
    public final String name;
    public final Status status;
    public final String iconUrl;
    public final long lastStoreDetailsUpdate;

    public AndroidApp(JSONObject obj) throws JSONException {
        JSONObject info = obj.getJSONObject("1");
        packageName = info.getString("1");
        status = Status.parseCode(info.getInt("7"));
        JSONObject timingsObj = info.getJSONObject("11");
        lastStoreDetailsUpdate = timingsObj.getLong("3");

        JSONObject evenMoreInfo = obj.getJSONObject("6");
        name = evenMoreInfo.getString("1");
        iconUrl = evenMoreInfo.getString("3");
    }

    public static List<AndroidApp> toAndroidAppsList(JSONArray array) throws JSONException {
        List<AndroidApp> apps = new ArrayList<>();
        for (int i = 0; i < array.length(); i++)
            apps.add(new AndroidApp(array.getJSONObject(i)));

        return apps;
    }

    public enum Status {
        PUBLISHED,
        NOT_PUBLISHED,
        DRAFT;

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
    }
}

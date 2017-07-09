package com.gianlu.playconsole.api.Models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


// TODO: Missing 1, 7, 10, 3 (why is it an array? does apks have different rollout periods?)
public class AppRelease implements Serializable {
    public final String versionName;
    public final String changelog;
    public final String releaseId;
    public final List<ReleaseApk> newApks;
    public final List<ReleaseApk> outdatedApks;

    public AppRelease(JSONObject obj) throws JSONException {
        versionName = obj.getJSONObject("2").getString("1");
        changelog = obj.getString("5");
        releaseId = obj.getString("6");

        newApks = new ArrayList<>();
        outdatedApks = new ArrayList<>();

        JSONObject apksDetails = obj.getJSONObject("4");
        JSONArray newApksArray = apksDetails.getJSONArray("1");
        for (int i = 0; i < newApksArray.length(); i++)
            newApks.add(new ReleaseApk(newApksArray.getJSONObject(i)));

        JSONArray outdatedApksArray = apksDetails.optJSONArray("2");
        if (outdatedApksArray == null) return;
        for (int i = 0; i < outdatedApksArray.length(); i++)
            outdatedApks.add(new ReleaseApk(outdatedApksArray.getJSONObject(i)));
    }

    public static List<AppRelease> toAppReleasesList(JSONArray array) throws JSONException {
        List<AppRelease> apps = new ArrayList<>();
        for (int i = 0; i < array.length(); i++)
            apps.add(new AppRelease(array.getJSONObject(i)));

        return apps;
    }

    // TODO: Missing 3, 9->1, 9->4
    public class ReleaseApk implements Serializable {
        public final long rolloutTimestamp;
        public final String apkId;
        public final String versionName;
        public final int versionCode;
        public final long apkSize;

        public ReleaseApk(JSONObject obj) throws JSONException {
            apkId = obj.getString("1");
            rolloutTimestamp = obj.getLong("2");
            versionCode = obj.getInt("6");
            versionName = obj.getString("7");
            apkSize = obj.getLong("8");
        }
    }
}

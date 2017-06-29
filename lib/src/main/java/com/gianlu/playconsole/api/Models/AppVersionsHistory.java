package com.gianlu.playconsole.api.Models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AppVersionsHistory implements Serializable {
    public final String packageName;
    public final ArrayList<Version> versions;

    public AppVersionsHistory(JSONObject _obj) throws JSONException {
        JSONObject obj = _obj.getJSONObject("1");
        packageName = obj.getString("1");

        JSONArray versionArray = obj.getJSONObject("4").getJSONObject("1").getJSONArray("1");
        versions = new ArrayList<>();
        for (int i = 0; i < versionArray.length(); i++)
            versions.add(new Version(versionArray.getJSONObject(i)));
    }

    public static List<AppVersionsHistory> toAppVersionsHistoriesList(JSONArray array) throws JSONException {
        List<AppVersionsHistory> apps = new ArrayList<>();
        for (int i = 0; i < array.length(); i++)
            apps.add(new AppVersionsHistory(array.getJSONObject(i)));

        return apps;
    }

    // TODO: Missing 3, 5, 6, 10, 12, 16, 2->22, 2->23, 2->26, 2->27, 2->28, 2->32, 2->35, 2->36, 2->37, 2->38, 2->39
    public class Version implements Serializable {
        public final int versionCode;
        public final String versionName;
        public final String[] supportedLanguages;
        public final int minimumSdk;
        public final int targetSdk;
        public final String[] requestedFeatures;
        public final int[] supportedDpis;
        public final String[] supportedScreenLayouts;
        public final String[] requestedPermissions;
        public final long apkSize;
        public final String digestSHA1;
        public final String digestSHA256;

        public Version(JSONObject obj) throws JSONException {
            digestSHA1 = obj.getString("13");
            digestSHA256 = obj.getString("18");

            JSONObject info = obj.getJSONObject("2");
            apkSize = info.getLong("1");
            versionCode = info.getInt("3");
            versionName = info.getString("4");

            JSONArray supportedLanguagesArray = info.getJSONArray("7");
            supportedLanguages = new String[supportedLanguagesArray.length() - 1];
            for (int i = 1; i < supportedLanguagesArray.length(); i++)
                supportedLanguages[i - 1] = supportedLanguagesArray.getString(i);

            minimumSdk = info.getInt("8");
            targetSdk = info.getInt("9");

            JSONArray requestedFeaturesArray = info.getJSONArray("13");
            requestedFeatures = new String[requestedFeaturesArray.length()];
            for (int i = 0; i < requestedFeaturesArray.length(); i++)
                requestedFeatures[i] = requestedFeaturesArray.getString(i);

            JSONArray supportedDpisArray = info.getJSONArray("19");
            supportedDpis = new int[supportedDpisArray.length()];
            for (int i = 0; i < supportedDpisArray.length(); i++)
                supportedDpis[i] = supportedDpisArray.getInt(i);

            JSONArray supportedScreenLayoutsArray = info.getJSONArray("20");
            supportedScreenLayouts = new String[supportedScreenLayoutsArray.length()];
            for (int i = 0; i < supportedScreenLayoutsArray.length(); i++)
                supportedScreenLayouts[i] = supportedScreenLayoutsArray.getString(i);

            JSONArray requestedPermissionsArray = info.getJSONArray("25");
            requestedPermissions = new String[requestedPermissionsArray.length()];
            for (int i = 0; i < requestedPermissionsArray.length(); i++)
                requestedPermissions[i] = requestedPermissionsArray.getJSONObject(i).getString("1");
        }
    }
}

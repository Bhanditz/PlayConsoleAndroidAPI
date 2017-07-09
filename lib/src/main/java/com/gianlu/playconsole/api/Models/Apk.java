package com.gianlu.playconsole.api.Models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Apk implements Serializable {
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

    public Apk(JSONObject obj) throws JSONException {
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

    public static List<Apk> toApksList(JSONArray array) throws JSONException {
        List<Apk> apps = new ArrayList<>();
        for (int i = 0; i < array.length(); i++)
            apps.add(new Apk(array.getJSONObject(i)));

        return apps;
    }
}

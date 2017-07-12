package com.gianlu.playconsole.api.Models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// TODO: Missing 3, 5, 6, 10, 12, 16
public class Apk implements Serializable {
    public final String extendedId;
    public final int versionCode;
    public final String iconUrl;
    public final String versionName;
    public final String packageName;
    public final Locale[] supportedLanguages; // "--_--" is the app default language
    public final int minimumSdk;
    public final int targetSdk;
    public final String[] requestedFeatures;
    public final int[] supportedDpis;
    public final String[] supportedScreenLayouts;
    public final String[] requestedPermissions;
    public final long apkSize;
    public final String digestSHA1;
    public final String digestSHA256;
    public final String appName;

    public Apk(JSONObject obj) throws JSONException {
        extendedId = obj.getString("1");
        digestSHA1 = obj.getString("13");
        digestSHA256 = obj.getString("18");

        JSONObject info = obj.getJSONObject("2");
        apkSize = info.getLong("1");
        packageName = info.getString("2");
        versionCode = info.getInt("3");
        versionName = info.getString("4");
        appName = info.getString("5");
        iconUrl = info.getJSONObject("6").getString("3");

        JSONArray supportedLanguagesArray = info.getJSONArray("7");
        supportedLanguages = new Locale[supportedLanguagesArray.length()];
        for (int i = 0; i < supportedLanguagesArray.length(); i++)
            supportedLanguages[i] = Locale.forLanguageTag(supportedLanguagesArray.getString(i));

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

package com.gianlu.playconsole.api.Models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

// TODO: Missing 3->4, 6->8
public class AppDetails extends App implements Serializable {
    public final long lastApkUpdate;
    public final long published;
    public final long totalRatings;
    public final float averageRating;
    public final long numInstalls;
    public final long numActiveInstalls;
    public final float price;
    public final Currency priceCurrency;

    public AppDetails(JSONObject obj) throws JSONException {
        super(obj);
        JSONObject info = obj.getJSONObject("1");
        JSONObject timingsObj = info.getJSONObject("11");
        lastApkUpdate = timingsObj.getLong("1");
        published = timingsObj.getLong("2");

        JSONObject moreInfo = obj.getJSONObject("3");
        totalRatings = moreInfo.getLong("2");
        averageRating = (float) moreInfo.getDouble("3");
        numInstalls = moreInfo.getLong("5");
        numActiveInstalls = moreInfo.getLong("7");

        JSONObject evenMoreInfo = obj.getJSONObject("6");
        JSONObject priceObj = evenMoreInfo.getJSONObject("5");
        price = (float) priceObj.getDouble("1");
        priceCurrency = Currency.getInstance(priceObj.getString("2"));
    }

    public static List<AppDetails> toDetailedAndroidAppsList(JSONArray array) throws JSONException {
        List<AppDetails> apps = new ArrayList<>();
        for (int i = 0; i < array.length(); i++)
            apps.add(new AppDetails(array.getJSONObject(i)));

        return apps;
    }

    public boolean isFree() {
        return price == 0;
    }
}

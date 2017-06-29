package com.gianlu.playconsole.api.Models;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class RatingStats extends BaseStats<RatingStats.Day> implements Serializable {

    public RatingStats(JSONObject obj) throws JSONException, ParseException {
        super(obj);

        JSONArray daysArray = obj.getJSONArray("1");
        SimpleDateFormat parser = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()); // TODO: Should get Locale as param 3
        for (int i = 0; i < daysArray.length(); i++)
            items.add(new RatingStats.Day(daysArray.getJSONObject(i), parser));
    }

    public class Day implements Serializable {
        public final long timestamp;
        public final long numRatings;
        public final float ratingsAverage;

        Day(JSONObject obj, SimpleDateFormat parser) throws JSONException, ParseException {
            timestamp = parser.parse(obj.getJSONArray("1").getString(0)).getTime();
            JSONArray metrics = obj.getJSONArray("2");
            numRatings = metrics.getJSONObject(0).getLong("1");
            ratingsAverage = (float) metrics.getJSONObject(1).getDouble("2");
        }
    }
}

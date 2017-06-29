package com.gianlu.playconsole.api.Models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class CrashesStats extends BaseStats<CrashesStats.Day> implements Serializable {

    public CrashesStats(JSONObject obj) throws JSONException, ParseException {
        super(obj);

        JSONArray daysArray = obj.getJSONArray("1");
        SimpleDateFormat parser = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()); // TODO: Should get Locale as param 3
        for (int i = 0; i < daysArray.length(); i++)
            items.add(new CrashesStats.Day(daysArray.getJSONObject(i), parser));
    }

    public class Day implements Serializable {
        public final long timestamp;
        public final long numCrashes;

        Day(JSONObject obj, SimpleDateFormat parser) throws JSONException, ParseException {
            timestamp = parser.parse(obj.getJSONArray("1").getString(0)).getTime();
            JSONArray metrics = obj.getJSONArray("2");
            numCrashes = metrics.getJSONObject(0).getLong("1");
        }
    }
}

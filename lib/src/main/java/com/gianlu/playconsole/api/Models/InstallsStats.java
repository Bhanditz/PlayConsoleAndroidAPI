package com.gianlu.playconsole.api.Models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class InstallsStats extends BaseStats<InstallsStats.Day> implements Serializable {
    public InstallsStats(JSONObject obj) throws JSONException, ParseException {
        super(obj);

        JSONArray daysArray = obj.getJSONArray("1");
        SimpleDateFormat parser = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()); // TODO: Should get Locale as param 3
        for (int i = 0; i < daysArray.length(); i++)
            items.add(new Day(daysArray.getJSONObject(i), parser));
    }

    public class Day implements Serializable {
        public final long timestamp;
        public final long installs;
        public final long uninstalls;

        Day(JSONObject obj, SimpleDateFormat parser) throws JSONException, ParseException {
            timestamp = parser.parse(obj.getJSONArray("1").getString(0)).getTime();
            JSONArray metrics = obj.getJSONArray("2");
            installs = metrics.getJSONObject(0).getLong("1");
            uninstalls = metrics.getJSONObject(1).getLong("1");
        }
    }
}

package com.gianlu.playconsole.api.Models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class Stats {
    public final TimeZone timezone;
    public final List<Entry> entries;

    public Stats(JSONObject obj) throws JSONException {
        timezone = TimeZone.getTimeZone(obj.getString("3"));

        entries = new ArrayList<>();
        JSONArray entriesArray = obj.getJSONArray("1");
        for (int i = 0; i < entriesArray.length(); i++)
            entries.add(new Entry(entriesArray.getJSONObject(i)));
    }

    public SimpleDateFormat getDateParser() {
        SimpleDateFormat parser = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        parser.setTimeZone(timezone);
        return parser;
    }

    public boolean hasDimension()  {
        return entries.size() > 1;
    }

    public Entry firstEntry() {
        return entries.get(0);
    }

    public class Entry {
        public final JSONObject obj;

        public Entry(JSONObject obj) {
            this.obj = obj;
        }
    }
}

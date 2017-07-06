package com.gianlu.playconsole.api.Models;

import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class Stats {
    public final TimeZone timezone;
    public final List<Entry> entries;

    public Stats(JSONObject obj) throws JSONException {
        timezone = TimeZone.getTimeZone(obj.getString("3"));

        entries = new ArrayList<>();
        JSONArray entriesArray = obj.optJSONArray("1");
        if (entriesArray == null) return; // There are no entries, it's not an error!
        for (int i = 0; i < entriesArray.length(); i++)
            entries.add(new Entry(entriesArray.getJSONObject(i)));
    }

    /**
     * Sorts entries evaluating their {@link Entry#value}.
     * It is not recommended to run this on date based entries.
     */
    public void sortEntries() {
        Collections.sort(entries, new EntryValueComparator());
    }

    public SimpleDateFormat getDateParser() {
        SimpleDateFormat parser = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        parser.setTimeZone(timezone);
        return parser;
    }

    public boolean hasDimension() {
        return entries.size() > 1;
    }

    public static class EntryValueComparator implements Comparator<Entry> {
        @Override
        public int compare(Entry o1, Entry o2) {
            if (o1.value == null && o2.value != null) return 1;
            else if (o1.value != null && o2.value == null) return -1;
            else if (o1.value == null /* && o2.value == null */) return 0;

            float diff = o1.value - o2.value;
            return diff == 0.0f ? 0 : (diff > 0.0f ? -1 : 1);
        }
    }

    public class Entry {
        public final JSONObject obj;
        public final String key;
        public final String keyDescription;
        public final Float value;

        public Entry(JSONObject obj) {
            this.obj = obj;
            this.key = optKey();
            this.keyDescription = optKeyDescription();
            this.value = optValue();
        }

        /**
         * Return the key of the entry (as it's usually)
         *
         * @return the key
         * @throws JSONException if the value cannot be found
         */
        public String getKey() throws JSONException {
            return obj.getJSONArray("1").getString(0);
        }

        @Nullable
        public String optKey() {
            try {
                return getKey();
            } catch (JSONException ex) {
                return null;
            }
        }

        /**
         * Return the formal representation of key of the entry (as it's usually)
         *
         * @return the key
         * @throws JSONException if the value cannot be found
         */
        public String getKeyDescription() throws JSONException {
            return obj.getJSONArray("4").getString(0);
        }

        @Nullable
        public String optKeyDescription() {
            try {
                return getKeyDescription();
            } catch (JSONException ex) {
                return null;
            }
        }

        /**
         * Return the value of the entry (as it's usually)
         *
         * @return the value
         * @throws JSONException    if the response is invalid (or unexpected)
         * @throws RuntimeException if the value cannot be found
         */
        public float getValue() throws JSONException, RuntimeException {
            JSONObject data = obj.getJSONArray("2").getJSONObject(0);
            if (data.has("1")) return data.getLong("1");
            else if (data.has("2")) return (float) data.getDouble("2");
            else throw new RuntimeException("Cannot find value!");
        }

        @Nullable
        public Float optValue() {
            try {
                return getValue();
            } catch (JSONException | RuntimeException ex) {
                return null;
            }
        }
    }
}

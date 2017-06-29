package com.gianlu.playconsole.api.Models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class CountriesStats extends BaseStats<CountriesStats.Country> implements Serializable {

    public CountriesStats(JSONObject obj) throws JSONException {
        super(obj);

        JSONArray daysArray = obj.getJSONArray("1");
        for (int i = 0; i < daysArray.length(); i++)
            items.add(new Country(daysArray.getJSONObject(i)));
    }

    public class Country implements Serializable {
        public final String countryCode;
        public final String countryName;
        public final long installs;

        Country(JSONObject obj) throws JSONException {
            countryCode = obj.getJSONArray("1").getString(0);
            countryName = obj.getJSONArray("4").getString(0);

            installs = obj.getJSONArray("2").getJSONObject(0).getLong("1");
        }
    }
}

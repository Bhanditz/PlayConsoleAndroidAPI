package com.gianlu.playconsole.api.Models;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class BaseStats<E> implements Serializable {
    public final long lastDay;
    public final ArrayList<E> items;

    public BaseStats(JSONObject obj) throws JSONException {
        lastDay = obj.getLong("2");
        items = new ArrayList<>();
    }
}

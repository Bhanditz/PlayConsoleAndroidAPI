package com.gianlu.playconsole.api.Models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// TODO: Missing 2->5
public class Annotation implements Serializable {
    public final long timestamp;
    public final Type type;
    public final String desc;

    public Annotation(JSONObject obj) throws JSONException {
        timestamp = obj.getLong("1");
        JSONObject info = obj.getJSONObject("2");
        desc = info.getString("3");
        type = Type.parse(info.getInt("4"));
    }

    public static List<Annotation> toAnnotationsList(JSONArray array) throws JSONException {
        List<Annotation> apps = new ArrayList<>();
        for (int i = 0; i < array.length(); i++)
            apps.add(new Annotation(array.getJSONObject(i)));

        return apps;
    }

    public enum Type {
        RELEASE;

        public static Type parse(int val) {
            switch (val) {
                default:
                case 1:
                    return RELEASE;
            }
        }
    }
}

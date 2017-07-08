package com.gianlu.playconsole.api.Models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// TODO: Missing 2, 3->3->2
public class Notification {
    public final String id;
    public final String shortMessage;
    public final String longMessage;
    public final String forApp;
    public final Type type;
    public final long timestamp;
    public final List<Action> actions;

    public Notification(JSONObject obj) throws JSONException {
        id = obj.getString("1");
        timestamp = obj.getLong("4");

        JSONObject notifInfo = obj.getJSONObject("3");
        JSONObject notifBody = notifInfo.getJSONObject("2");
        shortMessage = notifBody.getJSONObject("1").getString("2");
        longMessage = notifBody.getJSONObject("2").getString("2");

        JSONObject notifData = notifInfo.getJSONObject("3");
        forApp = notifData.getString("1");
        type = Type.parse(notifData.getInt("3"));

        actions = new ArrayList<>();
        JSONArray actions = notifData.optJSONArray("4");
        if (actions != null) {
            for (int i = 0; i < actions.length(); i++)
                this.actions.add(new Action(actions.getJSONObject(i)));
        }
    }

    public static List<Notification> toNotificationsList(JSONArray array) throws JSONException {
        List<Notification> notifications = new ArrayList<>();
        for (int i = 0; i < array.length(); i++)
            notifications.add(new Notification(array.getJSONObject(i)));

        return notifications;
    }

    public static List<Notification> filterFor(List<Notification> notifications, String packageName) {
        List<Notification> filtered = new ArrayList<>();
        for (Notification notif : notifications)
            if (Objects.equals(notif.forApp, packageName))
                filtered.add(notif);

        return filtered;
    }

    public enum Type {
        UPDATED_REVIEW,
        NEW_REVIEW,
        PUBLISHED_UPDATE;

        public static Type parse(int val) {
            switch (val) {
                case 2:
                    return UPDATED_REVIEW;
                case 8:
                    return PUBLISHED_UPDATE;
                case 14:
                    return NEW_REVIEW;
                default:
                    throw new IllegalArgumentException("Unknown notification type: " + val);
            }
        }
    }

    // TODO: Missing 4
    public class Action {
        public final String actionUrl;
        public final String name;

        public Action(JSONObject obj) throws JSONException {
            actionUrl = obj.getJSONObject("1").getString("3");
            name = obj.getString("2");
        }
    }
}

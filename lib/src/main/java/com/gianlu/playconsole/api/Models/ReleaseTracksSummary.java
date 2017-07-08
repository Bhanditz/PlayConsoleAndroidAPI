package com.gianlu.playconsole.api.Models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ReleaseTracksSummary {
    public final ReleaseTrack production;
    public final ReleaseTrack beta;
    public final ReleaseTrack alpha;

    public ReleaseTracksSummary(JSONArray tracks) throws JSONException {
        production = new ReleaseTrack(tracks.getJSONObject(0));
        beta = new ReleaseTrack(tracks.getJSONObject(1));
        alpha = new ReleaseTrack(tracks.getJSONObject(2));
    }

    public enum Track {
        PRODUCTION,
        BETA,
        ALPHA;

        public static Track parse(int val) {
            switch (val) {
                case 1:
                    return PRODUCTION;
                case 4:
                    return BETA;
                case 5:
                    return ALPHA;
                default:
                    throw new IllegalArgumentException("Unknown track type: " + val);
            }
        }
    }

    // TODO: Missing 2, 6, 8, 5->2->3, 5->2->4, 7->1, 7->3
    public class ReleaseTrack {
        public final Track which;
        public final boolean fullRollout;
        public final long rolloutTimestamp;
        public final String versionName;
        public final int versionCode;

        public ReleaseTrack(JSONObject obj) throws JSONException {
            which = Track.parse(obj.getInt("1"));

            JSONObject releaseInfo = obj.optJSONObject("5");
            if (releaseInfo != null) {
                versionName = releaseInfo.getJSONObject("1").getString("1");
                versionCode = releaseInfo.getJSONArray("3").getInt(0);

                JSONObject rolloutInfo = releaseInfo.getJSONObject("2");
                fullRollout = rolloutInfo.getInt("1") == 1;
                rolloutTimestamp = rolloutInfo.getLong("2");
            } else {
                fullRollout = false;
                rolloutTimestamp = -1;
                versionName = null;
                versionCode = -1;
            }
        }
    }
}

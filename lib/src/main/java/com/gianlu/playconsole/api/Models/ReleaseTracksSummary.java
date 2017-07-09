package com.gianlu.playconsole.api.Models;

import android.content.Context;

import com.gianlu.playconsole.api.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ReleaseTracksSummary implements Serializable {
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

        public String getFormal(Context context) {
            switch (this) {
                case PRODUCTION:
                    return context.getString(R.string.production);
                case BETA:
                    return context.getString(R.string.beta);
                case ALPHA:
                    return context.getString(R.string.alpha);
                default:
                    return context.getString(R.string.unknown);
            }
        }
    }

    public enum Status {
        /**
         * The release track has never been used
         */
        EMPTY,

        /**
         * The version code of the release track APK is old
         */
        OUTDATED,

        /**
         * The version code of the release track APK is the latest
         */
        NEWER
    }

    // TODO: Missing 5->2->3, 5->2->4, 7->1, 7->3
    public class ReleaseTrack implements Serializable {
        public final Track which;
        public final String trackId;
        public final Status status;
        public final boolean fullRollout;
        public final long rolloutTimestamp;
        public final String versionName;
        public final String lastReleaseId;
        public final List<Integer> versionCodes;
        public final List<String> apkIds;

        public ReleaseTrack(JSONObject obj) throws JSONException {
            which = Track.parse(obj.getInt("1"));
            trackId = obj.getString("2");
            versionCodes = new ArrayList<>();
            apkIds = new ArrayList<>();

            JSONObject releaseInfo = obj.optJSONObject("5");
            JSONObject someData = obj.optJSONObject("7");
            if (releaseInfo == null) {
                if (someData == null) status = Status.EMPTY;
                else status = Status.OUTDATED;

                fullRollout = false;
                rolloutTimestamp = -1;
                versionName = null;
                lastReleaseId = null;
            } else {
                versionName = releaseInfo.getJSONObject("1").getString("1");
                lastReleaseId = releaseInfo.getString("6");

                JSONArray versionCodesArray = releaseInfo.getJSONArray("3");
                for (int i = 0; i < versionCodesArray.length(); i++)
                    versionCodes.add(versionCodesArray.getInt(i));

                JSONArray apkIdsArray = releaseInfo.getJSONArray("8");
                for (int i = 0; i < apkIdsArray.length(); i++)
                    apkIds.add(apkIdsArray.getString(i));

                JSONObject rolloutInfo = releaseInfo.getJSONObject("2");
                fullRollout = rolloutInfo.getInt("1") == 1;
                rolloutTimestamp = rolloutInfo.getLong("2");

                if (someData.optInt("3", -1) == 1) status = Status.OUTDATED;
                else status = Status.NEWER;
            }
        }

        public boolean hasRolloutDetails() {
            return versionName != null;
        }
    }
}

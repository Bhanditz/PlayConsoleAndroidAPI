package com.gianlu.playconsole.api.Models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

// TODO: Missing 1->(2->2, 2->4, 3, 4->1->5, 8, 10, 11, 12, 13, 16, 17), 7, 9
public class AppStoreDetails implements Serializable {
    public final String packageName;
    public final ArrayList<LocalizedProductDetails> productDetails;
    public final String contactEmail;
    public final ArrayList<Apk> apks;
    public final ArrayList<Release> releases;
    public final ContentRating contentRating;

    public AppStoreDetails(JSONObject obj) throws JSONException {
        JSONObject info = obj.getJSONObject("1");

        packageName = info.getString("1");
        JSONObject productStoreInfo = info.getJSONObject("2");

        productDetails = new ArrayList<>();
        JSONArray productDetailsArray = productStoreInfo.getJSONArray("1");
        for (int i = 0; i < productDetailsArray.length(); i++)
            productDetails.add(new LocalizedProductDetails(productDetailsArray.getJSONObject(i)));

        contactEmail = productStoreInfo.getJSONObject("3").getString("2");

        JSONObject releasesInfo = info.getJSONObject("4");
        releases = new ArrayList<>();
        JSONArray releasesArray = releasesInfo.getJSONObject("2").getJSONArray("1");
        for (int i = 0; i < releasesArray.length(); i++)
            releases.add(new Release(releasesArray.getJSONObject(i)));

        apks = new ArrayList<>();
        JSONArray apksArray = releasesInfo.getJSONObject("1").getJSONArray("1");
        for (int i = 0; i < apksArray.length(); i++)
            apks.add(new Apk(apksArray.getJSONObject(i)));

        contentRating = new ContentRating(info.getJSONObject("14"));
    }

    // TODO: Missing types
    public enum ScreenshotType {
        PHONE,
        TABLET,
        ANDROID_TV,
        ANDROID_WEAR;

        public static ScreenshotType parse(int val) {
            switch (val) {
                case 1:
                    return PHONE;
                default:
                    throw new IllegalArgumentException("Unknown screenshot type: " + val);
            }
        }
    }

    public enum RatingProvider {
        CLASS_IND,
        ESRB,
        PEGI,
        USK,
        IARC,
        GOOGLE_PLAY;

        public static RatingProvider parse(int val) {
            switch (val) {
                case 1:
                    return GOOGLE_PLAY;
                case 2:
                    return IARC;
                case 4:
                    return CLASS_IND;
                case 5:
                    return ESRB;
                case 6:
                    return PEGI;
                case 7:
                    return USK;
                default:
                    throw new IllegalArgumentException("Unknown rating provider: " + val);
            }
        }
    }

    public class Release implements Serializable {
        public final int versionCode;
        public final ArrayList<LocalizedChangelog> changelogs;

        public Release(JSONObject obj) throws JSONException {
            versionCode = obj.getInt("1");

            changelogs = new ArrayList<>();
            JSONArray changelogsArray = obj.optJSONArray("2");
            if (changelogsArray == null) return;
            for (int i = 0; i < changelogsArray.length(); i++)
                changelogs.add(new LocalizedChangelog(changelogsArray.getJSONObject(i)));
        }

        public class LocalizedChangelog implements Serializable {
            public final String locale;
            public final String changelog;

            public LocalizedChangelog(JSONObject obj) throws JSONException {
                locale = obj.getString("1");
                changelog = obj.getString("3");
            }
        }
    }

    // TODO: Missing 1, 2, 6, 7
    public class ContentRating implements Serializable {
        public final String IARCCertificateId;
        public final long submittedAt;
        public final ArrayList<Rating> ratings;

        public ContentRating(JSONObject obj) throws JSONException {
            IARCCertificateId = obj.getString("5");
            submittedAt = obj.getLong("3");

            ratings = new ArrayList<>();
            JSONArray ratingsArray = obj.getJSONObject("4").getJSONArray("1");
            for (int i = 0; i < ratingsArray.length(); i++)
                ratings.add(new Rating(ratingsArray.getJSONObject(i)));
        }

        // TODO: Missing 5
        public class Rating implements Serializable {
            public final RatingProvider provider;
            public final int ratingType; // I can't list them all, maybe in the future

            public Rating(JSONObject obj) throws JSONException {
                provider = RatingProvider.parse(obj.getInt("1"));
                ratingType = obj.getInt("2");
            }
        }
    }

    public class LocalizedProductDetails implements Serializable {
        public final String locale;
        public final String title;
        public final String shortDescription;
        public final String fullDescription;
        public final String translatedFrom;
        public final String iconUrl;
        public final String bannerUrl;
        public final ArrayList<Screenshot> screenshots;

        public LocalizedProductDetails(JSONObject obj) throws JSONException {
            locale = obj.getString("1");
            title = obj.getString("2");
            shortDescription = obj.getString("3");
            fullDescription = obj.getString("4");

            if (obj.has("6")) translatedFrom = obj.getString("6");
            else translatedFrom = null;

            screenshots = new ArrayList<>();

            if (obj.has("7")) {
                JSONObject graphic = obj.getJSONObject("7");
                iconUrl = graphic.getJSONObject("2").getString("2");
                bannerUrl = graphic.getJSONObject("3").getString("2");

                JSONArray screenshotsArray = graphic.getJSONArray("1");
                for (int i = 0; i < screenshotsArray.length(); i++)
                    screenshots.add(new Screenshot(screenshotsArray.getJSONObject(i)));
            } else {
                iconUrl = null;
                bannerUrl = null;
            }
        }

        public class Screenshot implements Serializable {
            public final ScreenshotType type;
            public final String url;

            public Screenshot(JSONObject obj) throws JSONException {
                type = ScreenshotType.parse(obj.getInt("5"));
                url = obj.getString("2");
            }
        }
    }
}

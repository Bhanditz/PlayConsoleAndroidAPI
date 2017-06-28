package com.gianlu.playconsole.api;

import android.net.Uri;
import android.webkit.CookieManager;

import com.gianlu.playconsole.api.Models.StartupData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.client.CookieStore;
import cz.msebera.android.httpclient.impl.client.BasicCookieStore;
import cz.msebera.android.httpclient.impl.cookie.BasicClientCookie;

public class Utils {
    public static final String DEVELOPER_CONSOLE_URL = "https://play.google.com/apps/publish/";
    private static final Pattern GWT_MODULE_BASE_PATTERN = Pattern.compile("<meta name=\"gwt:property\" content=\"baseUrl=(.*?)\">");
    private static final Pattern STARTUP_DATA_PATTERN = Pattern.compile("startupData = (\\{.*?\\});");

    public static String extractGwtModuleBase(String html) throws HtmlParsingException {
        Matcher matcher = GWT_MODULE_BASE_PATTERN.matcher(html);
        if (matcher.find()) return matcher.group(1);
        else throw new HtmlParsingException("Cannot find GWT module base!");
    }

    public static String buildConsoleUrl(String dev_acc) {
        return DEVELOPER_CONSOLE_URL + "?dev_acc" + dev_acc;
    }

    public static StartupData extractStartupData(String html) throws HtmlParsingException, JSONException {
        Matcher matcher = STARTUP_DATA_PATTERN.matcher(html);
        if (matcher.find()) return new StartupData(new JSONObject(matcher.group(1)));
        else throw new HtmlParsingException("Cannot find startupData!");
    }

    public static String extractGwtPermutation(String html, String gwtModuleBase) throws HtmlParsingException {
        Matcher matcher = Pattern.compile("<script defer src=\"" + gwtModuleBase + "(.{32})\\.cache\\.js\"></script>").matcher(html);
        if (matcher.find()) return matcher.group(1);
        else throw new HtmlParsingException("Cannot find GWT permutation!");
    }

    public static CookieStore extractCookies(Uri url) {
        String rawCookies = CookieManager.getInstance().getCookie(url.toString());
        CookieStore store = new BasicCookieStore();
        if (rawCookies == null || rawCookies.isEmpty()) return store;

        for (String rawCookie : rawCookies.split(";")) {
            String[] split = rawCookie.split("=");
            if (split.length < 2) continue;

            BasicClientCookie cookie = new BasicClientCookie(split[0].trim(), split[1].trim());
            cookie.setDomain(url.getHost());
            store.addCookie(cookie);
        }

        return store;
    }
}

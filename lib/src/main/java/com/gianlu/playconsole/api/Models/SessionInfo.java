package com.gianlu.playconsole.api.Models;


import java.io.Serializable;

import cz.msebera.android.httpclient.client.CookieStore;

public class SessionInfo implements Serializable {
    public static final String contentType = "application/javascript";
    public final StartupData startupData;
    public final String xGwtModuleBase;
    public final String xGwtPermutation;
    public final CookieStore cookieStore;

    public SessionInfo(StartupData startupData, String xGwtModuleBase, String xGwtPermutation, CookieStore cookieStore) {
        this.startupData = startupData;
        this.xGwtModuleBase = xGwtModuleBase;
        this.xGwtPermutation = xGwtPermutation;
        this.cookieStore = cookieStore;
    }
}

package com.gianlu.playconsole.api.Models;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class StartupData implements Serializable {
    public final String xsrfToken;
    public final DeveloperAccount account;

    public StartupData(JSONObject startupData) throws JSONException {
        xsrfToken = new JSONObject(startupData.getString("XsrfToken")).getString("1");
        account = new DeveloperAccount(new JSONObject(startupData.getString("DeveloperConsoleAccounts")).getJSONArray("1").getJSONObject(0));
    }

    public class DeveloperAccount implements Serializable {
        public final String accountCode;
        public final String accountName;
        public final String email;
        public final String imageUrl;

        public DeveloperAccount(JSONObject account) throws JSONException {
            accountCode = account.getString("1");
            accountName = account.getString("2");
            email = account.getString("3");
            imageUrl = account.getString("4");
        }
    }
}

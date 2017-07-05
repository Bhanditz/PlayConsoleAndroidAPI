package com.gianlu.playconsole.api.Models;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class StartupData implements Serializable {
    public final String xsrfToken;
    public final DeveloperAccount account;
    public final UserDetails details;

    public StartupData(JSONObject startupData) throws JSONException {
        xsrfToken = new JSONObject(startupData.getString("XsrfToken")).getString("1");
        account = new DeveloperAccount(new JSONObject(startupData.getString("DeveloperConsoleAccounts")).getJSONArray("1").getJSONObject(0));
        details = new UserDetails(new JSONObject(startupData.getString("UserDetails")));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StartupData that = (StartupData) o;
        return xsrfToken.equals(that.xsrfToken) && account.equals(that.account);
    }

    public class UserDetails implements Serializable {
        public final String currency;

        UserDetails(JSONObject details) throws JSONException {
            currency = details.getString("2");
        }
    }

    public class DeveloperAccount implements Serializable {
        public final String accountCode;
        public final String accountName;
        public final String email;
        public final String imageUrl;

        DeveloperAccount(JSONObject account) throws JSONException {
            accountCode = account.getString("1");
            accountName = account.getString("2");
            email = account.getString("3");
            imageUrl = account.getString("4");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DeveloperAccount that = (DeveloperAccount) o;
            return accountCode.equals(that.accountCode) && email.equals(that.email);
        }
    }
}

package com.gianlu.playconsole.api;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.client.methods.HttpPost;

public class PlayConsoleRequest {
    final HttpPost request;
    final JSONObject body;

    private PlayConsoleRequest(HttpPost request, JSONObject body) {
        this.request = request;
        this.body = body;
    }

    public static class Builder {
        private String url;
        private JSONObject body;

        public Builder() {
            body = new JSONObject();
        }

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder setRequestMethod(String method) {
            try {
                body.put("method", method);
                return this;
            } catch (JSONException ex) {
                throw new RuntimeException(ex);
            }
        }

        public Builder setRequestParams(String params) {
            try {
                body.put("params", params);
                return this;
            } catch (JSONException ex) {
                throw new RuntimeException(ex);
            }
        }

        public PlayConsoleRequest build() {
            if (url == null) throw new IllegalArgumentException("an url must be specified!");
            return new PlayConsoleRequest(new HttpPost(url), body);
        }
    }
}

package com.gianlu.playconsole.api.Exceptions;

import android.support.annotation.Nullable;

import org.json.JSONObject;

public class PlayConsoleException extends Exception {
    private final JSONObject errorData;

    public PlayConsoleException(JSONObject error) {
        super("Play Console error: #" + error.optInt("code", -1));
        this.errorData = error.optJSONObject("data");
    }

    @Nullable
    public JSONObject getErrorData() {
        return errorData;
    }
}

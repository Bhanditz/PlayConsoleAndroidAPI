package com.gianlu.playconsole.api;

import cz.msebera.android.httpclient.StatusLine;

public class GeneralException extends Exception {
    public GeneralException(StatusLine sl) {
        super(sl.getStatusCode() + ": " + sl.getReasonPhrase());
    }

    public GeneralException(String message) {
        super(message);
    }
}
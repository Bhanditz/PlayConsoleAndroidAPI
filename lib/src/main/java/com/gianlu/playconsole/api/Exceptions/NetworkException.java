package com.gianlu.playconsole.api.Exceptions;

import cz.msebera.android.httpclient.StatusLine;

public class NetworkException extends Exception {
    public NetworkException(StatusLine sl) {
        super(sl.getStatusCode() + ": " + sl.getReasonPhrase());
    }
}

# Play-Console-Android-API

This is an Android API for the Google Play Developer Console which goes beyond the limits of the Android Publisher API (https://developers.google.com/android-publisher/).


# Testing

Run the demo app, login with your developer Google account and then press the test button. There you can try authenticate and send different requests. The body of the request must be a valid JSON object without the xsrf field.


# TODOs
* Do some test to ensure that the authentication method is valid and stable
* List all available API endpoints
* Output more sensible reponses
* Background authentication (without showing the WebView)
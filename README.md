# Play-Console-Android-API
This is an Android API for the Google Play Developer Console which goes beyond the limits of the Android Publisher API (https://developers.google.com/android-publisher/). 

# How it works
This API requires the user to login into his Google account from the app. The password isn't stored and the API will never know it as the login is done on a WebView. The API also support silent authentication (without showing the WebView) once the user logged at least once.

# Testing
Run the demo app, login with your developer Google account and then press the test button. There you can try authenticate and send different requests. The body of the request must be a valid JSON object without the xsrf field.

# TODOs
* List all available API endpoints (working on right now)
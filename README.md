
# 1Direction

[![Build Status](https://api.cirrus-ci.com/github/Sosol02/sdp.svg)](https://cirrus-ci.com/github/Sosol02/sdp)
[![Maintainability](https://api.codeclimate.com/v1/badges/db5bd17d55b87c54a634/maintainability)](https://codeclimate.com/github/Sosol02/sdp/maintainability)
[![Test Coverage](https://api.codeclimate.com/v1/badges/db5bd17d55b87c54a634/test_coverage)](https://codeclimate.com/github/Sosol02/sdp/test_coverage)

# Production Database
To use the production database instead of the firebase emulator (used among others by Cirrus),
you need to change the value of "USE_PROD_DB" to "true" in [build.gradle](https://github.com/Sosol02/sdp/blob/main/app/build.gradle) (line 37).
Otherwise, you **need** to have the firebase emulator running on the side (at least the firestore emulator is required).

# Google calendar interoperability
Google requires the the app to be signed to allow requests.
As such, if you want to use this functionality, contact me (@Ef55) so that I can add your build to the allowed apps
(The release APK should be signed). 

# Navigation
The navigation is sadly only available in the USA.

# API keys / Secrets
You should add 
```MAPBOX_DOWNLOADS_TOKEN=<token>``` and ```MAPQUEST_API_TOKEN=<token>```
at the end of [local.properties](https://github.com/Sosol02/sdp/blob/main/local.properties).
If you also want to run the tests yourself, you should add the file `app/src/main/res/values/test_secrets.xml`.

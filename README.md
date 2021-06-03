
# 1Direction

[![Build Status](https://api.cirrus-ci.com/github/Sosol02/sdp.svg)](https://cirrus-ci.com/github/Sosol02/sdp)
[![Maintainability](https://api.codeclimate.com/v1/badges/db5bd17d55b87c54a634/maintainability)](https://codeclimate.com/github/Sosol02/sdp/maintainability)
[![Test Coverage](https://api.codeclimate.com/v1/badges/db5bd17d55b87c54a634/test_coverage)](https://codeclimate.com/github/Sosol02/sdp/test_coverage)

# Production Database
To use the production database instead of the emulator use among others by Cirrus,
you need to change the value of "USE_PROD_DB" to "true" in the app build.gradle.

# Google calendar interoperability
Google requires the the app to be signed to allow requests.
As such, if you want to use this functionality, contact me (@Ef55) so that I add your build to the allowed apps
(The final APK should be signed). 
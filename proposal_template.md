# SDP Proposal - Team \#

## Team / App name:
TODO

## Description:
- Allow users to take 360° spherical pictures.
    - Made using a collage of multiple pictures.
- These images are geotagged and uploaded to our database.
- A google maps integration displays the images that people took on locations.
- A 3D viewer that uses the accelerator to see it in 3D.

## Requirements:
### Split app model: 
- Google logins
- Google Maps

### Sensor usage:
- Accelerometer for 3D display
- GPS / localization for picture geo-tagging.
- Camera

### User support:
TODO: what will a user be able to do ? Are there advantages to being logged in ? Will the content be personalized for each user ? How ?
- Take 3D pictures by taking a few pictures and collaging them together
- Login advantages:
    - Upload pictures
    - Report inappropriate pictures

### Local cache:
TODO: what content will be cached locally ?
- The images the user took, the local map, the images the user looked at recently.

### Offline mode:
TODO: which basic functionalities will your offline mode support ?
- Looking at images recently seen, take 3D images

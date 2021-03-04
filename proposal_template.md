# SDP Proposal - Team \#8

## Team / App name:
1Direction

## Description:
Universal scheduler: given a list of things (fixed in time or not) to do/go/spend time at, 
generate a planning taking opening times, durations and so on into account.

## Requirements:
### Split app model: 
- Google Map/OpenStreetMap for getting locations and opening times

### Sensor usage:
- GPS for location : track in real time the location of the user (a button will allow the user to disable this funcionnality)

### User support:
The user can input, using both a calendar and direct inputs, a list of things to do, and will get
a planning generated automatically. The user can add elements to the planning later-on and the app will generate on the fly the updated planning.
The apllication may send reminders regarding the planning, or export it (e.g. google calendar).
A logged in user may synchronize the planning with other devices.
Eventually, he might as well do collaborative planning

### Local cache:
Everything related to the planning is logged locally, so that it is available offline as well.

### Offline mode:
The user might still set-up events or alterate the planning, but no new itinerary can be computed.
Of course, no synchronization can be done while offline.

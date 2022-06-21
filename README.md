# ClickToRun, A Simple Fitness Tracker App
<img src="https://github.com/Coeeter/ClickToRun/blob/master/app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png?raw=true" align="left">
ClickToRun is an android native app using kotlin with MVVM architecture with firebase as the backend. This application is being created for my modules in my studies at Temasek Polytechnic to show my proficiency over native android app development with kotlin as the frontend language.
<br clear="left"/>

## The Dependencies used to build the app so far are:
- Material Design for easier designing of Views
- Dagger hilt for dependency injection
- Room database for storing the Runs recorded
- Google Play Services to use Google Maps and Location services
- Navigation Component for easier fragment usage
- Coroutine extensions for easier asynchronous coding
- Lifecycle extensions for MVVM architecture

## The features implemented into ClickToRun so far are:
- User authentication using Firebase Authentication
- Collected details of user and storing in firestore
- Tracking runs using a foreground service
- Saving runs to Local Database
- Dark Mode and Landscape compatible

## The features which I hope to integrate into ClickToRun soon are:
- Updating and deleting of data
- Steps counter, to count how many steps during run
- An Explore page, where Users can share their runs to other users
- A chats function where users can chat with each other

## Installation:
### If you want to try using the app ensure you add the line below by using your own Google Maps API key
```
GOOGLE_MAPS_API_KEY=YOUR_API_KEY
```

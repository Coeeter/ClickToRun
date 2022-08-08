# ClickToRun, A Simple Fitness Tracker App
<img src="https://github.com/Coeeter/ClickToRun/blob/master/app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png?raw=true" align="left">
ClickToRun is an android native app built using kotlin with MVVM architecture with firebase as the backend. This application is being created for my modules in my studies at Temasek Polytechnic to show my proficiency in native android app development with kotlin as the frontend language.
<br clear="left"/>

## The Dependencies used to build the app so far are:
- Material Design for easier designing of Views
- Dagger hilt for dependency injection
- Room database for storing the Runs recorded
- Google Play Services to use Google Maps and Location services
- Navigation Component for easier fragment usage
- Coroutine extensions for easier asynchronous coding
- Lifecycle extensions for MVVM architecture

## The features implemented into ClickToRun:
- User authentication using Firebase Authentication
- Collected details of user and storing in firestore
- Tracking runs using a foreground service
- Saving runs to Local Database
- Dark Mode and Landscape compatible
- An explore page, where users can share their runs to other users
- An user profile screen to see posts, followers and who user is following

## Demonstration
You can watch the demo of this app [here](https://youtu.be/LX3llOyXfSU)

## Installation:
If you want to try using the app, you can download it as a zip folder from [here](https://github.com/Coeeter/ClickToRun) or clone it from [here](https://github.com/Coeeter/ClickToRun.git). When running the app ensure you add the line below by using your own Google Maps API key to the `local.properties` file
```properties
GOOGLE_MAPS_API_KEY = YOUR_API_KEY
```

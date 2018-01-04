# toggl-things-tracker
Android Things app for tracking your workday via Toggl. Displays today's worked time and plays a well-known chime after 8 hours to remind you to wrap things up.

# Prerequisites

- Android Things compatible board (see https://developer.android.com/things/hardware/index.html: Raspberry Pi 3, NXP Pico i.MX7D, NXP Pico i.MX6UL)
- optional (but strongly recommended, the code will run without, but you won't see anything. Consider using a Pimoroni Rainbow HAT.): 
    - buzzer
    - alphanumeric displays

# Setup

0. We'll assume you already have a Rainbow HAT assembled on the board of your choice and Android Things flashed. 
1. Add a `gradle.properties` file (if not yet existent after importing into Android Studio) to the root of the project and configure Toggl. You can use the following template:

```
# Project-wide Gradle settings.

# IDE (e.g. Android Studio) users:
# Gradle settings configured through the IDE *will override*
# any settings specified in this file.

# For more details on how to configure your build environment visit
# http://www.gradle.org/docs/current/userguide/build_environment.html

# Specifies the JVM arguments used for the daemon process.
# The setting is particularly useful for tweaking memory settings.
org.gradle.jvmargs=-Xmx1536m

# When configured, Gradle will run in incubating parallel mode.
# This option should only be used with decoupled projects. More details, visit
# http://www.gradle.org/docs/current/userguide/multi_project_builds.html#sec:decoupled_projects
# org.gradle.parallel=true

# The toggl API token
togglToken=<insert toggl token here>
togglWorkspaceId=<insert toggl workspace ID here>
togglUserId=<insert toggl user ID here>
```

The Toggl data is used to retrieve your time entries for the current day.

2. Then install the APK onto the device (you might need to restart the device after first installation as permissions might not get picked up properly - this is a known bug on the Android Things platform).
3. The alphanumeric display shows the total amount tracked today (HH.MM). After 8 hours a chime will play via the buzzer to remind you to refuel your batteries. Data is refreshed every minute.

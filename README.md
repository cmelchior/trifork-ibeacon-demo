Trifork iBeacon Demo
============================

This app is a demo app demonstrating the iBeacon technology on a Android Device.

It was created as part of a lunch talk about iBeacons and will probably break in a number of horrible
ways if used on a non-compatible device.

It has been tested on a LG Nexus 4 with Android 4.4.2

Slides can be found here: http://www.slideshare.net/ChristianMelchior/trifork-ibeacon-demo-lunch-talk

1. Screens
---------------------------
![iBeacon scanner](images/scan.png?raw=true)
![iBeacon data](images/data.png?raw=true)
![iBeacon ranging](images/range.png?raw=true)
![iBeacon log](images/log.png?raw=true)


2. iBeacon SDK's
---------------------------

Android doesn't handle iBeacons very well at the moment, so this project shows the usage of two
frameworks from Estimote and Radius Network that makes this a lot easier.

There is also a sample implementation of how to scan-by-hand, although it only support full scans
atm.

The default SDK used is from RadiusNetwork as that works for all iBeacons. Estimotes SDK only support
Estimotes.

Implementations are chosen by modifing the file *ApplicationModule.java* and return the appropriate
*IBeaconDetector* in *providesIBeaconDetector()*.


3. Demo App
---------------------------
The demo app can be found here: *apk/ibeacon_demo.apk*

Install running:

    > adb install ibeacon_demo.apk


4. License
---------------------------
    ----------------------------------------------------------------------------
    "THE BEER-WARE LICENSE" (Revision 42):
    <cme@trifork.com> wrote this app. As long as you retain this notice you
    can do whatever you want with this stuff. If we meet some day, and you think
    this stuff is worth it, you can buy me a beer in return.

    - Christian Melchior
    ----------------------------------------------------------------------------





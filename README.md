Trifork iBeacon Demo
============================

This app is a demo app demonstrating the iBeacon technology on a Android Device.

It was created as part of a lunch talk about iBeacons and will properly break in a number of horrible
ways if used on a non-compatible device

It has been tested on a LG Nexus 4 with Android 4.4.2

Slides can be found here:

1. Screens
---------------------------
![iBeacon scanner](images/scan.png?raw=true =200x)
![iBeacon data](images/data.png?raw=true =200x)
![iBeacon ranging](images/range.png?raw=true =200x)
![iBeacon log](images/log.png?raw=true =200x)


2. iBeacon SDK's
---------------------------

Android doesn't handle iBeacons very well at the moment, so this project shows the usage of two
frameworks from Estimote and Radius Network.

There is also a sample implementation of how to scan-by-hand, although it only support full scans
atm.

The default SDK used is from RadiusNetwork as that works for all iBeacons. Estimotes SDK only support
Estimotes.



3. License
---------------------------
    ----------------------------------------------------------------------------
    "THE BEER-WARE LICENSE" (Revision 42):
    <cme@trifork.com> wrote this program. As long as you retain this notice you
    can do whatever you want with this stuff. If we meet some day, and you think
    this stuff is worth it, you can buy me a beer in return.

    - Christian Melchior
    ----------------------------------------------------------------------------





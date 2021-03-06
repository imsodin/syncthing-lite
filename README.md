# Syncthing Lite

[![MPLv2 License](https://img.shields.io/badge/license-MPLv2-blue.svg?style=flat-square)](https://www.mozilla.org/MPL/2.0/)

This project is an Android app, that works as a client for a [Syncthing][1] share (accessing 
Syncthing devices in the same way a client-server file sharing app access its proprietary server). 

This is a client-oriented implementation, designed to work online by downloading and 
uploading files from an active device on the network (instead of synchronizing a local copy of 
the entire repository). This is quite different from the way the [syncthing-android][2] works, 
and its useful from those devices that cannot or wish not to download the entire repository (for 
example, mobile devices with limited storage available, wishing to access a syncthing share).

This project is based on [syncthing-java][3], a java implementation of Syncthing protocols.

[<img alt="Get it on Google Play" src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png" height="80">](https://play.google.com/store/apps/details?id=net.syncthing.lite)

## Building

The project uses a standard Android build, and requires the Android SDK. The easiest option is if
you install [Android Studio][4] and import the project.

The syncthing-java library is not stable yet. If you encounter any build errors, you probably have
to build it from source. To do this, clone the repo and run `gradle install`.

## License
All code is licensed under the [MPLv2 License][5].

[1]: https://syncthing.net/
[2]: https://github.com/syncthing/syncthing-android
[3]: https://github.com/Nutomic/syncthing-java
[4]: https://developer.android.com/studio/index.html
[5]: LICENSE

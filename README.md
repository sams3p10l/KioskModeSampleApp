# KioskSampleApp

Sample shell Android app for demoing kiosk mode.
Exit key is triple tap in the middle of the screen (middle of the star picture).

App kiosk mode is fully functional, you just need to add your own base functionality (currently it's a display of a yellow star).

Once you install the app on the device, use this ADB command to make app the device owner:
> adb shell dpm set-device-owner com.sams3p10l.kioskmodesample/.receiver.MyDeviceAdminReceiver

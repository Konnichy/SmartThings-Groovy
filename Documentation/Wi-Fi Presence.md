# Presence detection by monitoring a hostapd-based Wi-Fi access point

The following set of components enable SmartThings to update presence of household members, based on whether their mobile device is connected to the home Wi-Fi access point or not.

1. A Linux program (`hostapd_mon`) running on the Wi-Fi access point monitors connections and disconnections. It then forwards these events to the local SmartThings hub.

2. A SmartApp (`Wi-Fi Presence`) receives connections and disconnections events and deduces who connected/disconnected from their mobile device MAC address. The SmartApp then updates Simulated Presence Sensors as a result.


## Installation

### On the Linux Wi-Fi access point

First, on the Linux machine already operating as a Wi-Fi access point, make sure the directive `ctrl_interface=` is enabled in hostapd's configuration file (usually `/etc/hostapd/hostapd.conf`).

Then:

```
git clone https://github.com/Konnichy/WiFi-Presence.git
cd hostapd_mon
make
vi hostapd_mon.service  # edit this file with your specific parameters
sudo make install
```

This installs the binary in `/usr/local/bin/` by default. You may want to change this path in the `Makefile` before running `make install`.

Specific parameters you **have** to change are located in `hostapd_mon.service`. You'll have to adapt the two command-line arguments provided to `hostapd_mon`. The first one is path to the UNIX socket created by your instance of hostapd. The second one is the destination URL of events. Change the default hostname by your SmartThings hub hostname or IP address. Keep the protocol, port and path as they are.

### On the SmartThings side

- For each household member, add a corresponding device with the type `Simulated Presence Sensor` and give it the name of the person it represents
- Add the `Wi-Fi Presence` SmartApp in the SmartThings IDE
- Add the `Wi-Fi Presence` SmartApp from your mobile device, and configure it while matching every Simulated Presence Sensor with the MAC address of the person's mobile device

That shoud be it!

Now, you are able to base your automation on the presence of every person. For example, you can make the routine 'I'm Back!' activate automatically when any person connects to the Wi-Fi. Or make the routine 'Goodbye!' activate when nobody's home.

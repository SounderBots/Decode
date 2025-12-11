package org.firstinspires.ftc.teamcode.util;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

public class WifiMonitor {
    private final WifiManager wifiManager;

    public WifiMonitor() {
        Context context = AppUtil.getDefContext();
        this.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    public double getSignalStrength() {
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                // Returns RSSI in dBm (e.g., -50 is good, -90 is bad)
                return wifiInfo.getRssi();
            }
        }
        return -127.0; // Error/No Signal
    }

    public double getLinkSpeed() {
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                // Returns link speed in Mbps
                return wifiInfo.getLinkSpeed();
            }
        }
        return 0.0;
    }
    
    public String getSSID() {
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                return wifiInfo.getSSID();
            }
        }
        return "UNKNOWN";
    }
}

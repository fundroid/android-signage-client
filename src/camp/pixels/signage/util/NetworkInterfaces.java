/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package camp.pixels.signage.util;

import android.annotation.SuppressLint;

import org.apache.http.conn.util.InetAddressUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author rcarmo
 */


public class NetworkInterfaces {
    /**
     *
     * @param interfaceName
     * @return
     */
    public static final List<String> VALID_INTERFACES = Arrays.asList("eth0","wlan0");

    public static String getMACAddress(String interfaceName) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (interfaceName != null) {
                    if (!intf.getName().equalsIgnoreCase(interfaceName)) continue;
                }
                if (!VALID_INTERFACES.contains(intf.getName())) continue;
                byte[] mac = intf.getHardwareAddress();
                if (mac==null) continue;//return "";
                StringBuilder buf = new StringBuilder();
                for (int idx=0; idx<mac.length; idx++)
                    buf.append(String.format("%02X:", mac[idx]));       
                if (buf.length()>0) buf.deleteCharAt(buf.length()-1);
                //Log.d("getMACAddress", intf.getName());
                //Log.d("getMACAddress", buf.toString());
                return buf.toString();
            }
        } catch (SocketException ex) {} 
        return "";
    }

    /**
     * @param interfaceName eth0, wlan0 or NULL=use first interface 
     * @param useIPv4
     * @return address or empty string
     */
    @SuppressLint("DefaultLocale")
    public static String getIPAddress(String interfaceName, boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (interfaceName != null) {
                    if (!intf.getName().equalsIgnoreCase(interfaceName)) continue;
                }
                if (!VALID_INTERFACES.contains(intf.getName())) continue;
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress().toUpperCase();
                        boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr); 
                        if (useIPv4) {
                            if (isIPv4) 
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 port suffix
                                return delim<0 ? sAddr : sAddr.substring(0, delim);
                            }
                        }
                    }
                }
            }
        } catch (SocketException ex) {}
        return "";
    }
}

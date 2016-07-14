package com.loadtestgo.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

public class NetworkInfo {
    public static ArrayList<InetAddress> getInetAddrFor(String interfaceName) {
        try {
            ArrayList<InetAddress> addrs = new ArrayList<>();
            Enumeration e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                NetworkInterface network = (NetworkInterface)e.nextElement();
                if (network.getName().equals(interfaceName)) {
                    Enumeration ee = network.getInetAddresses();
                    while (ee.hasMoreElements()) {
                        InetAddress ip = (InetAddress) ee.nextElement();
                        addrs.add(ip);
                    }
                }
            }
            return addrs;
        } catch (SocketException e1) {
            return null;
        }
    }
}

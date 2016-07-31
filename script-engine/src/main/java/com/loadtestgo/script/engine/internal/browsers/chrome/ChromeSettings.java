package com.loadtestgo.script.engine.internal.browsers.chrome;

public class ChromeSettings {
    // Ignore certificate errors, a big warning page will be displayed
    // and Chrome will not allow to navigate to page with invalid certs
    // (including unsigned certs) without this.
    public boolean ignoreCertErrors = false;

    // Enable Quic protocol
    public boolean enableQuic = false;

    // The host for force Quic for, without this Quic will only be
    // used once Chrome see the alternative protocol specified in
    // a HTTP response header.
    public String forceQuicForHost = null;

    // Should we start the browser maximized
    public boolean startMaximized = false;

    // Reuse the existing profile if any
    public boolean reuseProfile = false;

    // Unpack the extension every time chrome is opened, sometimes this is
    // not necessary e.g. if you are launching & closing chrome in tight loop
    public boolean unpackExtension = true;

    // Chrome (42) hangs on Debian (Wheezy 64bit) sometimes during startup
    // I wasn't able to figure it out, fully but it seemed related to chrome
    // opening another chrome process with identical command line params, causing
    // the main process to hang waiting on the subprocess and the subprocess to hang
    // waiting on some resources the parent is using. Happened maybe every 100-200
    // jobs on a machine with %50 CPU load.
    // Anyway besides that the retry logic wil be needed for emergencies when chrome
    // is not cooperating...
    public int openBrowserRetryCount = 0;

    // Extra args to pass to the chrome process
    public String[] args;
}

package com.loadtestgo.script.tester;

import com.loadtestgo.script.tester.server.TestServer;

public class Main {
    public static void main(String[] args) throws Exception {
        // Normally unit tests are ran, but the test server can be run
        // as a standalone project for examining the behaviour of different
        // browsers or just testing the JVMs handling of sockets in general.
        TestServer server = new TestServer();
        server.start();
    }
}

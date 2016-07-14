package com.loadtestgo.script.tester.server;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;

public abstract class Server {
    protected ServerSocketChannel socketChannel;
    protected int port;
    protected String host;

    Server(String host, int port) {
        this.host = host;
        this.port = port;
    }

    abstract public String getDescription();

    public String getUrl() {
        return String.format("http://%s:%d", host, port);
    }

    public ServerSocketChannel getSocketChannel() { return socketChannel; }

    public abstract void handleAccept() throws IOException;
}

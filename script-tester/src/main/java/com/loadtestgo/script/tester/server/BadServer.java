package com.loadtestgo.script.tester.server;

import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class BadServer extends Server {
    private Type type;

    enum Type {
        CLOSE,
        RESET,
        TIMEOUT,
        NEVER_CONNECT
    }

    BadServer(Selector selector, Type type, String baseUrl, int port) {
        super(baseUrl, port);
        this.type = type;

        try {
            socketChannel = ServerSocketChannel.open();
            socketChannel.socket().bind(new InetSocketAddress(port));
            socketChannel.configureBlocking(false);
            if (type != Type.NEVER_CONNECT) {
                socketChannel.register(selector, SelectionKey.OP_ACCEPT);
            }
        } catch (IOException e) {
            Logger.error(e, "unable to open server socket");
        }
    }

    public void handleAccept() throws IOException {
        SocketChannel client = socketChannel.accept();
        switch (type) {
            case CLOSE:
                client.socket().close();
                break;
            case RESET:
                client.socket().setSoLinger(true, 0);
                client.socket().close();
                break;
            case TIMEOUT:
                TimeoutRunnable t = new TimeoutRunnable(client, 60 * 1000);
                Thread thread = new Thread(t, "BadServerTimeoutThread");
                thread.start();
                break;
            case NEVER_CONNECT:
                // We never get past the connect stage, so no point in 'accepting'
                break;
        }
    }

    public String getDescription() {
        switch (type) {
            case CLOSE:
                return "Accept connection then close socket right away";
            case RESET:
                return "Accept connection then reset socket right away";
            case TIMEOUT:
                return "Accept connection then wait for 60 seconds before closing";
            case NEVER_CONNECT:
                return "Listen, but never accept incoming connections";
            default:
                return "";
        }
    }

    class TimeoutRunnable implements Runnable {
        int timeoutMS;
        SocketChannel client;

        TimeoutRunnable(SocketChannel client, int timeoutMS) {
            this.client = client;
            this.timeoutMS = timeoutMS;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(timeoutMS);
                client.socket().close();
            } catch (InterruptedException e) {
                Logger.error(e, "Timeout thread interrupted");
            } catch (IOException e) {
                Logger.error(e, "unable to close socket");
            }
        }
    }
}

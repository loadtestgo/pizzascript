package com.loadtestgo.script.tester.server;

import com.loadtestgo.script.tester.pages.*;
import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A no frills HTTP Server for HTTP test content.
 *
 * This is for *testing* HTTP clients.
 *
 * This involves doing non-standard things to replicate scenarios that sometimes
 * happen in the real world when web servers go aerie and start misbehaving.
 *
 * Since this is a self contained web server we have complete control over
 * connections and how we process requests, without having to hack Jetty to
 * get the desired behaviour.
 *
 * After writing this initial version, it would probably be better to use Netty
 * after all, but, you know, here we are.
 *
 * There's quite a variety of custom web servers and programs out there serving
 * HTTP content.  Many big websites will use ads and tracking pixels often
 * served up by badly behaving web servers (and often this bad behaviour is on
 * purpose to help handle the volume of traffic).
 */
public class TestServer {
    private int port = 3000;
    private int numThreads = 6;
    private ArrayList<Thread> threads;
    private ArrayList<HttpHandlerThread> httpHandlerThreads;
    private LinkedBlockingQueue<Socket> connections;
    private ServerSocketChannel server;
    private volatile boolean running = false;
    private final Object serverStartedCondition = new Object();
    private Thread mainServerThread;
    private ArrayList<Server> servers = new ArrayList<>();

    public TestServer() {
        this.threads = new ArrayList<>();
        this.connections = new LinkedBlockingQueue<>();
        this.httpHandlerThreads = new ArrayList<>();
        this.server = null;
    }

    public void start() throws IOException {
        String baseUrl = getBaseUrl();

        Logger.info("Test HTTP server at {}", baseUrl);

        // The selector we use to listen on all sockets
        Selector selector = Selector.open();

        // The main server socket
        server = ServerSocketChannel.open();
        server.socket().bind(new InetSocketAddress(port));
        server.configureBlocking(false);
        server.register(selector, SelectionKey.OP_ACCEPT);

        // The 'bad' servers that do close connections and timeout and things
        // like that before any HTTP request is handled.  They each get their
        // own port as otherwise we don't know which logic to invoke.
        String host = getHostName();
        int port = this.port;
        servers.add(new BadServer(selector, BadServer.Type.CLOSE, host, ++port));
        servers.add(new BadServer(selector, BadServer.Type.RESET, host, ++port));
        servers.add(new BadServer(selector, BadServer.Type.TIMEOUT, host, ++port));
        servers.add(new BadServer(selector, BadServer.Type.NEVER_CONNECT, host, ++port));
        servers.add(new SslServer(selector, SslServer.Type.ECHO, host, ++port));

        running = true;

        PageRegistry pageRegistry = new PageRegistry();
        pageRegistry.registerClass(StatusCodes.class, "/status");
        pageRegistry.registerClass(BadResponses.class, "/bad");
        pageRegistry.registerClass(WeirdUrls.class, "/urls");
        pageRegistry.registerClass(Redirects.class, "/redirect");
        pageRegistry.registerClass(Timeouts.class, "/timeouts");
        pageRegistry.registerClass(TestApi.class, "/test-api");
        pageRegistry.registerClass(WebRtcApi.class, "/webrtc-api");
        pageRegistry.registerClass(Posts.class, "/post");
        pageRegistry.registerClass(Headers.class, "/headers");
        pageRegistry.registerClass(Auth.class, "/auth");
        pageRegistry.registerServers(servers);

        for (int i = 0; i < numThreads; ++i) {
            final HttpHandlerThread requestHandlerThread =
                    new HttpHandlerThread(connections, i, pageRegistry, baseUrl);
            httpHandlerThreads.add(requestHandlerThread);
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    requestHandlerThread.start();
                }
            }, "Http Handler");
            threads.add(thread);
            thread.start();
        }

        synchronized (serverStartedCondition) {
            serverStartedCondition.notify();
            if (mainServerThread == null) {
                mainServerThread = Thread.currentThread();
            }
        }

        while (running) {
            selector.select();
            for (SelectionKey key : selector.selectedKeys()) {
                selector.selectedKeys().remove(key);

                // Farm out the socket accept to the appropriate listener
                if (key.isAcceptable()) {
                    ServerSocketChannel socketChannel = ((ServerSocketChannel)key.channel());
                    if (socketChannel == server) {
                        SocketChannel client = socketChannel.accept();
                        connections.add(client.socket());
                    } else {
                        boolean handled = false;
                        for (Server server : servers) {
                            if (server.getSocketChannel() == socketChannel) {
                                handled = true;
                                server.handleAccept();
                                break;
                            }
                        }
                        if (!handled) {
                            Logger.error("Unable to find handler of socket, closing...");
                            SocketChannel client = socketChannel.accept();
                            client.close();
                        }
                    }
                }
            }
        }

        server.close();
    }

    public void startDetached() throws InterruptedException {
        mainServerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    start();
                } catch (IOException e) {
                    Logger.error(e, "Unable to start test web server");
                }
            }
        });

        mainServerThread.start();

        synchronized (serverStartedCondition) {
            serverStartedCondition.wait();
        }
    }

    public void stop() {
        running = false;
        mainServerThread.interrupt();
    }

    public String getBaseUrl() {
        return String.format("http://%s:%d/", getHostName(), port);
    }

    public String getHostName() {
        return "localhost";
    }
}

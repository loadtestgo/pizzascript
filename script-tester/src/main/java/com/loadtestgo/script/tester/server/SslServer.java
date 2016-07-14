package com.loadtestgo.script.tester.server;

import com.loadtestgo.util.Hex;
import org.pmw.tinylog.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.*;
import java.security.cert.CertificateException;

public class SslServer extends Server {
    private Type type;
    private SSLContext sslContext;

    enum Type {
        ECHO
    }

    SslServer(Selector selector, Type type, String host, int port) {
        super(host, port);
        this.type = type;

        sslContext = setupContext();
        if (sslContext == null) {
            return;
        }

        try {
            socketChannel = ServerSocketChannel.open();
            socketChannel.socket().bind(new InetSocketAddress(port));
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            Logger.error(e, "unable to close socket");
        }
    }

    private SSLContext setupContext() {
        String protocol = "TLS";
        String keyAlgorithm = "SunX509";

        try {
            /**
             * SSL server cert created with:
             *
             * keytool  -genkey -alias loadbot -keysize 2048 -validity 36500
             * -keyalg RSA -dname "CN=loadbot"
             * -keypass secret -storepass secret -keystore cert.jks
             */
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(SslServer.class.getResourceAsStream("/cert.jks"),
                    "secret".toCharArray());

            // Set up key manager factory to use our key store
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(keyAlgorithm);
            kmf.init(ks, "secret".toCharArray());

            SSLContext context = SSLContext.getInstance(protocol);
            context.init(kmf.getKeyManagers(), null, new java.security.SecureRandom());
            return context;
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void handleAccept() throws IOException {
        SocketChannel client = socketChannel.accept();
        switch (type) {
            case ECHO:
                BlockingRunnable t = new BlockingRunnable(client);
                Thread thread = new Thread(t, "BlockingSSLThread");
                thread.start();
                break;
        }
    }

    public String getUrl() {
        return String.format("https://%s:%d", host, port);
    }

    public String getDescription() {
        switch (type) {
            case ECHO:
                return "Echo SSL";
            default:
                return "";
        }
    }

    class BlockingRunnable implements Runnable {
        SocketChannel client;

        BlockingRunnable(SocketChannel client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                SslTransport sslTransport = new SslTransport(client, sslContext, host, port);
                handleRequest(sslTransport, sslTransport.getOutputStream(), new InputStreamReader(sslTransport.getInputStream()));
                sslTransport.close();
                client.close();
            } catch (IOException e) {
                Logger.error(e, "unable to close socket");
            } catch (InterruptedException e) {
                Logger.error(e, "unable to interrupt socket");
            }
        }

        private boolean handleRequest(SslTransport sslTransport,
                                      OutputStream output,
                                      InputStreamReader input) throws IOException, InterruptedException {
            String line = input.readLine();
            if (line == null || line.isEmpty()) {
                Logger.info("{}: HTTP request line is empty", 1);
                return false;
            }

            HttpRequestLine requestLine = new HttpRequestLine();
            if (!requestLine.parse(line)) {
                Logger.info("Unable to parse request line {}", line);
                return true;
            }

            String location = requestLine.getLocation();
            Logger.info("{}: {} {}", 1, requestLine.getType(), location);

            HttpRequest request = new HttpRequest(requestLine, input, output, null, getUrl());
            request.readHeaders();

            if (location.equals("/") || location.isEmpty()) {
                handleRequest(request, sslTransport);
            } else {
                request.write404Page();
            }

            return true;
        }

        private void handleRequest(HttpRequest request, SslTransport sslTransport) throws IOException {

            request.writeOk();
            request.writeDefaultHeaders();
            request.writeHeader("Content-Type", "text/plain");

            SSLSession session = sslTransport.getSslSession();

            StringBuilder buffer = new StringBuilder();
            buffer.append(String.format("SSL Protocol: %s\n", session.getProtocol()));
            buffer.append(String.format("Cipher Suite: %s\n", session.getCipherSuite()));
            buffer.append(String.format("Session Id: %s\n", Hex.bytesToHex(session.getId())));
            buffer.append(String.format("Client Hello: %s\n", sslTransport.getHandshakeParser().clientHelloVersion));
            buffer.append(String.format("Client Version: %s\n", sslTransport.getHandshakeParser().clientRequested));
            buffer.append(String.format("Server Version: %s\n", sslTransport.getHandshakeParser().serverResponded));

            String response = buffer.toString();
            request.writeHeader("Content-Length", String.valueOf(response.length()));
            request.writeln();
            request.write(response);
        }
    }
}
